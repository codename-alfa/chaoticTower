package com.alfa.backend.controller;

import com.alfa.backend.entity.PlayerAchievement;
import com.alfa.backend.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    @Autowired
    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @PostMapping("/unlock")
    public ResponseEntity<PlayerAchievement> unlockAchievement(
            @RequestParam Long playerId,
            @RequestParam String achievementName) {
        PlayerAchievement unlocked = achievementService.unlockAchievement(playerId, achievementName);
        if (unlocked != null) {
            return ResponseEntity.ok(unlocked);
        }
        return ResponseEntity.badRequest().build();
    }
}