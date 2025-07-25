package com.example.kickmatch.service;

import com.example.kickmatch.domain.Reservation;
import com.example.kickmatch.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public Reservation makeReservation(Reservation reservation) {
        LocalDateTime start = reservation.getReservedAt();

        LocalDateTime end = start.plusMinutes(reservation.getDurationMinutes());

        List<Reservation> conflicts = reservationRepository
                .findByLocationIdAndReservedAtBetween(
                        reservation.getLocation().getId(), start, end);

        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("해당 시간대에 이미 예약이 존재합니다.");
        }

        if (reservation.getStatus() == null) {
            reservation.setStatus("예약완료");
        }

        return   reservationRepository.save(reservation);
    }

    @Transactional
    public List<Reservation> getReservationByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }
}
