package com.railway.reservation.repository;

import com.railway.reservation.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    Optional<Station> findByStationCodeIgnoreCase(String stationCode);

    List<Station> findByActiveTrueOrderByStationNameAsc();

    @Query("SELECT s FROM Station s WHERE s.active = true AND (" +
           "LOWER(s.stationCode) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.stationName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "(s.district IS NOT NULL AND LOWER(s.district) LIKE LOWER(CONCAT('%', :q, '%'))) OR " +
           "(s.aliases IS NOT NULL AND LOWER(s.aliases) LIKE LOWER(CONCAT('%', :q, '%'))) OR " +
           "LOWER(s.state) LIKE LOWER(CONCAT('%', :q, '%')))" +
           " ORDER BY CASE WHEN LOWER(s.stationCode) = LOWER(:q) THEN 1 " +
           "               WHEN LOWER(s.stationCode) LIKE LOWER(CONCAT(:q, '%')) THEN 2 " +
           "               WHEN LOWER(s.stationName) = LOWER(:q) THEN 3 " +
           "               WHEN LOWER(s.stationName) LIKE LOWER(CONCAT(:q, '%')) THEN 4 " +
           "               WHEN LOWER(s.city) = LOWER(:q) THEN 5 " +
           "               WHEN LOWER(s.city) LIKE LOWER(CONCAT(:q, '%')) THEN 6 " +
           "               WHEN s.aliases IS NOT NULL AND LOWER(s.aliases) LIKE LOWER(CONCAT('%', :q, '%')) THEN 7 " +
           "               ELSE 8 END, s.stationName ASC")
    List<Station> searchStations(@Param("q") String query);

    @Query("SELECT s FROM Station s WHERE s.active = true AND (" +
           "LOWER(s.stationCode) = LOWER(:codeOrCity) OR " +
           "LOWER(s.city) = LOWER(:codeOrCity) OR " +
           "LOWER(s.stationName) = LOWER(:codeOrCity) OR " +
           "(s.aliases IS NOT NULL AND LOWER(s.aliases) LIKE LOWER(CONCAT('%', :codeOrCity, '%'))))")
    List<Station> findByCodeOrCityOrName(@Param("codeOrCity") String codeOrCity);
}
