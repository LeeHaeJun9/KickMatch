package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
