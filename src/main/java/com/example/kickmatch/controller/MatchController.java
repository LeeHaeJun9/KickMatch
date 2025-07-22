package com.example.kickmatch.controller;

import com.example.kickmatch.domain.Comment;
import com.example.kickmatch.domain.Location;
import com.example.kickmatch.domain.Match;
import com.example.kickmatch.domain.User;
import com.example.kickmatch.repository.CommentRepository;
import com.example.kickmatch.repository.LocationRepository;
import com.example.kickmatch.service.CommentService;
import com.example.kickmatch.service.MatchService;
import com.example.kickmatch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/match")
public class MatchController {

    private final MatchService matchService;
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final CommentService commentService;

    @GetMapping("/create")
    public String showCreateForm(Model model,
                                 @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser) {
        if (springUser.getAuthorities().stream()
                .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        model.addAttribute("match", new Match());
        model.addAttribute("locations", locationRepository.findAll());
        model.addAttribute("now", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        return "match/create";
    }

    @PostMapping("/create")
    public String createMatch(@ModelAttribute Match match,
                              @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser,
                              RedirectAttributes redirectAttributes) {
        if (match.getMatchDate().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "현재 시간보다 이전의 날짜로 등록할 수 없습니다.");
            return "redirect:/match/create";
        }
        String username = springUser.getUsername();
        User user = userService.findByUsername(username).orElseThrow();
        match.setUser(user);
        matchService.createMatch(match);
        return "redirect:/match/list";
    }

    @GetMapping("/list")
    public String listMatchesAlias(Model model) {
        List<Match> matches = matchService.findAll();
        model.addAttribute("matches", matches);
        return "match/list";
    }

    @GetMapping("/{id}")
    public String matchDetail(@PathVariable Long id,
                              @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser,
                              @RequestParam(value = "edit", required = false) Long editingCommentId,
                              Model model) {

        Match match = matchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 매칭을 찾을 수 없습니다. id=" + id));
        model.addAttribute("match", match);

        boolean isParticipated = false;
        boolean isFull = match.getParticipants().size() >= match.getMaxPlayers();
        boolean canWriteComment = false;

        List<Comment> comments = commentService.findByMatchId(id);
        model.addAttribute("comments", comments);

        if (springUser != null) {
            User user = userService.findByUsername(springUser.getUsername()).orElse(null);
            if (user != null) {
                isParticipated = matchService.isParticipant(id, user.getId());
                // 경기 종료 + 참가자일 경우만 후기 작성 가능
                if (match.getMatchDate().isBefore(LocalDateTime.now()) && isParticipated) {
                    canWriteComment = true;
                }
            }
        }

        model.addAttribute("isParticipated", isParticipated);
        model.addAttribute("isFull", isFull);
        model.addAttribute("canWriteComment", canWriteComment);
        model.addAttribute("editingCommentId", editingCommentId);

        return "match/detail";
    }



    @PostMapping("/{matchId}/join")
    public String joinMatch(@PathVariable Long matchId,
                            @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        try {
            matchService.joinMatch(matchId, user);
            redirectAttributes.addFlashAttribute("message", "매칭 참가가 완료되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/match/" + matchId;  // 상세 페이지로 다시 이동
    }

    @PostMapping("/{matchId}/cancel")
    public String cancelParticipation(@PathVariable Long matchId,
                                      @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser,
                                      RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        try {
            matchService.cancelParticipation(matchId, user);
            redirectAttributes.addFlashAttribute("message", "참가가 취소되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/match/" + matchId;
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Match match = matchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("매치를 찾을 수 없습니다."));

        boolean isAdmin = springUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/match/" + id;
        }

        model.addAttribute("match", match);
        model.addAttribute("locations", locationRepository.findAll());
        return "match/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateMatch(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam String description,
                              @RequestParam String matchDate, // 문자열로 받기
                              @RequestParam Integer maxPlayers,
                              @RequestParam Long locationId,
                              @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser,
                              RedirectAttributes redirectAttributes) {

        Match match = matchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("매치를 찾을 수 없습니다."));

        boolean isAdmin = springUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/match/" + id;
        }

        // matchDate 문자열 -> LocalDateTime 변환
        LocalDateTime parsedMatchDate;
        try {
            parsedMatchDate = LocalDateTime.parse(matchDate);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "유효하지 않은 날짜 형식입니다.");
            return "redirect:/match/" + id + "/edit";
        }

        if (parsedMatchDate.isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "과거 날짜로 수정할 수 없습니다.");
            return "redirect:/match/" + id + "/edit";
        }

        // Location 엔티티 조회
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다."));

        // 필드 업데이트
        match.setTitle(title);
        match.setDescription(description);
        match.setMatchDate(parsedMatchDate);
        match.setMaxPlayers(maxPlayers);
        match.setLocation(location);

        matchService.updateMatch(match);
        redirectAttributes.addFlashAttribute("message", "매치 정보가 수정되었습니다.");
        return "redirect:/match/" + id;
    }


    @PostMapping("/{id}/delete")
    public String deleteMatch(@PathVariable Long id,
                              @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser,
                              RedirectAttributes redirectAttributes) {
        Match match = matchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("매치를 찾을 수 없습니다."));

        boolean isAdmin = springUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
            return "redirect:/match/" + id;
        }

        matchService.deleteMatch(id);
        redirectAttributes.addFlashAttribute("message", "매치가 삭제되었습니다.");
        return "redirect:/match/list";
    }

    @PostMapping("/{matchId}/comments")
    public String postComment(@PathVariable Long matchId,
                              @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser,
                              @RequestParam String content,
                              RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Match match = matchService.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("경기를 찾을 수 없습니다."));

        // 조건: 경기 종료 이후, 참가자만 작성 가능
        if (match.getMatchDate().isAfter(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "경기 종료 후에만 후기를 작성할 수 있습니다.");
            return "redirect:/match/" + matchId;
        }

        if (!matchService.isParticipant(matchId, user.getId())) {
            redirectAttributes.addFlashAttribute("error", "경기에 참여한 사람만 후기를 작성할 수 있습니다.");
            return "redirect:/match/" + matchId;
        }

        Comment comment = Comment.builder()
                .match(match)
                .user(user)
                .content(content)
                .build();
        commentService.save(comment);
        return "redirect:/match/" + matchId;
    }

}
