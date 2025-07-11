package com.example.kickmatch.service;

import com.example.kickmatch.domain.Team;
import com.example.kickmatch.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public List<Team> findRecommendedTeams() {
        return teamRepository.findByIsRecommendedTrue();
    }

    public Team save(Team team) {
        return teamRepository.save(team);
    }
}
