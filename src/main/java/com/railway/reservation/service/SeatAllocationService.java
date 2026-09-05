package com.railway.reservation.service;

import com.railway.reservation.exception.BookingConflictException;
import com.railway.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SeatAllocationService {

    private final ReservationRepository reservationRepository;
    private final SecureRandom random = new SecureRandom();
    private static final String PNR_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    public SeatAllocationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public synchronized List<String> allocateSeats(Long trainId, LocalDate journeyDate, String travelClass, int totalSeats, int count) {
        List<String> occupiedSeatsList = reservationRepository.findOccupiedSeats(trainId, journeyDate);
        Set<String> occupiedSeats = new HashSet<>();
        for (String seatStr : occupiedSeatsList) {
            if (seatStr != null) {
                for (String s : seatStr.split(",")) {
                    occupiedSeats.add(s.trim());
                }
            }
        }

        if (occupiedSeats.size() + count > totalSeats) {
            throw new BookingConflictException("Not enough seats available for " + count + " passengers on " + journeyDate);
        }

        String classLetter = getCoachLetter(travelClass);
        int coachCapacity = getCoachCapacity(travelClass);
        int maxCoaches = (int) Math.ceil((double) totalSeats / coachCapacity);
        if (maxCoaches < 1) maxCoaches = 1;

        List<String> allocated = new ArrayList<>();
        int seatsChecked = 0;
        for (int coachNum = 1; coachNum <= maxCoaches && seatsChecked < totalSeats && allocated.size() < count; coachNum++) {
            for (int seatNum = 1; seatNum <= coachCapacity && seatsChecked < totalSeats && allocated.size() < count; seatNum++) {
                seatsChecked++;
                String seatCandidate = String.format("%s%d-%02d", classLetter, coachNum, seatNum);
                if (!occupiedSeats.contains(seatCandidate)) {
                    allocated.add(seatCandidate);
                    occupiedSeats.add(seatCandidate);
                }
            }
        }

        if (allocated.size() < count) {
            throw new BookingConflictException("Could not allocate " + count + " seats in class '" + travelClass + "' for train #" + trainId + " on " + journeyDate);
        }

        return allocated;
    }

    public synchronized String allocateSeat(Long trainId, LocalDate journeyDate, String travelClass, int totalSeats) {
        return allocateSeats(trainId, journeyDate, travelClass, totalSeats, 1).get(0);
    }

    public String generateUniquePnr() {
        String pnr;
        int attempts = 0;
        do {
            StringBuilder sb = new StringBuilder("PNR");
            for (int i = 0; i < 7; i++) {
                int index = random.nextInt(PNR_CHARS.length());
                sb.append(PNR_CHARS.charAt(index));
            }
            pnr = sb.toString();
            attempts++;
            if (attempts > 100) {
                pnr = "PNR" + System.currentTimeMillis();
                break;
            }
        } while (reservationRepository.existsByPnr(pnr));
        return pnr;
    }

    private String getCoachLetter(String travelClass) {
        if (travelClass == null) return "S";
        String normalized = travelClass.trim().toUpperCase();
        if (normalized.contains("1A") || normalized.contains("FIRST") || normalized.contains("1 TIER")) {
            return "H";
        } else if (normalized.contains("2A") || normalized.contains("2 TIER")) {
            return "A";
        } else if (normalized.contains("3A") || normalized.contains("3 TIER")) {
            return "B";
        } else if (normalized.contains("CC") || normalized.contains("CHAIR")) {
            return "C";
        } else if (normalized.contains("2S") || normalized.contains("SECOND")) {
            return "D";
        } else {
            return "S"; // Default Sleeper
        }
    }

    private int getCoachCapacity(String travelClass) {
        if (travelClass == null) return 72;
        String normalized = travelClass.trim().toUpperCase();
        if (normalized.contains("1A") || normalized.contains("FIRST") || normalized.contains("1 TIER")) {
            return 24;
        } else if (normalized.contains("2A") || normalized.contains("2 TIER")) {
            return 48;
        } else if (normalized.contains("3A") || normalized.contains("3 TIER")) {
            return 64;
        } else if (normalized.contains("CC") || normalized.contains("CHAIR")) {
            return 72;
        } else if (normalized.contains("2S") || normalized.contains("SECOND")) {
            return 80;
        } else {
            return 72;
        }
    }
}
