package com.railway.reservation;

import com.railway.reservation.dto.AvailabilityDto;
import com.railway.reservation.dto.FareDetailsDto;
import com.railway.reservation.dto.TrainDto;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.service.RailwayInfoService;
import com.railway.reservation.service.TrainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TrainSearchAndCodeTest {

    @Autowired
    private TrainService trainService;

    @Autowired
    private RailwayInfoService railwayInfoService;

    @Test
    @DisplayName("Train Search: Search using Station Codes 'SBC' to 'MAS'")
    void testSearchByStationCodes() {
        LocalDate date = LocalDate.now().plusDays(2);
        List<TrainDto> trains = trainService.searchTrains("SBC", "MAS", date);

        assertNotNull(trains);
        assertFalse(trains.isEmpty());
        assertTrue(trains.stream().anyMatch(t -> t.getTrainNumber().equals("20608") || t.getTrainNumber().equals("12028") || t.getTrainNumber().equals("12608")));
        
        TrainDto vandeBharat = trains.stream().filter(t -> t.getTrainNumber().equals("20608")).findFirst().orElse(null);
        if (vandeBharat != null) {
            assertEquals("SBC", vandeBharat.getSourceCode());
            assertEquals("MAS", vandeBharat.getDestinationCode());
            assertEquals("Vande Bharat", vandeBharat.getTrainType());
            assertNotNull(vandeBharat.getDepartureTime());
            assertNotNull(vandeBharat.getArrivalTime());
            assertNotNull(vandeBharat.getAvailableClasses());
        }
    }

    @Test
    @DisplayName("Train Search: Search using Station Names 'Bengaluru' to 'Chennai'")
    void testSearchByStationNames() {
        LocalDate date = LocalDate.now().plusDays(2);
        List<TrainDto> trains = trainService.searchTrains("Bengaluru", "Chennai", date);

        assertNotNull(trains);
        assertFalse(trains.isEmpty());
        assertTrue(trains.stream().anyMatch(t -> t.getSourceCode().equals("SBC") && t.getDestinationCode().equals("MAS")));
    }

    @Test
    @DisplayName("Train Search: Search using mixed autocomplete format 'Bengaluru City Junction (SBC)' to 'Chennai Central (MAS)'")
    void testSearchByAutocompleteFormat() {
        LocalDate date = LocalDate.now().plusDays(2);
        List<TrainDto> trains = trainService.searchTrains("Bengaluru City Junction (SBC)", "Chennai Central (MAS)", date);

        assertNotNull(trains);
        assertFalse(trains.isEmpty());
    }

    @Test
    @DisplayName("Train Search: No direct trains returns empty list")
    void testSearchNoDirectTrains() {
        LocalDate date = LocalDate.now().plusDays(2);
        List<TrainDto> trains = trainService.searchTrains("DWR", "ASR", date);

        assertNotNull(trains);
        assertTrue(trains.isEmpty());
    }

    @Test
    @DisplayName("Train Search: Same station codes rejected")
    void testSameStationCodesRejected() {
        assertThrows(BadRequestException.class, () ->
                trainService.searchTrains("SBC", "SBC", LocalDate.now().plusDays(1))
        );
    }

    @Test
    @DisplayName("Train Search: Past date rejected")
    void testPastDateRejected() {
        assertThrows(BadRequestException.class, () ->
                trainService.searchTrains("SBC", "MAS", LocalDate.now().minusDays(1))
        );
    }

    @Test
    @DisplayName("Live Fare: Accurate itemized telescopic fare calculation for Shatabdi 12028")
    void testFareBreakdownShatabdi() {
        FareDetailsDto fare = railwayInfoService.getTrainFare("12028", LocalDate.now().plusDays(1));

        assertNotNull(fare);
        assertEquals("12028", fare.getTrainNumber());
        assertEquals("SBC", fare.getSourceCode());
        assertEquals("MAS", fare.getDestinationCode());
        assertFalse(fare.getClassFares().isEmpty());

        FareDetailsDto.ClassFareBreakdown ccFare = fare.getClassFares().stream()
                .filter(f -> f.getTravelClass().equals("CC") || f.getClassName().contains("Chair"))
                .findFirst()
                .orElse(null);

        assertNotNull(ccFare);
        assertTrue(ccFare.getBaseFare() > 0);
        assertTrue(ccFare.getReservationCharge() > 0);
        assertTrue(ccFare.getTotalFare() >= ccFare.getBaseFare());
    }

    @Test
    @DisplayName("Live Availability: Quota-aware availability inquiry for Karnataka Express 12627")
    void testAvailabilityQuota() {
        AvailabilityDto gnAvail = railwayInfoService.getTrainAvailability("12627", LocalDate.now().plusDays(5), "3A", "GN");
        assertNotNull(gnAvail);
        assertEquals("12627", gnAvail.getTrainNumber());
        assertEquals("GN", gnAvail.getQuota());
        assertFalse(gnAvail.getClassAvailabilities().isEmpty());
    }
}
