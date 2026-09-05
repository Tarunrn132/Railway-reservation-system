package com.railway.reservation;

import com.railway.reservation.dto.StationDto;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.service.StationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class StationServiceTest {

    @Autowired
    private StationService stationService;

    @Test
    @DisplayName("Station Search: Search by station name 'Bengaluru'")
    void testSearchByName() {
        List<StationDto> results = stationService.searchStations("Bengaluru");
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(s -> s.getStationCode().equals("SBC")));
    }

    @Test
    @DisplayName("Station Search: Search by station code 'MAS'")
    void testSearchByCode() {
        List<StationDto> results = stationService.searchStations("MAS");
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("MAS", results.get(0).getStationCode());
        assertEquals("Chennai", results.get(0).getCity());
    }

    @Test
    @DisplayName("Station Search: Search by city 'Delhi'")
    void testSearchByCity() {
        List<StationDto> results = stationService.searchStations("Delhi");
        assertNotNull(results);
        assertTrue(results.size() >= 3);
        assertTrue(results.stream().anyMatch(s -> s.getStationCode().equals("NDLS")));
    }

    @Test
    @DisplayName("Station Autocomplete: Prefix match 'Chen' suggests Chennai stations")
    void testAutocompletePrefix() {
        List<StationDto> results = stationService.searchStations("Chen");
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(s -> s.getStationCode().equals("MAS") || s.getStationCode().equals("MS")));
    }

    @Test
    @DisplayName("Station Resolution: Resolve station codes from mixed input formats")
    void testResolveStationCode() {
        assertEquals("SBC", stationService.resolveStationCode("Bengaluru City Junction (SBC)"));
        assertEquals("MAS", stationService.resolveStationCode("MAS"));
        assertEquals("NDLS", stationService.resolveStationCode("New Delhi"));
        assertEquals("MMCT", stationService.resolveStationCode("Mumbai Central"));
    }

    @Test
    @DisplayName("Get Station By Code: Valid station code 'SBC'")
    void testGetStationByCodeSuccess() {
        StationDto station = stationService.getStationByCode("SBC");
        assertNotNull(station);
        assertEquals("SBC", station.getStationCode());
        assertEquals("Bengaluru", station.getCity());
        assertEquals("Karnataka", station.getState());
        assertNotNull(station.getLatitude());
        assertNotNull(station.getLongitude());
    }

    @Test
    @DisplayName("Get Station By Code: Invalid station code throws ResourceNotFoundException")
    void testGetStationByCodeNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> stationService.getStationByCode("INVALID999"));
    }

    @Test
    @DisplayName("Get Station By Code: Empty station code throws BadRequestException")
    void testGetStationByCodeEmpty() {
        assertThrows(BadRequestException.class, () -> stationService.getStationByCode("   "));
    }

    @Test
    @DisplayName("Station Search: Non-existent query returns empty list")
    void testNonExistentStationSearch() {
        List<StationDto> results = stationService.searchStations("ZZZZ_NONEXISTENT_STATION");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
