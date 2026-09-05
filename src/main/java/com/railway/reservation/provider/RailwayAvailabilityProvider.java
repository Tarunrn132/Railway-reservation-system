package com.railway.reservation.provider;

import com.railway.reservation.dto.AvailabilityDto;
import com.railway.reservation.entity.Train;

import java.time.LocalDate;

public interface RailwayAvailabilityProvider {
    String getProviderName();
    boolean isLiveProvider();
    AvailabilityDto getTrainAvailability(String trainNumber, LocalDate date, String travelClass, String quota, Train trainMaster);
}
