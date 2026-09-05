package com.railway.reservation.provider;

import com.railway.reservation.dto.*;
import com.railway.reservation.entity.Train;
import com.railway.reservation.repository.ReservationRepository;
import com.railway.reservation.repository.TrainRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Master timetable reference provider for development & fallback.
 * Uses local database master timetable records.
 */
@Component
public class LocalTimetableProvider implements RailwayDataProvider {

    private final TrainRepository trainRepository;
    private final ReservationRepository reservationRepository;

    public LocalTimetableProvider(TrainRepository trainRepository, ReservationRepository reservationRepository) {
        this.trainRepository = trainRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public String getProviderName() {
        return "Local Master Database";
    }

    @Override
    public boolean isLiveProvider() {
        return false;
    }

    @Override
    public List<TrainDto> searchTrains(String source, String destination, LocalDate date) {
        List<Train> trains = trainRepository.searchTrains(source.trim(), destination.trim());
        LocalDate journeyDate = date != null ? date : LocalDate.now();
        List<TrainDto> dtoList = new ArrayList<>();

        for (Train train : trains) {
            long booked = reservationRepository.countConfirmedBookings(train.getId(), journeyDate);
            int available = Math.max(0, train.getTotalSeats() - (int) booked);

            List<String> classes = new ArrayList<>();
            if (train.getTrainClass() != null) {
                for (String s : train.getTrainClass().split(",")) {
                    if (!s.trim().isEmpty()) classes.add(s.trim());
                }
            }
            if (classes.isEmpty()) {
                classes = List.of("Sleeper (SL)", "AC 3 Tier (3A)", "AC 2 Tier (2A)", "AC First Class (1A)");
            }

            dtoList.add(new TrainDto(
                    train.getId(),
                    train.getTrainNumber(),
                    train.getTrainName(),
                    train.getSource(),
                    train.getDestination(),
                    train.getSourceCode() != null ? train.getSourceCode() : "",
                    train.getDestinationCode() != null ? train.getDestinationCode() : "",
                    train.getTrainType() != null ? train.getTrainType() : "Express",
                    train.getDepartureTime(),
                    train.getArrivalTime(),
                    train.getTotalSeats(),
                    available,
                    train.getFare(),
                    train.getTrainClass(),
                    train.getDistanceKm() != null ? train.getDistanceKm() : 0,
                    train.getRunningDays() != null ? train.getRunningDays() : "Daily",
                    journeyDate,
                    classes
            ));
        }
        return dtoList;
    }

    @Override
    public AvailabilityDto getTrainAvailability(String trainNumber, LocalDate date, String travelClass, String quota, Train train) {
        AvailabilityDto dto = new AvailabilityDto();
        dto.setTrainNumber(train.getTrainNumber());
        dto.setTrainName(train.getTrainName());
        dto.setJourneyDate(date != null ? date : LocalDate.now());
        dto.setSourceCode(train.getSourceCode());
        dto.setDestinationCode(train.getDestinationCode());
        dto.setQuota(quota != null ? quota : "GN");
        dto.setLiveDataAvailable(false);
        dto.setProviderSource("APPLICATION_INTERNAL_CAPACITY");
        dto.setDataSource("Application Internal Capacity (External PRS Availability Unconnected)");

        long booked = reservationRepository.countConfirmedBookings(train.getId(), dto.getJourneyDate());
        int totalAvailable = Math.max(0, train.getTotalSeats() - (int) booked);

        List<String> classes = train.getTrainClass() != null ?
                Arrays.asList(train.getTrainClass().split(",")) :
                List.of("Sleeper (SL)", "AC 3 Tier (3A)", "AC 2 Tier (2A)", "AC First Class (1A)");

        for (String cls : classes) {
            String clean = cls.trim();
            String code = extractClassCode(clean);
            double fare = calculateClassFare(train.getFare(), clean);
            int classSeats = Math.max(0, totalAvailable / classes.size());
            String status = classSeats > 0 ? "AVAILABLE" : "RAC";
            String statusDetails = classSeats > 0 ? "AVAILABLE-" + String.format("%04d", classSeats) : "RAC 12";

            dto.getClassAvailabilities().add(new AvailabilityDto.ClassAvailability(
                    code,
                    clean,
                    status,
                    statusDetails,
                    classSeats,
                    fare
            ));
        }

        return dto;
    }

    @Override
    public FareDetailsDto getTrainFare(String trainNumber, LocalDate date, Train train) {
        FareDetailsDto dto = new FareDetailsDto();
        dto.setTrainNumber(train.getTrainNumber());
        dto.setTrainName(train.getTrainName());
        dto.setJourneyDate(date != null ? date : LocalDate.now());
        dto.setSourceCode(train.getSourceCode());
        dto.setDestinationCode(train.getDestinationCode());
        dto.setDistanceKm(train.getDistanceKm() != null ? train.getDistanceKm() : 500);
        dto.setLiveDataAvailable(false);
        dto.setProviderSource("APPLICATION_ESTIMATE");
        dto.setDataSource("Calculated Telescopic Model (Application Estimate)");

        List<String> classes = train.getTrainClass() != null ?
                Arrays.asList(train.getTrainClass().split(",")) :
                List.of("Sleeper (SL)", "AC 3 Tier (3A)", "AC 2 Tier (2A)", "AC First Class (1A)");

        for (String cls : classes) {
            String clean = cls.trim();
            String code = extractClassCode(clean);
            double base = train.getFare();
            double multiplier = getMultiplier(clean);
            double classBase = Math.round(base * multiplier * 0.85 * 100.0) / 100.0;
            double resCharge = clean.contains("1A") || clean.contains("EC") ? 60.0 : 40.0;
            double superfast = train.getTrainType() != null && train.getTrainType().contains("Superfast") ? 45.0 : 0.0;
            double gst = clean.contains("AC") ? Math.round((classBase + resCharge + superfast) * 0.05 * 100.0) / 100.0 : 0.0;
            double total = Math.round((classBase + resCharge + superfast + gst) * 100.0) / 100.0;

            dto.getClassFares().add(new FareDetailsDto.ClassFareBreakdown(
                    code,
                    clean,
                    classBase,
                    resCharge,
                    superfast,
                    0.0,
                    gst,
                    0.0,
                    total
            ));
        }

        return dto;
    }

    @Override
    public PnrStatusDto getPnrStatus(String pnr) {
        PnrStatusDto dto = new PnrStatusDto();
        dto.setPnr(pnr);
        dto.setLiveDataAvailable(false);
        dto.setDataSource("NOT_CONFIGURED");
        dto.setErrorMessage("Official Indian Railways PNR lookup requires an active authorized CRIS/IRCTC gateway subscription. For internal bookings made on this app, please search in the 'Check Booking Reference' section.");
        dto.setBookingStatus("UNAVAILABLE");
        return dto;
    }

    private String extractClassCode(String name) {
        if (name.contains("1A") || name.contains("First")) return "1A";
        if (name.contains("2A") || name.contains("2 Tier")) return "2A";
        if (name.contains("3A") || name.contains("3 Tier")) return "3A";
        if (name.contains("CC") || name.contains("Chair")) return "CC";
        if (name.contains("EC") || name.contains("Executive")) return "EC";
        if (name.contains("2S") || name.contains("Second")) return "2S";
        return "SL";
    }

    private double calculateClassFare(double baseFare, String travelClass) {
        double multiplier = getMultiplier(travelClass);
        return Math.round(baseFare * multiplier * 100.0) / 100.0;
    }

    private double getMultiplier(String travelClass) {
        String normalized = travelClass.toUpperCase();
        if (normalized.contains("1A") || normalized.contains("FIRST") || normalized.contains("1 TIER") || normalized.contains("EXECUTIVE") || normalized.contains("EC")) {
            return 2.8;
        } else if (normalized.contains("2A") || normalized.contains("2 TIER")) {
            return 2.0;
        } else if (normalized.contains("3A") || normalized.contains("3 TIER")) {
            return 1.4;
        } else if (normalized.contains("CC") || normalized.contains("CHAIR")) {
            return 1.2;
        } else if (normalized.contains("2S") || normalized.contains("SECOND")) {
            return 0.6;
        }
        return 1.0;
    }
}
