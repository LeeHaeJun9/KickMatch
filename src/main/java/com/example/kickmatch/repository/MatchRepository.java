package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByMatchDateBetween(LocalDateTime start, LocalDateTime end);
}
