package com.example.kickmatch.controller;

import com.example.kickmatch.domain.Match;
import com.example.kickmatch.domain.User;
import com.example.kickmatch.service.MatchService;
import com.example.kickmatch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my")
public class MyPageController {

    private final MatchService matchService;
    private final UserService userService;

    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser) {
        System.out.println("🌟 springUser username: " + springUser.getUsername());

        User user = userService.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));


        List<Match> joinedMatches = matchService.findMatchesUserParticipated(user.getId());


        model.addAttribute("joinedMatches", joinedMatches);
        model.addAttribute("user", user);
        return "my/mypage";
    }
}
