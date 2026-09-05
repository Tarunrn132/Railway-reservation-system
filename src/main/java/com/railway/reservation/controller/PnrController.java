package com.railway.reservation.controller;

import com.railway.reservation.dto.ApiResponse;
import com.railway.reservation.dto.PnrStatusDto;
import com.railway.reservation.service.RailwayInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pnr")
@CrossOrigin(origins = "*")
public class PnrController {

    private final RailwayInfoService railwayInfoService;

    public PnrController(RailwayInfoService railwayInfoService) {
        this.railwayInfoService = railwayInfoService;
    }

    @GetMapping("/{pnr}")
    public ResponseEntity<ApiResponse<PnrStatusDto>> getOfficialPnrStatus(@PathVariable String pnr) {
        PnrStatusDto status = railwayInfoService.getPnrStatus(pnr);
        return ResponseEntity.ok(ApiResponse.success("PNR status retrieved", status));
    }
}
