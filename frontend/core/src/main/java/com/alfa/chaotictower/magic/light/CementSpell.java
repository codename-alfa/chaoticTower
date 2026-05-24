package com.alfa.chaotictower.magic.light;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellContext;


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
