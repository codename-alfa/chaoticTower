package com.alfa.chaotictower.magic.dark;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellContext;
import com.badlogic.gdx.physics.box2d.Fixture;


public class FrostSpell extends Spell {

    public FrostSpell() {
        super("Frost", "Opponent's blocks become icy", false, 15f);
    }

    @Override
    public void apply(SpellContext context) {
        
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
