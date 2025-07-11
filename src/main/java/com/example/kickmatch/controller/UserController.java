package com.example.kickmatch.controller;

import com.example.kickmatch.domain.User;
import com.example.kickmatch.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 폼
    @GetMapping("/register")
    public String showRegisterForm() {
        return "users/register";  // src/main/resources/templates/users/register.html
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                               @RequestParam("requestedRole") String requestedRole,
                               Model model) {
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("errorMessage", "이미 사용 중인 아이디입니다.");
            return "users/register";
        }

        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("errorMessage", "이미 사용 중인 이메일입니다.");
            return "users/register";
        }

        if (userService.existsByNickname(user.getNickname())) {
            model.addAttribute("errorMessage", "이미 사용 중이 닉네임입니다.");
            return "users/register";
        }

        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 선택한 역할에 따라 분기
        if ("ROLE_PENDING_ADMIN".equals(requestedRole)) {
            user.setRole("ROLE_PENDING_ADMIN");
        } else {
            user.setRole("ROLE_USER");
        }

        userService.save(user);
        return "redirect:/users/login";
    }

    // 로그인 폼
    @GetMapping("/login")
    public String showLoginForm() {
        return "users/login";  // 로그인 페이지 템플릿
    }

    // 마이페이지
    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername(); // 현재 로그인된 사용자 이름

        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "redirect:/users/login";
        }

        model.addAttribute("user", userOpt.get());

        // (예약 정보까지 넘기고 싶으면 여기에 추가)
        // model.addAttribute("reservations", reservationService.findByUser(userOpt.get()));

        return "users/mypage";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        request.logout(); // 로그아웃 처리
        return "redirect:/"; // 홈으로 리다이렉트
    }

    @GetMapping("/edit")
    public String showEditForm(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        model.addAttribute("user", user);
        return "users/edit"; // 위 Thymeleaf 템플릿 경로
    }

    // 회원정보 수정 처리 POST
    @PostMapping("/edit")
    public String updateUser(@RequestParam String nickname,
                             @RequestParam String email,
                             @RequestParam(required = false) String password,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        String username = principal.getName();

        // ✅ 닉네임 중복 체크 추가
        if (userService.isNicknameDuplicate(nickname, username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 사용 중인 닉네임입니다.");
            return "redirect:/users/edit";
        }

        try {
            userService.updateUserInfo(username, nickname, email, password);
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원 정보 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/users/edit";
        }

        return "redirect:/users/mypage";
    }

    @PostMapping("/delete")
    public String deleteUser(Principal principal, HttpServletRequest request) throws ServletException {
        String username = principal.getName();
        userService.deleteByUsername(username);  // 사용자 삭제
        request.logout();  // 세션 로그아웃
        return "redirect:/";  // 홈으로 이동
    }
}
