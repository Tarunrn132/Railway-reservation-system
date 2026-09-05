package com.railway.reservation.service;

import com.railway.reservation.dto.StationDto;
import com.railway.reservation.entity.Station;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.repository.StationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StationService {

    private static final Logger log = LoggerFactory.getLogger(StationService.class);

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<StationDto> searchStations(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveStations();
        }
        String cleanQuery = query.trim();
        List<Station> stations = stationRepository.searchStations(cleanQuery);
        return stations.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public StationDto getStationByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BadRequestException("Station code cannot be empty");
        }
        Station station = stationRepository.findByStationCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with code: " + code.trim().toUpperCase()));
        return mapToDto(station);
    }

    public List<StationDto> getAllActiveStations() {
        return stationRepository.findByActiveTrueOrderByStationNameAsc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public String resolveStationCode(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        String clean = input.trim();

        // If input contains parentheses like "Bengaluru City Junction (SBC)", extract SBC
        if (clean.contains("(") && clean.contains(")")) {
            int start = clean.lastIndexOf('(');
            int end = clean.lastIndexOf(')');
            if (end > start) {
                String candidate = clean.substring(start + 1, end).trim();
                Optional<Station> station = stationRepository.findByStationCodeIgnoreCase(candidate);
                if (station.isPresent()) {
                    return station.get().getStationCode();
                }
            }
        }

        // Check if direct station code match
        Optional<Station> directCode = stationRepository.findByStationCodeIgnoreCase(clean);
        if (directCode.isPresent()) {
            return directCode.get().getStationCode();
        }

        // Check by city or name
        List<Station> byCityOrName = stationRepository.findByCodeOrCityOrName(clean);
        if (!byCityOrName.isEmpty()) {
            return byCityOrName.get(0).getStationCode();
        }

        // Search match
        List<Station> searchResults = stationRepository.searchStations(clean);
        if (!searchResults.isEmpty()) {
            return searchResults.get(0).getStationCode();
        }

        return clean.toUpperCase();
    }

    public StationDto mapToDto(Station station) {
        return new StationDto(
                station.getId(),
                station.getStationCode(),
                station.getStationName(),
                station.getCity(),
                station.getDistrict(),
                station.getState(),
                station.getRailwayZone(),
                station.getRailwayDivision(),
                station.getAliases(),
                station.getLatitude(),
                station.getLongitude(),
                station.getActive()
        );
    }
}
