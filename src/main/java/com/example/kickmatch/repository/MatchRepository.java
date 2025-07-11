package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
