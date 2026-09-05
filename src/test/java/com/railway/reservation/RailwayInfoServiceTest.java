package com.railway.reservation;

import com.railway.reservation.dto.AvailabilityDto;
import com.railway.reservation.dto.FareDetailsDto;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.service.RailwayInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RailwayInfoServiceTest {

    @Autowired
    private RailwayInfoService railwayInfoService;

    @BeforeEach
    void setUp() {
        railwayInfoService.clearCache();
        railwayInfoService.setApiConfiguration("", "", "mock");
    }

    @Test
    @DisplayName("Seat Availability: Retrieve authentic capacity for Vande Bharat 20607")
    void testGetAvailabilityVandeBharat() {
        AvailabilityDto avail = railwayInfoService.getTrainAvailability("20607", LocalDate.now(), "CC", "GN");

        assertNotNull(avail);
        assertEquals("20607", avail.getTrainNumber());
        assertFalse(avail.getClassAvailabilities().isEmpty());
        assertTrue(avail.getClassAvailabilities().get(0).getAvailableSeats() >= 0);
        assertNotNull(avail.getDataSource());
        assertTrue(avail.getDataSource().contains("Internal Capacity") || avail.getDataSource().contains("LOCAL_TIMETABLE"));
    }

    @Test
    @DisplayName("Fare Calculation: Retrieve itemized fare for Shatabdi 12028")
    void testGetFareShatabdi() {
        FareDetailsDto fare = railwayInfoService.getTrainFare("12028", LocalDate.now());

        assertNotNull(fare);
        assertEquals("12028", fare.getTrainNumber());
        assertFalse(fare.getClassFares().isEmpty());
        assertTrue(fare.getClassFares().get(0).getBaseFare() > 0);
        assertTrue(fare.getClassFares().get(0).getTotalFare() >= fare.getClassFares().get(0).getBaseFare());
    }

    @Test
    @DisplayName("Availability Caching: Consecutive calls return cached results")
    void testAvailabilityCaching() {
        LocalDate date = LocalDate.now();
        AvailabilityDto first = railwayInfoService.getTrainAvailability("12951", date, "3A", "GN");
        AvailabilityDto second = railwayInfoService.getTrainAvailability("12951", date, "3A", "GN");

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.getTrainNumber(), second.getTrainNumber());
        assertEquals(first.getClassAvailabilities().size(), second.getClassAvailabilities().size());
    }

    @Test
    @DisplayName("Invalid Train: Throws ResourceNotFoundException for non-existent train number")
    void testInvalidTrainNumberFails() {
        assertThrows(ResourceNotFoundException.class, () ->
                railwayInfoService.getTrainAvailability("99999_INVALID", LocalDate.now(), "SL", "GN")
        );
    }

    @Test
    @DisplayName("Empty Train: Throws ResourceNotFoundException for empty string")
    void testEmptyTrainNumberFails() {
        assertThrows(ResourceNotFoundException.class, () ->
                railwayInfoService.getTrainAvailability("   ", LocalDate.now(), "SL", "GN")
        );
    }
}
