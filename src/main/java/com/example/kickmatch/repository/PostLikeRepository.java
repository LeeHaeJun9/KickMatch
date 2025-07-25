package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Post;
import com.example.kickmatch.domain.PostLike;
import com.example.kickmatch.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<PostLike> findByUserAndPost(User user, Post post);
    long countByPost(Post post);
}
