package com.alfa.chaotictower.magic.light;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellContext;

/**
 * Light Magic: Lightning
 * Destroys the most recently placed block — useful for recovering
 * from bad placements before they cause collapse.
 */
public class LightningSpell extends Spell {

    public LightningSpell() {
        super("Lightning", "Destroys your last block", true, 0f);
    }

    @Override
    public void apply(SpellContext context) {
        Block block = context.getLastSettledBlock();
        if (block != null && block.body != null) {
            context.world.destroyBody(block.body);
            block.body = null;
            context.activeBlocks.removeValue(block, true);
        }
    }
}
