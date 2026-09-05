package com.railway.reservation.controller;

import com.railway.reservation.dto.ApiResponse;
import com.railway.reservation.dto.AvailabilityDto;
import com.railway.reservation.dto.FareDetailsDto;
import com.railway.reservation.dto.TrainDto;
import com.railway.reservation.service.RailwayInfoService;
import com.railway.reservation.service.TrainService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trains")
@CrossOrigin(origins = "*")
public class TrainController {

    private final TrainService trainService;
    private final RailwayInfoService railwayInfoService;

    public TrainController(TrainService trainService, RailwayInfoService railwayInfoService) {
        this.trainService = trainService;
        this.railwayInfoService = railwayInfoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainDto>>> getAllTrains(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TrainDto> trains = trainService.getAllTrains(date);
        return ResponseEntity.ok(ApiResponse.success("Trains retrieved successfully", trains));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TrainDto>>> searchTrains(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String travelClass,
            @RequestParam(required = false, defaultValue = "GN") String quota) {
        List<TrainDto> results = trainService.searchTrains(source, destination, date);
        return ResponseEntity.ok(ApiResponse.success("Trains found", results));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainDto>> getTrainById(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TrainDto train = trainService.getTrainDtoById(id, date);
        return ResponseEntity.ok(ApiResponse.success("Train retrieved successfully", train));
    }

    @GetMapping("/stations")
    public ResponseEntity<ApiResponse<List<String>>> getStations() {
        List<String> stations = trainService.getAllStations();
        return ResponseEntity.ok(ApiResponse.success("Stations retrieved successfully", stations));
    }

    @GetMapping("/{trainNumber}/availability")
    public ResponseEntity<ApiResponse<AvailabilityDto>> getTrainAvailability(
            @PathVariable String trainNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "ALL") String travelClass,
            @RequestParam(required = false, defaultValue = "GN") String quota) {
        AvailabilityDto availability = railwayInfoService.getTrainAvailability(trainNumber, date, travelClass, quota);
        return ResponseEntity.ok(ApiResponse.success("Train seat availability retrieved", availability));
    }

    @GetMapping("/by-number/{trainNumber}")
    public ResponseEntity<ApiResponse<TrainDto>> getTrainByNumber(
            @PathVariable String trainNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TrainDto train = trainService.getTrainDtoByNumber(trainNumber, date);
        return ResponseEntity.ok(ApiResponse.success("Train retrieved successfully", train));
    }

    @GetMapping("/{trainNumber}/schedule")
    public ResponseEntity<ApiResponse<com.railway.reservation.dto.TrainScheduleDto>> getTrainSchedule(
            @PathVariable String trainNumber) {
        com.railway.reservation.dto.TrainScheduleDto schedule = trainService.getTrainSchedule(trainNumber);
        return ResponseEntity.ok(ApiResponse.success("Train route schedule retrieved", schedule));
    }

    @GetMapping("/{trainNumber}/fare")
    public ResponseEntity<ApiResponse<FareDetailsDto>> getTrainFare(
            @PathVariable String trainNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        FareDetailsDto fare = railwayInfoService.getTrainFare(trainNumber, date);
        return ResponseEntity.ok(ApiResponse.success("Train fare details retrieved", fare));
    }
}
