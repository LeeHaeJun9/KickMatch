package com.example.kickmatch.controller;

import com.example.kickmatch.domain.Comment;
import com.example.kickmatch.domain.User;
import com.example.kickmatch.service.CommentService;
import com.example.kickmatch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    // 댓글 수정 처리 (POST)
    @PostMapping("/{commentId}/edit")
    public String edit(@PathVariable Long commentId,
                       @RequestParam String content,
                       @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser,
                       RedirectAttributes redirectAttributes) {

        Comment comment = commentService.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + commentId));

        User user = userService.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "본인 댓글만 수정할 수 있습니다.");
            return "redirect:/match/" + comment.getMatch().getId();
        }

        comment.setContent(content);
        commentService.save(comment);

        redirectAttributes.addFlashAttribute("message", "댓글이 수정되었습니다.");
        return "redirect:/match/" + comment.getMatch().getId();
    }

    // 댓글 삭제 처리 (POST)
    @PostMapping("/{commentId}/delete")
    public String delete(@PathVariable Long commentId,
                         @AuthenticationPrincipal org.springframework.security.core.userdetails.User springUser,
                         RedirectAttributes redirectAttributes) {

        Comment comment = commentService.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + commentId));

        User user = userService.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "본인 댓글만 삭제할 수 있습니다.");
            return "redirect:/match/" + comment.getMatch().getId();
        }

        commentService.delete(commentId);
        redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");
        return "redirect:/match/" + comment.getMatch().getId();
    }
}
