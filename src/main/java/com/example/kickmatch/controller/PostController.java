package com.example.kickmatch.controller;

import com.example.kickmatch.domain.Post;
import com.example.kickmatch.domain.User;
import com.example.kickmatch.service.PostService;
import com.example.kickmatch.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @GetMapping("/list")
    public String listPosts(Model model) {
        List<Post> postList = postService.findAllPosts();
        model.addAttribute("posts", postList);
        return "post/list";
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("post", new Post());
        return "post/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute Post post,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        post.setWriter(user);
        postService.savePost(post);
        return "redirect:/posts/list";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model, HttpSession session,
                           @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.getPost(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        Set<Long> viewedPostIds = (Set<Long>) session.getAttribute("VIEWED_POSTS");

        if (viewedPostIds == null) {
            viewedPostIds = new HashSet<>();
        }

        // 해당 게시글을 처음 보는 경우에만 조회수 증가
        if (!viewedPostIds.contains(id)) {
            postService.increaseViewCount(post);
            viewedPostIds.add(id);
            session.setAttribute("VIEWED_POSTS", viewedPostIds);
        }

        model.addAttribute("post", post);

        if (userDetails != null) {
            model.addAttribute("currentUsername", userDetails.getUsername());

            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

            boolean liked = post.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(user.getId()));
            model.addAttribute("liked", liked);
        } else {
            model.addAttribute("currentUsername", null);
        }

        model.addAttribute("likeCount", post.getLikes().size());


        return "post/detail";
    }

    @GetMapping("/edit/{id}")
    public String editPostForm(@PathVariable Long id, Model model,
                               @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        Post post = postService.getPost(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        if (!post.getWriter().getUsername().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        model.addAttribute("post", post);  // 수정할 게시글 정보를 뷰로 전달
        return "post/edit";  // post/edit.html로 이동
    }

    @PostMapping("/edit/{id}")
    public String editPostSubmit(@PathVariable Long id,
                                 @ModelAttribute("post") Post postForm,
                                 @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        Post post = postService.getPost(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        if (!post.getWriter().getUsername().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        post.setTitle(postForm.getTitle());
        post.setContent(postForm.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        postService.savePost(post);

        return "redirect:/posts/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        Post post = postService.getPost(id)
                .orElseThrow(()-> new IllegalArgumentException("게시글 없음"));

        if (!post.getWriter().getUsername().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        postService.deletePost(id);
        return "redirect:/posts/list";
    }

    @PostMapping("/{id}/like")
    public String likePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Post post = postService.getPost(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        postService.toggleLike(user, post);

        return "redirect:/posts/" + id;
    }
}
