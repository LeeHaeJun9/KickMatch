package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMatchIdOrderByCreatedAtAsc(Long matchId);

}
