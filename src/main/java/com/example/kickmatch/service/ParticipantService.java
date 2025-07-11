package com.example.kickmatch.service;

import com.example.kickmatch.domain.Participant;
import com.example.kickmatch.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public Participant save(Participant participant) {
        return participantRepository.save(participant);
    }

    public boolean existsByMatchIdAndUserId(Long matchId, Long userId) {
        return participantRepository.existsByMatchIdAndUserId(matchId, userId);
    }
}
