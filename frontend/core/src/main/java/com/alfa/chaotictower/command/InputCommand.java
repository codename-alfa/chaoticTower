package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;

/**
 * Command Pattern (Design Pattern #8).
 * Encapsulates a single player action so that input handling is
 * decoupled from the concrete key-bindings and can be reused,
 * queued, or even replayed.
 */
public interface InputCommand {
    /**
     * Execute this command on the given block.
     * @param block the currently controlled block for a specific player
     */
    void execute(Block block);
}
