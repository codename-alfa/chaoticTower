package com.alfa.chaotictower.magic.dark;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellContext;
import com.badlogic.gdx.physics.box2d.Fixture;

/**
 * Dark Magic: Frost
 * Sets the opponent's next few blocks to near-zero friction (slippery ice).
 * Duration: 15 seconds — blocks spawned during this period will be icy.
 * The actual friction override is handled in PlayingScreen's preSolve callback
 * by checking SpellManager.hasActiveEffect().
 */
public class FrostSpell extends Spell {

    public FrostSpell() {
        super("Frost", "Opponent's blocks become icy", false, 15f);
    }

    @Override
    public void apply(SpellContext context) {
        // Set friction to near-zero on the opponent's currently controlled block
        Block current = context.target.getCurrentBlock();
        if (current != null && current.body != null) {
            for (Fixture f : current.body.getFixtureList()) {
                f.setFriction(0.02f);
            }
        }
        context.target.setFrosted(true);
    }

    @Override
    public void remove(SpellContext context) {
        context.target.setFrosted(false);
    }
}
