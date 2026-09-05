package com.railway.reservation.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.reservation.dto.*;
import com.railway.reservation.entity.Train;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * External Railway Data Provider.
 * Connects to upstream Indian Railway REST APIs for train search, availability, fare, and PNR inquiries.
 */
@Component
public class ExternalRailwayDataProvider implements RailwayDataProvider {

    private static final Logger log = LoggerFactory.getLogger(ExternalRailwayDataProvider.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    // Granular URLs and Keys
    @Value("${railway.search.provider:}")
    private String searchProvider;
    @Value("${railway.search.api.url:}")
    private String searchApiUrl;
    @Value("${railway.search.api.key:}")
    private String searchApiKey;

    @Value("${railway.availability.provider:}")
    private String availabilityProvider;
    @Value("${railway.availability.api.url:}")
    private String availabilityApiUrl;
    @Value("${railway.availability.api.key:}")
    private String availabilityApiKey;

    @Value("${railway.pnr.provider:}")
    private String pnrProvider;
    @Value("${railway.pnr.api.url:}")
    private String pnrApiUrl;
    @Value("${railway.pnr.api.key:}")
    private String pnrApiKey;

    @Value("${railway.fare.provider:}")
    private String fareProvider;
    @Value("${railway.fare.api.url:}")
    private String fareApiUrl;
    @Value("${railway.fare.api.key:}")
    private String fareApiKey;

    // Legacy Fallback
    @Value("${railway.api.url:}")
    private String legacyApiUrl;
    @Value("${railway.api.key:}")
    private String legacyApiKey;
    @Value("${railway.api.provider:ExternalRailwayAPI}")
    private String legacyProviderName;

    public ExternalRailwayDataProvider() {
        this.objectMapper = new ObjectMapper();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public void configure(String url, String key, String name) {
        this.legacyApiUrl = url;
        this.legacyApiKey = key;
        this.legacyProviderName = name;
    }

    @Override
    public String getProviderName() {
        if (searchProvider != null && !searchProvider.trim().isEmpty()) return searchProvider;
        if (legacyProviderName != null && !legacyProviderName.trim().isEmpty()) return legacyProviderName;
        return "ExternalRailwayAPI";
    }

    @Override
    public boolean isLiveProvider() {
        return isSearchConfigured() || isAvailabilityConfigured() || isFareConfigured() || isPnrConfigured();
    }

    public boolean isSearchConfigured() {
        String url = getSearchUrl();
        String key = getSearchKey();
        return url != null && !url.trim().isEmpty() && key != null && !key.trim().isEmpty();
    }

    public boolean isAvailabilityConfigured() {
        String url = getAvailabilityUrl();
        String key = getAvailabilityKey();
        return url != null && !url.trim().isEmpty() && key != null && !key.trim().isEmpty();
    }

    public boolean isFareConfigured() {
        String url = getFareUrl();
        String key = getFareKey();
        return url != null && !url.trim().isEmpty() && key != null && !key.trim().isEmpty();
    }

    public boolean isPnrConfigured() {
        String url = getPnrUrl();
        String key = getPnrKey();
        return url != null && !url.trim().isEmpty() && key != null && !key.trim().isEmpty();
    }

    private String getSearchUrl() {
        return (searchApiUrl != null && !searchApiUrl.trim().isEmpty()) ? searchApiUrl : legacyApiUrl;
    }

    private String getSearchKey() {
        return (searchApiKey != null && !searchApiKey.trim().isEmpty()) ? searchApiKey : legacyApiKey;
    }

    private String getAvailabilityUrl() {
        return (availabilityApiUrl != null && !availabilityApiUrl.trim().isEmpty()) ? availabilityApiUrl : legacyApiUrl;
    }

    private String getAvailabilityKey() {
        return (availabilityApiKey != null && !availabilityApiKey.trim().isEmpty()) ? availabilityApiKey : legacyApiKey;
    }

    private String getFareUrl() {
        return (fareApiUrl != null && !fareApiUrl.trim().isEmpty()) ? fareApiUrl : legacyApiUrl;
    }

    private String getFareKey() {
        return (fareApiKey != null && !fareApiKey.trim().isEmpty()) ? fareApiKey : legacyApiKey;
    }

    private String getPnrUrl() {
        return (pnrApiUrl != null && !pnrApiUrl.trim().isEmpty()) ? pnrApiUrl : legacyApiUrl;
    }

    private String getPnrKey() {
        return (pnrApiKey != null && !pnrApiKey.trim().isEmpty()) ? pnrApiKey : legacyApiKey;
    }

    @Override
    public List<TrainDto> searchTrains(String source, String destination, LocalDate date) {
        if (!isSearchConfigured()) {
            throw new IllegalStateException("External railway train search API is not configured");
        }
        String endpoint = getSearchUrl() + "/trains/between?from=" + source + "&to=" + destination + "&date=" + date;
        String response = executeGet(endpoint, getSearchKey());
        return parseTrainSearchResponse(response, date);
    }

    @Override
    public AvailabilityDto getTrainAvailability(String trainNumber, LocalDate date, String travelClass, String quota, Train trainMaster) {
        if (!isAvailabilityConfigured()) {
            throw new IllegalStateException("External availability API is not configured");
        }
        String endpoint = getAvailabilityUrl() + "/" + trainNumber + "?date=" + date + "&class=" + travelClass + "&quota=" + quota;
        String response = executeGet(endpoint, getAvailabilityKey());
        return parseAvailabilityResponse(response, trainMaster, date, quota);
    }

    @Override
    public FareDetailsDto getTrainFare(String trainNumber, LocalDate date, Train trainMaster) {
        if (!isFareConfigured()) {
            throw new IllegalStateException("External fare API is not configured");
        }
        String endpoint = getFareUrl() + "/" + trainNumber + "?date=" + date;
        String response = executeGet(endpoint, getFareKey());
        return parseFareResponse(response, trainMaster, date);
    }

    @Override
    public PnrStatusDto getPnrStatus(String pnr) {
        if (!isPnrConfigured()) {
            throw new IllegalStateException("External PNR API is not configured");
        }
        String endpoint = getPnrUrl() + "/" + pnr;
        String response = executeGet(endpoint, getPnrKey());
        return parsePnrResponse(response, pnr);
    }

    private String executeGet(String url, String key) {
        log.info("Dispatching external request to railway API: {}", maskUrlForLogs(url));
        return restClient.get()
                .uri(url)
                .header("X-API-KEY", key)
                .header("User-Agent", "RailwayReservationSystem/1.0")
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    throw new RestClientResponseException(
                            "External Railway API returned HTTP " + resp.getStatusCode(),
                            resp.getStatusCode(),
                            resp.getStatusText(),
                            resp.getHeaders(),
                            resp.getBody().readAllBytes(),
                            null
                    );
                })
                .body(String.class);
    }

