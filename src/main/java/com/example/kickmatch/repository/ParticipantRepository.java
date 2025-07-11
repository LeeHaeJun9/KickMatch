package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    boolean existsByMatchIdAndUserId(Long matchId, Long userId);
}
