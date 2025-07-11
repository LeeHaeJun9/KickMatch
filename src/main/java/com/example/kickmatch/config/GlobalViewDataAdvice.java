package com.example.kickmatch.config;

import com.example.kickmatch.domain.User;
import com.example.kickmatch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalViewDataAdvice {

    private final UserService userService;

    @ModelAttribute
    public void addUserNicknameToModel(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            String username = userDetails.getUsername();
            Optional<User> user = userService.findByUsername(username);
            user.ifPresent(u -> model.addAttribute("nickname", u.getNickname()));
        }
    }
}