    private String maskUrlForLogs(String url) {
        if (url == null) return "";
        return url.replaceAll("key=[^&]+", "key=***");
    }

    public List<TrainDto> parseTrainSearchResponse(String json, LocalDate date) {
        List<TrainDto> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode dataNode = root.path("data");
            if (dataNode.isArray()) {
                for (JsonNode item : dataNode) {
                    TrainDto dto = new TrainDto();
                    dto.setTrainNumber(item.path("train_number").asText());
                    dto.setTrainName(item.path("train_name").asText());
                    dto.setSource(item.path("from_station").asText());
                    dto.setDestination(item.path("to_station").asText());
                    dto.setDepartureTime(item.path("departure_time").asText());
                    dto.setArrivalTime(item.path("arrival_time").asText());
                    dto.setAvailableSeats(item.path("available_seats").asInt(50));
                    dto.setFare(item.path("base_fare").asDouble(500.0));
                    dto.setSearchDate(date != null ? date : LocalDate.now());
                    result.add(dto);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse external train search response: {}", e.getMessage());
        }
        return result;
    }

    public AvailabilityDto parseAvailabilityResponse(String json, Train trainMaster, LocalDate date, String quota) {
        AvailabilityDto dto = new AvailabilityDto();
        dto.setTrainNumber(trainMaster != null ? trainMaster.getTrainNumber() : "");
        dto.setTrainName(trainMaster != null ? trainMaster.getTrainName() : "");
        dto.setJourneyDate(date);
        dto.setSourceCode(trainMaster != null ? trainMaster.getSourceCode() : "");
        dto.setDestinationCode(trainMaster != null ? trainMaster.getDestinationCode() : "");
        dto.setQuota(quota != null ? quota : "GN");

        try {
            JsonNode root = objectMapper.readTree(json);
            dto.setLiveDataAvailable(true);
            dto.setProviderSource("LIVE_EXTERNAL_AVAILABILITY");
            dto.setDataSource("External Railway Availability Provider (" + (availabilityProvider != null ? availabilityProvider : getProviderName()) + ")");

            JsonNode list = root.path("data").isArray() ? root.path("data") : root.path("availability");
            if (list.isArray()) {
                for (JsonNode item : list) {
                    dto.getClassAvailabilities().add(new AvailabilityDto.ClassAvailability(
                            item.path("class").asText("SL"),
                            item.path("class_name").asText("Sleeper"),
                            item.path("status").asText("AVAILABLE"),
                            item.path("status_details").asText("AVAILABLE-24"),
                            item.path("seats").asInt(24),
                            item.path("fare").asDouble(450.0)
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse availability response: {}", e.getMessage());
            dto.setLiveDataAvailable(false);
            dto.setProviderSource("NOT_CONFIGURED");
            dto.setDataSource("External Provider Failed");
        }
        return dto;
    }

    public FareDetailsDto parseFareResponse(String json, Train trainMaster, LocalDate date) {
        FareDetailsDto dto = new FareDetailsDto();
        dto.setTrainNumber(trainMaster != null ? trainMaster.getTrainNumber() : "");
        dto.setTrainName(trainMaster != null ? trainMaster.getTrainName() : "");
        dto.setJourneyDate(date);
        dto.setSourceCode(trainMaster != null ? trainMaster.getSourceCode() : "");
        dto.setDestinationCode(trainMaster != null ? trainMaster.getDestinationCode() : "");
        dto.setDistanceKm(trainMaster != null ? trainMaster.getDistanceKm() : 0);

        try {
            JsonNode root = objectMapper.readTree(json);
            dto.setLiveDataAvailable(true);
            dto.setProviderSource("OFFICIAL_FARE");
            dto.setDataSource("External Railway Fare Provider (" + (fareProvider != null ? fareProvider : getProviderName()) + ")");

            JsonNode list = root.path("data").isArray() ? root.path("data") : root.path("fares");
            if (list.isArray()) {
                for (JsonNode item : list) {
                    dto.getClassFares().add(new FareDetailsDto.ClassFareBreakdown(
                            item.path("class").asText("SL"),
                            item.path("class_name").asText("Sleeper"),
                            item.path("base_fare").asDouble(350.0),
                            item.path("reservation_charge").asDouble(20.0),
                            item.path("superfast_charge").asDouble(30.0),
                            item.path("dynamic_pricing").asDouble(0.0),
                            item.path("gst").asDouble(0.0),
                            item.path("catering").asDouble(0.0),
                            item.path("total_fare").asDouble(400.0)
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse fare response: {}", e.getMessage());
            dto.setLiveDataAvailable(false);
            dto.setProviderSource("NOT_CONFIGURED");
            dto.setDataSource("External Provider Failed");
        }
        return dto;
    }

    public PnrStatusDto parsePnrResponse(String json, String pnr) {
        PnrStatusDto dto = new PnrStatusDto();
        dto.setPnr(pnr);
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.has("data") ? root.get("data") : root;
            dto.setLiveDataAvailable(true);
            dto.setDataSource("Official Indian Railways PRS (" + (pnrProvider != null ? pnrProvider : getProviderName()) + ")");
            dto.setTrainNumber(data.path("train_number").asText());
            dto.setTrainName(data.path("train_name").asText());
            dto.setSourceStation(data.path("from_station").asText());
            dto.setDestinationStation(data.path("to_station").asText());
            dto.setBoardingStation(data.path("boarding_station").asText());
            dto.setReservationClass(data.path("class").asText());
            dto.setQuota(data.path("quota").asText("GN"));
            dto.setChartStatus(data.path("chart_status").asText("CHART NOT PREPARED"));
            dto.setBookingStatus(data.path("booking_status").asText("CONFIRMED"));

            JsonNode passengers = data.path("passengers");
            if (passengers.isArray()) {
                int index = 1;
                for (JsonNode p : passengers) {
                    dto.getPassengerStatuses().add(new PnrStatusDto.PassengerPnrStatus(
                            index++,
                            p.path("booking_status").asText("CNF"),
                            p.path("current_status").asText("CNF"),
                            p.path("coach").asText("B1"),
                            p.path("berth").asText("24"),
                            p.path("berth_type").asText("LOWER")
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse PNR response: {}", e.getMessage());
            dto.setLiveDataAvailable(false);
            dto.setErrorMessage("Failed to parse official PNR status response.");
        }
        return dto;
    }
}
