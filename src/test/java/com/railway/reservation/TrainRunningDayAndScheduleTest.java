package com.railway.reservation;

import com.railway.reservation.dto.TrainDto;
import com.railway.reservation.dto.TrainScheduleDto;
import com.railway.reservation.service.TrainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TrainRunningDayAndScheduleTest {

    @Autowired
    private TrainService trainService;

    @Test
    @DisplayName("Should validate running days correctly against journey date")
    void testRunningDaysFilter() {
        // Find a Tuesday (e.g. 2026-09-08 is Tuesday)
        LocalDate tuesday = LocalDate.of(2026, 9, 8);
        // 2026-09-09 is Wednesday
        LocalDate wednesday = LocalDate.of(2026, 9, 9);

        // Train 20607 runs 'Except Tuesday'
        boolean runsOnTue = trainService.isTrainRunningOnDate("Except Tuesday", tuesday);
        assertFalse(runsOnTue, "Train with 'Except Tuesday' should not run on Tuesday");

        boolean runsOnWed = trainService.isTrainRunningOnDate("Except Tuesday", wednesday);
        assertTrue(runsOnWed, "Train with 'Except Tuesday' should run on Wednesday");

        // Daily train
        assertTrue(trainService.isTrainRunningOnDate("Daily", tuesday));
        assertTrue(trainService.isTrainRunningOnDate("Daily", wednesday));

        // Train with specific days e.g. "Tue, Thu, Fri"
        assertTrue(trainService.isTrainRunningOnDate("Tue, Thu, Fri", tuesday));
        assertFalse(trainService.isTrainRunningOnDate("Tue, Thu, Fri", wednesday));
    }

    @Test
    @DisplayName("Should retrieve train schedule with intermediate stops")
    void testGetTrainSchedule() {
        TrainScheduleDto schedule = trainService.getTrainSchedule("20607");
        assertNotNull(schedule);
        assertEquals("20607", schedule.getTrainNumber());
        assertEquals("MAS", schedule.getSourceCode());
        assertEquals("SBC", schedule.getDestinationCode());
        assertNotNull(schedule.getRouteStops());
        assertTrue(schedule.getRouteStops().size() >= 2);

        // First stop is source, last is destination
        assertEquals("MAS", schedule.getRouteStops().get(0).getStationCode());
        assertEquals("SBC", schedule.getRouteStops().get(schedule.getRouteStops().size() - 1).getStationCode());
    }

    @Test
    @DisplayName("Should lookup train by train number")
    void testGetTrainByNumber() {
        TrainDto train = trainService.getTrainDtoByNumber("12627", LocalDate.now());
        assertNotNull(train);
        assertEquals("12627", train.getTrainNumber());
        assertTrue(train.getTrainName().contains("Karnataka Express"));
    }
}
