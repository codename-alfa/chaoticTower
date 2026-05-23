package com.alfa.backend.entity;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = true)
    private String password;

    @JsonIgnore
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<Leaderboard> leaderboards;

    @JsonIgnore
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<PlayerAchievement> playerAchievements;

    public Player() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<Leaderboard> getLeaderboards() { return leaderboards; }
    public void setLeaderboards(List<Leaderboard> leaderboards) { this.leaderboards = leaderboards; }
    public List<PlayerAchievement> getPlayerAchievements() { return playerAchievements; }
    public void setPlayerAchievements(List<PlayerAchievement> playerAchievements) { this.playerAchievements = playerAchievements; }
}