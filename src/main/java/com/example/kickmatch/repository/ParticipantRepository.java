package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Participant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    boolean existsByMatchIdAndUserId(Long matchId, Long userId);

    Optional<Participant> findByMatchIdAndUserId(Long matchId, Long id);

    @EntityGraph(attributePaths = {"match", "match.location"})
    List<Participant> findByUserId(Long id);
}
