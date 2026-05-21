package com.alfa.chaotictower.magic.light;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellContext;

/**
 * Light Magic: Cement
 * Turns the most recently settled block into an immovable static body.
 * The block becomes permanently anchored — it won't be knocked off.
 */
public class CementSpell extends Spell {

    public CementSpell() {
        super("Cement", "Last block becomes immovable", true, 0f);
    }

    @Override
    public void apply(SpellContext context) {
        Block block = context.getLastSettledBlock();
        if (block != null && block.body != null) {
            block.body.setType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);
            block.setCemented(true);
        }
    }
}
