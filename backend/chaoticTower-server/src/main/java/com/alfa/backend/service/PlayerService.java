package com.alfa.backend.service;

import com.alfa.backend.entity.Player;
import com.alfa.backend.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player login(String username, String password) {
        Optional<Player> existingPlayer = playerRepository.findByUsername(username);
        if (existingPlayer.isPresent()) {
            Player player = existingPlayer.get();
            
            if (player.getPassword() == null || player.getPassword().isEmpty()) {
                player.setPassword(password);
                return playerRepository.save(player);
            }
            if (player.getPassword().equals(password)) {
                return player;
            } else {
                throw new IllegalArgumentException("Incorrect password");
            }
        }
        throw new IllegalArgumentException("Username not found");
    }

    public Player register(String username, String password) {
        Optional<Player> existingPlayer = playerRepository.findByUsername(username);
        if (existingPlayer.isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        Player newPlayer = new Player();
        newPlayer.setUsername(username);
        newPlayer.setPassword(password);
        return playerRepository.save(newPlayer);
    }

    @jakarta.annotation.PostConstruct
    public void initAlfaPlayer() {
        Optional<Player> alfa = playerRepository.findByUsername("alfa");
        if (alfa.isPresent()) {
            Player player = alfa.get();
            if (player.getPassword() == null || player.getPassword().isEmpty()) {
                player.setPassword("alfa");
                playerRepository.save(player);
            }
        }
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }
}