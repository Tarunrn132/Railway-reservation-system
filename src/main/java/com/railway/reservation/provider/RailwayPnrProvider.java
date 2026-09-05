package com.railway.reservation.provider;

import com.railway.reservation.dto.PnrStatusDto;

public interface RailwayPnrProvider {
    String getProviderName();
    boolean isLiveProvider();
    PnrStatusDto getPnrStatus(String pnr);
}
