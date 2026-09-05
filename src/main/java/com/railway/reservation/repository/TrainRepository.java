package com.railway.reservation.repository;

import com.railway.reservation.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByTrainNumber(String trainNumber);

    @Query("SELECT t FROM Train t WHERE t.active = true AND (" +
           "(LOWER(TRIM(t.source)) = LOWER(TRIM(:source)) OR LOWER(TRIM(t.sourceCode)) = LOWER(TRIM(:source))) AND " +
           "(LOWER(TRIM(t.destination)) = LOWER(TRIM(:destination)) OR LOWER(TRIM(t.destinationCode)) = LOWER(TRIM(:destination))))")
    List<Train> searchTrains(@Param("source") String source, @Param("destination") String destination);

    @Query("SELECT DISTINCT t.sourceCode FROM Train t WHERE t.sourceCode IS NOT NULL")
    List<String> findDistinctSourceCodes();

    @Query("SELECT DISTINCT t.destinationCode FROM Train t WHERE t.destinationCode IS NOT NULL")
    List<String> findDistinctDestinationCodes();

    @Query("SELECT DISTINCT t.source FROM Train t ORDER BY t.source ASC")
    List<String> findDistinctSources();

    @Query("SELECT DISTINCT t.destination FROM Train t ORDER BY t.destination ASC")
    List<String> findDistinctDestinations();
}
