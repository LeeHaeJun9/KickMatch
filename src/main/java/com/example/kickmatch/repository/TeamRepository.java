package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByIsRecommendedTrue();
}
