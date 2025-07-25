package com.example.kickmatch.service;

import com.example.kickmatch.domain.Post;
import com.example.kickmatch.domain.PostLike;
import com.example.kickmatch.domain.User;
import com.example.kickmatch.repository.PostLikeRepository;
import com.example.kickmatch.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPost(Long id) {
        return postRepository.findById(id);
    }

    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public void increaseViewCount(Post post) {
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    public boolean toggleLike(User user, Post post) {
        Optional<PostLike> existingLike  = postLikeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            return false;
        } else {
            PostLike like = PostLike.builder()
                    .user(user)
                    .post(post)
                    .createdAt(LocalDateTime.now())
                    .build();
            postLikeRepository.save(like);
            return true;
        }
    }

    public long countLikes(Post post) {
        return postLikeRepository.countByPost(post);
    }
}
