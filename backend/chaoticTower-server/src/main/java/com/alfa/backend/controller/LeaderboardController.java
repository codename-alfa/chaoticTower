package com.alfa.backend.controller;

import com.alfa.backend.entity.Leaderboard;
import com.alfa.backend.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @Autowired
    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @PostMapping("/submit")
    public ResponseEntity<Leaderboard> submitScore(
            @RequestParam Long playerId,
            @RequestParam String gameMode,
            @RequestParam Integer score,
            @RequestParam Double timeRecord) {
        Leaderboard entry = leaderboardService.submitScore(playerId, gameMode, score, timeRecord);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/top10")
    public ResponseEntity<List<Leaderboard>> getTop10(@RequestParam String gameMode) {
        List<Leaderboard> top10 = leaderboardService.getTop10Scores(gameMode);
        return ResponseEntity.ok(top10);
    }
}