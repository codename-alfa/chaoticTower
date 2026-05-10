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

    public Player registerOrLogin(String username) {
        Optional<Player> existingPlayer = playerRepository.findByUsername(username);
        if (existingPlayer.isPresent()) {
            return existingPlayer.get();
        }

        Player newPlayer = new Player();
        newPlayer.setUsername(username);
        return playerRepository.save(newPlayer);
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }
}