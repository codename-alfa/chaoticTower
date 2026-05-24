package com.alfa.backend.repository;

import com.alfa.backend.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {
    List<Leaderboard> findTop10ByGameModeOrderByScoreDesc(String gameMode);
    List<Leaderboard> findTop10ByGameModeOrderByScoreAsc(String gameMode);
    Optional<Leaderboard> findByPlayer_IdAndGameMode(Long playerId, String gameMode);
}