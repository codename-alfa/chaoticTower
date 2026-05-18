package com.alfa.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leaderboard")
public class Leaderboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private String gameMode;

    private Integer score;
    private Double timeRecord;
    private Double maxHeight;

    public Leaderboard() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Double getTimeRecord() { return timeRecord; }
    public void setTimeRecord(Double timeRecord) { this.timeRecord = timeRecord; }
    public Double getMaxHeight() { return maxHeight; }
    public void setMaxHeight(Double maxHeight) { this.maxHeight = maxHeight; }
}