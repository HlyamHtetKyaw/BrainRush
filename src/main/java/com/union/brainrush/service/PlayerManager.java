package com.union.brainrush.service;

import com.union.brainrush.model.PlayerEntity;
import com.union.brainrush.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerManager {
    @Autowired
    private PlayerRepository repository;

    private PlayerEntity currentPlayer;

    public void startNewSession() {
        currentPlayer = new PlayerEntity();
        repository.save(currentPlayer);
    }

    public void updateMark(int mark) {
        if (currentPlayer != null) {
            currentPlayer.setMark(mark);
            repository.save(currentPlayer);
        }
    }

    public void abandonSession() {
        if (currentPlayer != null) {
            repository.deleteByUuid(currentPlayer.getUuid());
            currentPlayer = null;
        }
    }

    public PlayerEntity getCurrentPlayer() { return currentPlayer; }
}
