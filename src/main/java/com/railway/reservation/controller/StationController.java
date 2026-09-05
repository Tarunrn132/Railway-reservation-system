package com.railway.reservation.controller;

import com.railway.reservation.dto.ApiResponse;
import com.railway.reservation.dto.StationDto;
import com.railway.reservation.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StationDto>>> getAllStations() {
        List<StationDto> stations = stationService.getAllActiveStations();
        return ResponseEntity.ok(ApiResponse.success("Stations retrieved successfully", stations));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StationDto>>> searchStations(@RequestParam(required = false, defaultValue = "") String q) {
        List<StationDto> stations = stationService.searchStations(q);
        return ResponseEntity.ok(ApiResponse.success("Matching stations found", stations));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<StationDto>> getStationByCode(@PathVariable String code) {
        StationDto station = stationService.getStationByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Station retrieved successfully", station));
    }
}
