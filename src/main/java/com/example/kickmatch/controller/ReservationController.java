package com.example.kickmatch.controller;


import com.example.kickmatch.domain.Location;
import com.example.kickmatch.domain.Reservation;
import com.example.kickmatch.domain.User;
import com.example.kickmatch.repository.LocationRepository;
import com.example.kickmatch.repository.ReservationRepository;
import com.example.kickmatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ReservationRepository reservationRepository;

    @PostMapping("/create")
    public String createReservation(@RequestParam Long locationId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reservedAt,
                                    @RequestParam int durationMinutes,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        // 사용자 정보 가져오기
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 구장 정보 가져오기
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("구장 없음"));

        // 예약 생성
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setLocation(location);
        reservation.setReservedAt(reservedAt);
        reservation.setDurationMinutes(durationMinutes);
        reservation.setStatus("완료");

        reservationRepository.save(reservation);

        redirectAttributes.addFlashAttribute("message", "예약이 완료되었습니다!");
        return "redirect:/location/" + locationId;
    }

}
