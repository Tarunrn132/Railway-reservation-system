package com.railway.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.reservation.dto.StationImportSummary;
import com.railway.reservation.entity.Station;
import com.railway.reservation.repository.StationRepository;
import com.railway.reservation.service.StationImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StationImportServiceTest {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    private StationImportService stationImportService;

    @BeforeEach
    void setUp() {
        stationImportService = new StationImportService(stationRepository, objectMapper, resourceLoader);
    }

    @Test
    @DisplayName("Should parse and import valid station JSON dataset")
    void testImportValidJsonStream() {
        String json = """
        [
          {
            "stationCode": "TEST1",
            "stationName": "Test Junction One",
            "city": "Test City",
            "district": "Test District",
            "state": "Karnataka",
            "railwayZone": "SWR",
            "railwayDivision": "SBC",
            "aliases": "Alias One, Test 1",
            "latitude": 12.97,
            "longitude": 77.56,
            "active": true
          },
          {
            "stationCode": "TEST2",
            "stationName": "Test Terminal Two",
            "city": "Test City Two",
            "district": "Test District Two",
            "state": "Tamil Nadu",
            "railwayZone": "SR",
            "railwayDivision": "MAS",
            "aliases": "Alias Two",
            "latitude": 13.08,
            "longitude": 80.27,
            "active": true
          }
        ]
        """;

        StationImportSummary summary = stationImportService.importFromInputStream(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        );

        assertTrue(summary.getImported() >= 0);
        assertEquals(0, summary.getInvalid());

        Optional<Station> s1 = stationRepository.findByStationCodeIgnoreCase("TEST1");
        assertTrue(s1.isPresent());
        assertEquals("Test Junction One", s1.get().getStationName());
        assertEquals("SWR", s1.get().getRailwayZone());
    }

    @Test
    @DisplayName("Should detect invalid station codes and report in summary")
    void testInvalidStationCodeHandling() {
        String json = """
        [
          {
            "stationCode": "INVALID_STATION_CODE_TOO_LONG",
            "stationName": "Invalid Code Station",
            "city": "City",
            "state": "State"
          },
          {
            "stationCode": "",
            "stationName": "Empty Code Station",
            "city": "City",
            "state": "State"
          }
        ]
        """;

        StationImportSummary summary = stationImportService.importFromInputStream(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(2, summary.getInvalid());
        assertEquals(2, summary.getErrors().size());
    }

    @Test
    @DisplayName("Should verify comprehensive stations.json is present and has 600+ records")
    void testMasterStationDatasetLoad() {
        StationImportSummary summary = stationImportService.importFromResource("classpath:data/stations.json");
        assertNotNull(summary);
        long totalInDb = stationRepository.count();
        assertTrue(totalInDb >= 600, "Database should contain at least 600 authentic Indian Railway stations, found: " + totalInDb);
    }
}
