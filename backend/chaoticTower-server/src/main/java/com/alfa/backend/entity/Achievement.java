package com.alfa.backend.entity;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "achievements")
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL)
    private List<PlayerAchievement> playerAchievements;

    public Achievement() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<PlayerAchievement> getPlayerAchievements() { return playerAchievements; }
    public void setPlayerAchievements(List<PlayerAchievement> playerAchievements) { this.playerAchievements = playerAchievements; }
}