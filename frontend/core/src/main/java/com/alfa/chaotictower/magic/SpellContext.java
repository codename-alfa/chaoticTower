package com.alfa.chaotictower.magic;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.entity.Player;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;


public class SpellContext {
    public final Player caster;
    public final Player target;
    public final World world;
    public final Array<Block> activeBlocks;

    public SpellContext(Player caster, Player target, World world, Array<Block> activeBlocks) {
        this.caster = caster;
        this.target = target;
        this.world = world;
        this.activeBlocks = activeBlocks;
    }

    
    public Block getLastSettledBlock() {
        Block last = null;
        for (int i = activeBlocks.size - 1; i >= 0; i--) {
            Block b = activeBlocks.get(i);
            if (b.ownerId == target.getId() && !b.isControlled() && b.body != null) {
                last = b;
                break;
            }
        }
        return last;
    }
}
