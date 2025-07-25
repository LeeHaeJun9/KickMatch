package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);

    // 특정 구장과 시간대의 중복 예약 체크용
    List<Reservation> findByLocationIdAndReservedAtBetween(Long locationId,
                                                           LocalDateTime start, LocalDateTime end);
}
