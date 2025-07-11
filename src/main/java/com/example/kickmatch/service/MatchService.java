package com.example.kickmatch.service;

import com.example.kickmatch.domain.Match;
import com.example.kickmatch.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    public List<Match> findAll() {
        return matchRepository.findAll();
    }

    public Match save(Match match) {
        return matchRepository.save(match);
    }

    public void deleteById(Long id) {
        matchRepository.deleteById(id);
    }
}
