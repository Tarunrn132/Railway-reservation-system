package com.railway.reservation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.reservation.dto.StationImportSummary;
import com.railway.reservation.entity.Station;
import com.railway.reservation.repository.StationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class StationImportService {

    private static final Logger log = LoggerFactory.getLogger(StationImportService.class);
    private static final Pattern STATION_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{2,7}$");

    private final StationRepository stationRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public StationImportService(StationRepository stationRepository,
                                ObjectMapper objectMapper,
                                ResourceLoader resourceLoader) {
        this.stationRepository = stationRepository;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    @Transactional
    public StationImportSummary importFromResource(String resourcePath) {
        StationImportSummary summary = new StationImportSummary();
        try {
            Resource resource = resourceLoader.getResource(resourcePath);
            if (!resource.exists()) {
                log.warn("Station resource not found: {}", resourcePath);
                summary.addError("Resource not found: " + resourcePath);
                return summary;
            }
            try (InputStream is = resource.getInputStream()) {
                return importFromInputStream(is);
            }
        } catch (Exception e) {
            log.error("Failed to import stations from resource: {}", resourcePath, e);
            summary.addError("Exception during resource import: " + e.getMessage());
            return summary;
        }
    }

    @Transactional
    public StationImportSummary importFromInputStream(InputStream inputStream) {
        StationImportSummary summary = new StationImportSummary();
        try {
            List<Map<String, Object>> records = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
            log.info("Station import started with {} raw records...", records.size());

            Set<String> processedCodesInBatch = new HashSet<>();

            for (Map<String, Object> row : records) {
                String rawCode = row.get("stationCode") != null ? row.get("stationCode").toString().trim().toUpperCase() : null;
                String rawName = row.get("stationName") != null ? row.get("stationName").toString().trim() : null;
                String city = row.get("city") != null ? row.get("city").toString().trim() : null;
                String district = row.get("district") != null ? row.get("district").toString().trim() : null;
                String state = row.get("state") != null ? row.get("state").toString().trim() : null;
                String zone = row.get("railwayZone") != null ? row.get("railwayZone").toString().trim().toUpperCase() : null;
                String division = row.get("railwayDivision") != null ? row.get("railwayDivision").toString().trim().toUpperCase() : null;
                String aliases = row.get("aliases") != null ? row.get("aliases").toString().trim() : null;
                Double lat = parseDouble(row.get("latitude"));
                Double lon = parseDouble(row.get("longitude"));
                Boolean active = row.get("active") != null ? Boolean.valueOf(row.get("active").toString()) : true;

                // Validation
                if (rawCode == null || rawCode.isEmpty() || !STATION_CODE_PATTERN.matcher(rawCode).matches()) {
                    summary.incrementInvalid();
                    summary.addError("Invalid station code: '" + rawCode + "' in record: " + rawName);
                    continue;
                }

                if (rawName == null || rawName.isEmpty()) {
                    summary.incrementInvalid();
                    summary.addError("Missing station name for code: " + rawCode);
                    continue;
                }

                if (state == null || state.isEmpty()) {
                    state = "India";
                }
                if (city == null || city.isEmpty()) {
                    city = rawName;
                }

                if (processedCodesInBatch.contains(rawCode)) {
                    summary.incrementSkipped();
                    log.debug("Duplicate code in current batch skipped: {}", rawCode);
                    continue;
                }
                processedCodesInBatch.add(rawCode);

                // Upsert logic
                Optional<Station> existingOpt = stationRepository.findByStationCodeIgnoreCase(rawCode);
                if (existingOpt.isPresent()) {
                    Station existing = existingOpt.get();
                    existing.setStationName(rawName);
                    existing.setCity(city);
                    existing.setDistrict(district);
                    existing.setState(state);
                    existing.setRailwayZone(zone);
                    existing.setRailwayDivision(division);
                    existing.setAliases(aliases);
                    if (lat != null) existing.setLatitude(lat);
                    if (lon != null) existing.setLongitude(lon);
                    existing.setActive(active);
                    stationRepository.save(existing);
                    summary.incrementUpdated();
                } else {
                    Station newStation = new Station(
                            rawCode, rawName, city, district, state,
                            zone, division, aliases, lat, lon, active
                    );
                    stationRepository.save(newStation);
                    summary.incrementImported();
                }
            }

            log.info("Station import completed: Imported={}, Updated={}, Skipped={}, Invalid={}",
                    summary.getImported(), summary.getUpdated(), summary.getSkipped(), summary.getInvalid());

        } catch (Exception e) {
            log.error("Error processing station JSON stream", e);
            summary.addError("Processing error: " + e.getMessage());
        }

        return summary;
    }

    private Double parseDouble(Object val) {
        if (val == null) return null;
        try {
            return Double.valueOf(val.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
