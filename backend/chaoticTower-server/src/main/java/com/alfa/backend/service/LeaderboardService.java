package com.alfa.backend.service;

import com.alfa.backend.entity.Leaderboard;
import com.alfa.backend.entity.Player;
import com.alfa.backend.repository.LeaderboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final PlayerService playerService;

    @Autowired
    public LeaderboardService(LeaderboardRepository leaderboardRepository, PlayerService playerService) {
        this.leaderboardRepository = leaderboardRepository;
        this.playerService = playerService;
    }

    public Leaderboard submitScore(Long playerId, String gameMode, Integer score, Double timeRecord) {
        Player player = playerService.getPlayerById(playerId);
        if (player == null) {
            throw new RuntimeException("Player not found");
        }

        Leaderboard entry = new Leaderboard();
        entry.setPlayer(player);
        entry.setGameMode(gameMode);
        entry.setScore(score);
        entry.setTimeRecord(timeRecord);

        return leaderboardRepository.save(entry);
    }

    public List<Leaderboard> getTop10Scores(String gameMode) {
        return leaderboardRepository.findTop10ByGameModeOrderByScoreDesc(gameMode);
    }
}