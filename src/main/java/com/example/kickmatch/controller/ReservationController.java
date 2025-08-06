package com.example.kickmatch.controller;


import com.example.kickmatch.domain.Location;
import com.example.kickmatch.domain.Reservation;
import com.example.kickmatch.domain.ReservationStatus;
import com.example.kickmatch.domain.User;
import com.example.kickmatch.repository.LocationRepository;
import com.example.kickmatch.repository.ReservationRepository;
import com.example.kickmatch.repository.UserRepository;
import com.example.kickmatch.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    @PostMapping("/create")
    public String createReservation(@RequestParam Long locationId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reservedAt,
                                    @RequestParam int durationMinutes,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {

        // 사용자, 구장 조회는 기존과 동일
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("구장 없음"));

        // 예약 시간 범위 계산
        LocalDateTime reservedEnd = reservedAt.plusMinutes(durationMinutes);

        List<Reservation> existingReservations = reservationRepository.findByLocationIdAndReservedAtBetween(
                locationId, reservedAt.minusHours(4), reservedAt.plusHours(4) // 충분히 넓은 범위
        );

        // 중복 예약 체크: DB에 겹치는 예약이 있는지 조회
        boolean overlap = existingReservations.stream().anyMatch(r -> {
            LocalDateTime rEnd = r.getReservedAt().plusMinutes(r.getDurationMinutes());
            LocalDateTime newEnd = reservedAt.plusMinutes(durationMinutes);
            return reservedAt.isBefore(rEnd) && newEnd.isAfter(r.getReservedAt());
        });

        if (overlap) {
            redirectAttributes.addFlashAttribute("error", "해당 시간대에 이미 예약이 있습니다.");
            return "redirect:/location/" + locationId;
        }

        // 중복 없으면 저장
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setLocation(location);
        reservation.setReservedAt(reservedAt);
        reservation.setDurationMinutes(durationMinutes);
        reservation.setStatus(ReservationStatus.PENDING);

        reservationRepository.save(reservation);

        redirectAttributes.addFlashAttribute("message", "예약이 완료되었습니다!");
        return "redirect:/location/" + locationId;
    }

    @GetMapping("/{id}")
    public String viewReservation(@PathVariable Long id, Model model) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 예약을 찾을 수 없습니다."));
        model.addAttribute("reservation", reservation);
        return "reservation/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, Principal principal) {
        Reservation reservation = reservationService.findById(id);

        // 로그인 사용자 본인 확인
        if (!reservation.getUser().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 예약만 취소할 수 있습니다.");
        }

        // 상태 변경
        reservation.setStatus(ReservationStatus.CANCELED);
        reservationService.save(reservation);

        return "redirect:/reservation/" + id;
    }


}
