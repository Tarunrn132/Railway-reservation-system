package com.railway.reservation;

import com.railway.reservation.dto.AvailabilityDto;
import com.railway.reservation.dto.FareDetailsDto;
import com.railway.reservation.dto.PnrStatusDto;
import com.railway.reservation.service.RailwayInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PnrAndAvailabilityTest {

    @Autowired
    private RailwayInfoService railwayInfoService;

    @Test
    @DisplayName("Seat Availability: Retrieve multi-class availability for Vande Bharat 20607")
    void testGetAvailabilityVandeBharat() {
        AvailabilityDto dto = railwayInfoService.getTrainAvailability("20607", LocalDate.now().plusDays(1), "CC", "GN");

        assertNotNull(dto);
        assertEquals("20607", dto.getTrainNumber());
        assertEquals("MAS", dto.getSourceCode());
        assertEquals("SBC", dto.getDestinationCode());
        assertFalse(dto.getClassAvailabilities().isEmpty());
        assertFalse(dto.isLiveDataAvailable(), "Master fallback should state live feed is unconnected");
    }

    @Test
    @DisplayName("Fare Breakdown: Retrieve telescopic fare breakdown for Shatabdi 12028")
    void testGetFareShatabdi() {
        FareDetailsDto dto = railwayInfoService.getTrainFare("12028", LocalDate.now().plusDays(2));

        assertNotNull(dto);
        assertEquals("12028", dto.getTrainNumber());
        assertFalse(dto.getClassFares().isEmpty());

        FareDetailsDto.ClassFareBreakdown firstClass = dto.getClassFares().get(0);
        assertTrue(firstClass.getTotalFare() > 0);
        assertTrue(firstClass.getBaseFare() > 0);
    }

    @Test
    @DisplayName("Official PNR Status: Query official PNR with clear external dependency message")
    void testOfficialPnrInquiry() {
        PnrStatusDto dto = railwayInfoService.getPnrStatus("1234567890");

        assertNotNull(dto);
        assertEquals("1234567890", dto.getPnr());
        assertFalse(dto.isLiveDataAvailable());
        assertTrue(dto.getErrorMessage().contains("Official Indian Railways PNR lookup requires"));
    }
}
