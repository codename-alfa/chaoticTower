package com.alfa.chaotictower.magic.light;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellContext;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;


public class IvySpell extends Spell {

    public IvySpell() {
        super("Ivy", "Binds top blocks together", true, 0f);
    }

    @Override
    public void apply(SpellContext context) {
        
        Block last = null, secondLast = null;
        for (int i = context.activeBlocks.size - 1; i >= 0; i--) {
            Block b = context.activeBlocks.get(i);
            if (b.ownerId == context.target.getId() && !b.isControlled() && b.body != null) {
                if (last == null) {
                    last = b;
                } else {
                    secondLast = b;
                    break;
                }
            }
        }

        if (last != null && secondLast != null && last.body != null && secondLast.body != null) {
            WeldJointDef jointDef = new WeldJointDef();
            jointDef.initialize(last.body, secondLast.body, last.body.getWorldCenter());
            jointDef.collideConnected = true;
            context.world.createJoint(jointDef);
            last.setIvied(true);
            secondLast.setIvied(true);
        }
    }
}
