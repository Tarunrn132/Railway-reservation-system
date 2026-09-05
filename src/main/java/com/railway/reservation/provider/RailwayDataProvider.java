package com.railway.reservation.provider;

import com.railway.reservation.dto.AvailabilityDto;
import com.railway.reservation.dto.FareDetailsDto;
import com.railway.reservation.dto.PnrStatusDto;
import com.railway.reservation.dto.TrainDto;
import com.railway.reservation.entity.Train;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstraction layer for Indian Railway data providers.
 * Supports train search, real-time availability, fare breakdowns, and official PNR status.
 */
public interface RailwayDataProvider extends RailwayAvailabilityProvider, RailwayFareProvider, RailwayPnrProvider {

    @Override
    String getProviderName();

    @Override
    boolean isLiveProvider();

    List<TrainDto> searchTrains(String source, String destination, LocalDate date);

    @Override
    AvailabilityDto getTrainAvailability(String trainNumber, LocalDate date, String travelClass, String quota, Train trainMaster);

    @Override
    FareDetailsDto getTrainFare(String trainNumber, LocalDate date, Train trainMaster);

    @Override
    PnrStatusDto getPnrStatus(String pnr);
}
