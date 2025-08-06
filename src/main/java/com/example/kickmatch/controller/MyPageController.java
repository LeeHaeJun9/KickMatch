package com.example.kickmatch.controller;

import com.example.kickmatch.domain.Match;
import com.example.kickmatch.domain.Reservation;
import com.example.kickmatch.domain.User;
import com.example.kickmatch.service.MatchService;
import com.example.kickmatch.service.ReservationService;
import com.example.kickmatch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my")
public class MyPageController {

    private final MatchService matchService;
    private final UserService userService;
    private final ReservationService reservationService;

    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser) {

        User user = userService.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));


        List<Match> joinedMatches = matchService.findMatchesUserParticipated(user.getId());

        LocalDateTime now = LocalDateTime.now();

        List<Match> upcomingMatches = joinedMatches.stream()
                        .filter(match -> match.getMatchDate().isAfter(now))
                        .collect(Collectors.toList());

        List<Match> pastMatches = joinedMatches.stream()
                        .filter(match -> match.getMatchDate().isBefore(now))
                        .collect(Collectors.toList());

        List<Reservation> reservations = reservationService.findByUserId(user.getId());


//        model.addAttribute("joinedMatches", joinedMatches);
        model.addAttribute("user", user);
        model.addAttribute("upcomingMatches", upcomingMatches);
        model.addAttribute("pastMatches", pastMatches);
        model.addAttribute("reservations", reservations);

        return "my/mypage";
    }
}
