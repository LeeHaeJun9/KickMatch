package com.example.kickmatch.service;

import com.example.kickmatch.domain.Match;
import com.example.kickmatch.domain.Participant;
import com.example.kickmatch.domain.User;
import com.example.kickmatch.repository.MatchRepository;
import com.example.kickmatch.repository.ParticipantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    private final ParticipantRepository participantRepository;

    public List<Match> findAll() {
        return matchRepository.findAll();
    }

    public Match save(Match match) {
        return matchRepository.save(match);
    }

    public void deleteById(Long id) {
        matchRepository.deleteById(id);
    }

    public Match createMatch(Match match) {
        return matchRepository.save(match);
    }

    public Optional<Match> findById(Long id) {
        return matchRepository.findById(id);
    }

    @Transactional
    public void joinMatch(Long matchId, User user) {
        if ("ROLE_ADMIN".equals(user.getRole())) {
            throw new IllegalStateException("관리자 계정은 경기 참가가 불가능합니다.");
        }
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("매칭을 찾을 수 없습니다. id=" + matchId));

        // 이미 참가한 사람인지 체크
        boolean alreadyParticipated = match.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));

        if (alreadyParticipated) {
            throw new IllegalStateException("이미 참가한 매칭입니다.");
        }

        // 최대 인원 초과 체크
        if (match.getParticipants().size() >= match.getMaxPlayers()) {
            throw new IllegalStateException("매칭 정원이 가득 찼습니다.");
        }

        if (hasTimeConflict(user, match)) {
            throw new IllegalStateException("동시간대에 이미 참가한 경기가 있습니다.");
        }

        Participant participant = Participant.builder()
                .match(match)
                .user(user)
                .build();

        match.getParticipants().add(participant);
        participantRepository.save(participant);
    }

    private boolean hasTimeConflict(User user, Match newMatch) {
        List<Participant> participantList = participantRepository.findByUserId(user.getId());

        for (Participant p : participantList) {
            Match existingMatch = p.getMatch();
            // 경기 시간 겹치는지 확인 (예: matchDate가 같은 날, 시간 겹침 등)
            // 단순 비교 예시(필요 시 경기 종료시간 등 추가 고려)
            if (existingMatch.getMatchDate().equals(newMatch.getMatchDate())) {
                return true;
            }
        }
        return false;
    }


    @Transactional
    public void cancelParticipation(Long matchId, User user) {
        Optional<Participant> participantOpt = participantRepository.findByMatchIdAndUserId(matchId, user.getId());
        if (participantOpt.isEmpty()) {
            throw new IllegalStateException("참가 기록이 없습니다.");
        }
        participantRepository.delete(participantOpt.get());
    }

    public boolean isParticipant(Long matchId, Long userId) {
        return participantRepository.existsByMatchIdAndUserId(matchId, userId);
    }

    public void updateMatch(Match match) {
        matchRepository.save(match);
    }

    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }
}
