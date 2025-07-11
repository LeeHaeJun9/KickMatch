package com.example.kickmatch.controller;

import com.example.kickmatch.domain.User;
import com.example.kickmatch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // 관리자 권한 있는 사용자만 접근 가능
public class AdminController {

    private final UserService userService;

    // 1. 승인 대기중인 사용자 목록 페이지
    @GetMapping("/approval")
    public String showApprovalList(Model model) {
        List<User> pendingAdmins = userService.findByRole("ROLE_PENDING_ADMIN");
        model.addAttribute("pendingAdmins", pendingAdmins);
        return "admin/approval";
    }

    // 2. 승인 처리
    @PostMapping("/approve/{userId}")
    public String approveUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        userService.updateUserRole(userId, "ROLE_ADMIN");
        redirectAttributes.addFlashAttribute("message", "관리자 승인이 완료되었습니다.");
        return "redirect:/admin/approval";
    }

    // 3. 거절 처리 (삭제하거나 ROLE_USER로 변경)
    @PostMapping("/reject/{userId}")
    public String rejectUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        userService.deleteUserById(userId); // 삭제 처리
        redirectAttributes.addFlashAttribute("message", "승인 요청이 거절되었습니다.");
        return "redirect:/admin/approval";
    }
}
