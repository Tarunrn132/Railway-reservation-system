package com.railway.reservation.service;

import com.railway.reservation.dto.StationDto;
import com.railway.reservation.dto.TrainDto;
import com.railway.reservation.dto.TrainScheduleDto;
import com.railway.reservation.entity.Train;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.provider.ExternalRailwayDataProvider;
import com.railway.reservation.repository.ReservationRepository;
import com.railway.reservation.repository.TrainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrainService {

    private static final Logger log = LoggerFactory.getLogger(TrainService.class);

    private final TrainRepository trainRepository;
    private final ReservationRepository reservationRepository;
    private final StationService stationService;
    private final ExternalRailwayDataProvider externalProvider;

    public TrainService(TrainRepository trainRepository,
                        ReservationRepository reservationRepository,
                        StationService stationService,
                        ExternalRailwayDataProvider externalProvider) {
        this.trainRepository = trainRepository;
        this.reservationRepository = reservationRepository;
        this.stationService = stationService;
        this.externalProvider = externalProvider;
    }

    public List<TrainDto> getAllTrains(LocalDate searchDate) {
        LocalDate date = searchDate != null ? searchDate : LocalDate.now();
        return trainRepository.findAll().stream()
                .filter(t -> isTrainRunningOnDate(t.getRunningDays(), date))
                .map(train -> mapToDto(train, date))
                .collect(Collectors.toList());
    }

    public List<TrainDto> searchTrains(String source, String destination, LocalDate journeyDate) {
        if (source == null || source.trim().isEmpty()) {
            throw new BadRequestException("Source station cannot be empty");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new BadRequestException("Destination station cannot be empty");
        }

        String rawSource = source.trim();
        String rawDest = destination.trim();

        String srcCode = stationService.resolveStationCode(rawSource);
        String dstCode = stationService.resolveStationCode(rawDest);

        if (srcCode.equalsIgnoreCase(dstCode)) {
            throw new BadRequestException("Departure and destination stations cannot be the same");
        }
        if (journeyDate != null && journeyDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Journey date cannot be in the past");
        }

        LocalDate date = journeyDate != null ? journeyDate : LocalDate.now();

        // Check if external live search provider is configured
        if (externalProvider != null && externalProvider.isSearchConfigured()) {
            try {
                log.info("Querying live railway provider for trains between {} and {}", srcCode, dstCode);
                List<TrainDto> liveResults = externalProvider.searchTrains(srcCode, dstCode, date);
                if (liveResults != null && !liveResults.isEmpty()) {
                    return liveResults;
                }
            } catch (Exception e) {
                log.warn("External railway train search failed: {}. Falling back to authentic master timetable.", e.getMessage());
            }
        }

        // Query master repository using station code or name
        Set<Train> resultSet = new LinkedHashSet<>();
        resultSet.addAll(trainRepository.searchTrains(srcCode, dstCode));
        if (resultSet.isEmpty()) {
            resultSet.addAll(trainRepository.searchTrains(rawSource, rawDest));
        }

        // Also check intermediate route stops if direct search returned empty
        if (resultSet.isEmpty()) {
            List<Train> allActive = trainRepository.findAll();
            for (Train t : allActive) {
                if (t.getIntermediateRoute() != null && !t.getIntermediateRoute().isEmpty()) {
                    String[] stops = t.getIntermediateRoute().split(",");
                    int srcIdx = -1;
                    int dstIdx = -1;
                    for (int i = 0; i < stops.length; i++) {
                        String stop = stops[i].trim();
                        if (stop.equalsIgnoreCase(srcCode) || stop.equalsIgnoreCase(rawSource)) {
                            srcIdx = i;
                        }
                        if (stop.equalsIgnoreCase(dstCode) || stop.equalsIgnoreCase(rawDest)) {
                            dstIdx = i;
                        }
                    }
                    if (srcIdx != -1 && dstIdx != -1 && srcIdx < dstIdx) {
                        resultSet.add(t);
                    }
                }
            }
        }

        // Filter by running day on the backend (do not trust frontend)
        return resultSet.stream()
                .filter(train -> isTrainRunningOnDate(train.getRunningDays(), date))
                .map(train -> mapToDto(train, date))
                .collect(Collectors.toList());
    }

    public boolean isTrainRunningOnDate(String runningDays, LocalDate date) {
        if (date == null || runningDays == null || runningDays.trim().isEmpty()) {
            return true;
        }
        String rd = runningDays.trim().toLowerCase();
        if (rd.contains("daily")) {
            return true;
        }
        DayOfWeek dow = date.getDayOfWeek();
        String dayName = dow.name().toLowerCase(); // e.g. monday
        String dayShort = dayName.substring(0, 3); // e.g. mon

        if (rd.startsWith("except") || rd.startsWith("excluding")) {
            boolean matchesExcluded = rd.contains(dayName) || rd.contains(dayShort);
            return !matchesExcluded;
        }

        return rd.contains(dayName) || rd.contains(dayShort);
    }

    public Train getTrainById(Long id) {
        return trainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with ID: " + id));
    }

    public Train getTrainByNumber(String trainNumber) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            throw new BadRequestException("Train number cannot be empty");
        }
        return trainRepository.findByTrainNumber(trainNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with number: " + trainNumber.trim()));
    }

    public TrainDto getTrainDtoById(Long id, LocalDate journeyDate) {
        Train train = getTrainById(id);
        LocalDate date = journeyDate != null ? journeyDate : LocalDate.now();
        return mapToDto(train, date);
    }

    public TrainDto getTrainDtoByNumber(String trainNumber, LocalDate journeyDate) {
        Train train = getTrainByNumber(trainNumber);
        LocalDate date = journeyDate != null ? journeyDate : LocalDate.now();
        return mapToDto(train, date);
    }

    public TrainScheduleDto getTrainSchedule(String trainNumber) {
        Train train = getTrainByNumber(trainNumber);

        List<TrainScheduleDto.StationStopDto> stops = new ArrayList<>();
        if (train.getIntermediateRoute() != null && !train.getIntermediateRoute().trim().isEmpty()) {
            String[] stopCodes = train.getIntermediateRoute().split(",");
            int totalStops = stopCodes.length;
            int totalDist = train.getDistanceKm() != null ? train.getDistanceKm() : 500;

            for (int i = 0; i < stopCodes.length; i++) {
                String code = stopCodes[i].trim().toUpperCase();
                String name = code;
                String city = code;
                String state = "";
                try {
                    StationDto stationDto = stationService.getStationByCode(code);
                    name = stationDto.getStationName();
                    city = stationDto.getCity();
                    state = stationDto.getState();
                } catch (Exception ignored) {
                }

                int estDist = totalStops > 1 ? (totalDist * i) / (totalStops - 1) : 0;
                String arr = (i == 0) ? "Source" : ((i == totalStops - 1) ? train.getArrivalTime() : "--");
                String dep = (i == totalStops - 1) ? "Destination" : ((i == 0) ? train.getDepartureTime() : "--");

                stops.add(new TrainScheduleDto.StationStopDto(
                        i + 1, code, name, city, state, estDist, arr, dep
                ));
            }
        } else {
            stops.add(new TrainScheduleDto.StationStopDto(1, train.getSourceCode(), train.getSource(), train.getSource(), "", 0, "Source", train.getDepartureTime()));
            stops.add(new TrainScheduleDto.StationStopDto(2, train.getDestinationCode(), train.getDestination(), train.getDestination(), "", train.getDistanceKm(), train.getArrivalTime(), "Destination"));
        }

        return new TrainScheduleDto(
                train.getTrainNumber(),
                train.getTrainName(),
                train.getTrainType(),
                train.getSource(),
                train.getDestination(),
                train.getSourceCode(),
                train.getDestinationCode(),
                train.getDepartureTime(),
                train.getArrivalTime(),
                train.getRunningDays(),
                train.getDistanceKm(),
                stops
        );
    }

    public List<String> getAllStations() {
        Set<String> stations = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        stationService.getAllActiveStations().forEach(s -> stations.add(s.getDisplayName()));
        return new ArrayList<>(stations);
    }

    public TrainDto mapToDto(Train train, LocalDate journeyDate) {
        long confirmedBookings = reservationRepository.countConfirmedBookings(train.getId(), journeyDate);
        int availableSeats = Math.max(0, train.getTotalSeats() - (int) confirmedBookings);

        List<String> classes = new ArrayList<>();
        if (train.getTrainClass() != null) {
            String[] split = train.getTrainClass().split(",");
            for (String s : split) {
                if (!s.trim().isEmpty()) {
                    classes.add(s.trim());
                }
            }
        }
        if (classes.isEmpty()) {
            classes.add("Sleeper (SL)");
            classes.add("AC 3 Tier (3A)");
            classes.add("AC 2 Tier (2A)");
            classes.add("AC First Class (1A)");
        }

        return new TrainDto(
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
                availableSeats,
                train.getFare(),
                train.getTrainClass(),
                train.getDistanceKm() != null ? train.getDistanceKm() : 0,
                train.getRunningDays() != null ? train.getRunningDays() : "Daily",
                train.getIntermediateRoute(),
                journeyDate,
                classes
        );
    }
}

