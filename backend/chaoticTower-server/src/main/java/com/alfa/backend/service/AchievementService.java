package com.alfa.backend.service;

import com.alfa.backend.entity.Achievement;
import com.alfa.backend.entity.Player;
import com.alfa.backend.entity.PlayerAchievement;
import com.alfa.backend.repository.AchievementRepository;
import com.alfa.backend.repository.PlayerAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final PlayerAchievementRepository playerAchievementRepository;
    private final PlayerService playerService;

    @Autowired
    public AchievementService(AchievementRepository achievementRepository, PlayerAchievementRepository playerAchievementRepository, PlayerService playerService) {
        this.achievementRepository = achievementRepository;
        this.playerAchievementRepository = playerAchievementRepository;
        this.playerService = playerService;
    }

    public PlayerAchievement unlockAchievement(Long playerId, String achievementName) {
        Player player = playerService.getPlayerById(playerId);
        Optional<Achievement> achievementOpt = achievementRepository.findByName(achievementName);

        if (player == null || achievementOpt.isEmpty()) {
            throw new RuntimeException("Player or Achievement not found");
        }

        Achievement achievement = achievementOpt.get();
        List<PlayerAchievement> unlocked = playerAchievementRepository.findByPlayerId(playerId);

        boolean alreadyUnlocked = unlocked.stream()
                .anyMatch(pa -> pa.getAchievement().getId().equals(achievement.getId()));

        if (alreadyUnlocked) {
            return null;
        }

        PlayerAchievement pa = new PlayerAchievement();
        pa.setPlayer(player);
        pa.setAchievement(achievement);

        return playerAchievementRepository.save(pa);
    }
}