package com.example.kickmatch.controller;

import com.example.kickmatch.domain.Location;
import com.example.kickmatch.domain.User;
import com.example.kickmatch.service.ImageService;
import com.example.kickmatch.service.LocationService;
import com.example.kickmatch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // 관리자 권한 있는 사용자만 접근 가능
public class AdminController {

    private final UserService userService;
    private final LocationService locationService;
    private final ImageService imageService;

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

    @GetMapping("/location/new")
    public String showCreateLocationForm(Model model) {
        model.addAttribute("location", new Location());
        return "admin/location_form";
    }

    // 구장 등록 처리
    @PostMapping("/location/new")
    public String createLocation(@Valid @ModelAttribute Location location,
                                 BindingResult bindingResult,
                                 @RequestParam("imageFile") MultipartFile imageFile,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/location_form";
        }

        if (!imageFile.isEmpty()) {
            String savedFilePath = imageService.saveImage(imageFile);
            location.setImageUrl(savedFilePath);
        }

        locationService.save(location);
        return "redirect:/admin/location/list";
    }

    // 구장 목록 페이지 (선택)
    @GetMapping("/location/list")
    public String showLocationList(Model model) {
        model.addAttribute("locations", locationService.findAll());
        return "admin/location_list";
    }


    @GetMapping("/location/update/{id}")
    public String showEditLocationForm(@PathVariable Long id, Model model) {
        Location location = locationService.findById(id);
        model.addAttribute("location", location);
        return "admin/location_form";  // 등록 폼과 수정 폼을 동일한 뷰로 사용 가능
    }

    @PostMapping("/location/update/{id}")
    public String updateLocation(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam String address,
                                 @RequestParam String region,
                                 @RequestParam String description,
                                 @RequestParam(required = false, name = "imageFile") MultipartFile imageFile) {
        Location location = locationService.findById(id);

        location.setName(name);
        location.setAddress(address);
        location.setRegion(region);
        location.setDescription(description);

        if (imageFile  != null && !imageFile .isEmpty()) {
            String newImageUrl = imageService.updateImage(imageFile , location.getImageUrl());
            location.setImageUrl(newImageUrl);
        }

        locationService.save(location);
        return "redirect:/admin/location/list";
    }

    @PostMapping("/location/delete/{id}")
    public String deleteLocation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Location location = locationService.findById(id);

        if (location.getMatches() != null && !location.getMatches().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "해당 구장은 등록된 매치가 있어 삭제할 수 없습니다.");
            return "redirect:/admin/location/list";
        }

        imageService.deleteImage(location.getImageUrl());
        locationService.deleteById(id);
        return "redirect:/admin/location/list";
    }

    @GetMapping("/location/{id}")
    public String showLocationDetail(@PathVariable Long id, Model model) {
        Location location = locationService.findById(id);
        model.addAttribute("location", location);
        return "admin/location_detail"; // 뷰 이름
    }

}
