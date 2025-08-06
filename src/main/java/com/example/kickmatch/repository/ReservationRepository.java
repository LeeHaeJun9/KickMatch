package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Location;
import com.example.kickmatch.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);

    // 특정 구장과 시간대의 중복 예약 체크용
    List<Reservation> findByLocationIdAndReservedAtBetween(Long locationId,
                                                           LocalDateTime start, LocalDateTime end);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reservation r " +
            "WHERE r.location = :location " +
            "AND r.reservedAt < :reservedEnd " +
            "AND r.endTime > :reservedAt")
    boolean existsOverlapReservation(
            @Param("location") Location location,
            @Param("reservedAt") LocalDateTime reservedAt,
            @Param("reservedEnd") LocalDateTime reservedEnd);
}
