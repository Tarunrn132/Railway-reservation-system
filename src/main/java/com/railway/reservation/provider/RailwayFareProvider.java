package com.railway.reservation.provider;

import com.railway.reservation.dto.FareDetailsDto;
import com.railway.reservation.entity.Train;

import java.time.LocalDate;

public interface RailwayFareProvider {
    String getProviderName();
    boolean isLiveProvider();
    FareDetailsDto getTrainFare(String trainNumber, LocalDate date, Train trainMaster);
}
