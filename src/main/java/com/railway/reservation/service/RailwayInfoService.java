package com.railway.reservation.service;

import com.railway.reservation.dto.AvailabilityDto;
import com.railway.reservation.dto.FareDetailsDto;
import com.railway.reservation.dto.PnrStatusDto;
import com.railway.reservation.entity.Train;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.provider.ExternalRailwayDataProvider;
import com.railway.reservation.provider.LocalTimetableProvider;
import com.railway.reservation.repository.TrainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RailwayInfoService {

    private static final Logger log = LoggerFactory.getLogger(RailwayInfoService.class);

    private final TrainRepository trainRepository;
    private final ExternalRailwayDataProvider externalProvider;
    private final LocalTimetableProvider localProvider;

    private final Map<String, CachedStatus<AvailabilityDto>> availabilityCache = new ConcurrentHashMap<>();
    private final Map<String, CachedStatus<FareDetailsDto>> fareCache = new ConcurrentHashMap<>();
    
    // Short cache for live seat availability to avoid stale booking information
    private static final long AVAILABILITY_CACHE_TTL_MILLIS = 30_000; // 30 seconds
    private static final long FARE_CACHE_TTL_MILLIS = 300_000; // 5 minutes

    public RailwayInfoService(TrainRepository trainRepository,
                              ExternalRailwayDataProvider externalProvider,
                              LocalTimetableProvider localProvider) {
        this.trainRepository = trainRepository;
        this.externalProvider = externalProvider;
        this.localProvider = localProvider;
    }

    public AvailabilityDto getTrainAvailability(String trainNumber, LocalDate journeyDate, String travelClass, String quota) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            throw new ResourceNotFoundException("Train number cannot be empty");
        }

        String normalizedNumber = trainNumber.trim();
        LocalDate date = journeyDate != null ? journeyDate : LocalDate.now();
        String cacheKey = normalizedNumber + "_" + date + "_" + (travelClass != null ? travelClass : "ALL") + "_" + (quota != null ? quota : "GN");

        CachedStatus<AvailabilityDto> cached = availabilityCache.get(cacheKey);
        if (cached != null && !cached.isExpired(AVAILABILITY_CACHE_TTL_MILLIS)) {
            return cached.data;
        }

        Train train = trainRepository.findByTrainNumber(normalizedNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Train #" + normalizedNumber + " not found."));

        AvailabilityDto dto;
        if (externalProvider.isAvailabilityConfigured()) {
            try {
                dto = externalProvider.getTrainAvailability(normalizedNumber, date, travelClass != null ? travelClass : "ALL", quota, train);
            } catch (Exception e) {
                log.warn("Live railway availability API call failed: {}. Reporting provider unavailable.", e.getMessage());
                dto = new AvailabilityDto();
                dto.setTrainNumber(train.getTrainNumber());
                dto.setTrainName(train.getTrainName());
                dto.setJourneyDate(date);
                dto.setSourceCode(train.getSourceCode());
                dto.setDestinationCode(train.getDestinationCode());
                dto.setQuota(quota != null ? quota : "GN");
                dto.setLiveDataAvailable(false);
                dto.setProviderSource("PROVIDER_UNAVAILABLE");
                dto.setDataSource("Live railway availability is temporarily unavailable. Please try again.");
            }
        } else {
            dto = localProvider.getTrainAvailability(normalizedNumber, date, travelClass, quota, train);
        }

        availabilityCache.put(cacheKey, new CachedStatus<>(dto, System.currentTimeMillis()));
        return dto;
    }

    public FareDetailsDto getTrainFare(String trainNumber, LocalDate journeyDate) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            throw new ResourceNotFoundException("Train number cannot be empty");
        }

        String normalizedNumber = trainNumber.trim();
        LocalDate date = journeyDate != null ? journeyDate : LocalDate.now();
        String cacheKey = normalizedNumber + "_" + date;

        CachedStatus<FareDetailsDto> cached = fareCache.get(cacheKey);
        if (cached != null && !cached.isExpired(FARE_CACHE_TTL_MILLIS)) {
            return cached.data;
        }

        Train train = trainRepository.findByTrainNumber(normalizedNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Train #" + normalizedNumber + " not found."));

        FareDetailsDto dto;
        if (externalProvider.isFareConfigured()) {
            try {
                dto = externalProvider.getTrainFare(normalizedNumber, date, train);
            } catch (Exception e) {
                log.warn("External fare API call failed: {}. Falling back to calculated fare breakdown.", e.getMessage());
                dto = localProvider.getTrainFare(normalizedNumber, date, train);
            }
        } else {
            dto = localProvider.getTrainFare(normalizedNumber, date, train);
        }

        fareCache.put(cacheKey, new CachedStatus<>(dto, System.currentTimeMillis()));
        return dto;
    }

    public PnrStatusDto getPnrStatus(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            throw new ResourceNotFoundException("PNR cannot be empty");
        }

        String cleanPnr = pnr.trim().toUpperCase();
        if (externalProvider.isPnrConfigured()) {
            try {
                return externalProvider.getPnrStatus(cleanPnr);
            } catch (Exception e) {
                log.warn("External PNR API call failed: {}", e.getMessage());
                PnrStatusDto dto = localProvider.getPnrStatus(cleanPnr);
                dto.setErrorMessage("Live railway PNR status is temporarily unavailable. Please try again later.");
                return dto;
            }
        }
        return localProvider.getPnrStatus(cleanPnr);
    }

    public void clearCache() {
        availabilityCache.clear();
        fareCache.clear();
    }

    public void setApiConfiguration(String url, String key, String provider) {
        externalProvider.configure(url, key, provider);
    }

    private static class CachedStatus<T> {
        final T data;
        final long timestamp;

        CachedStatus(T data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }

        boolean isExpired(long ttlMillis) {
            return System.currentTimeMillis() - timestamp > ttlMillis;
        }
    }
}
