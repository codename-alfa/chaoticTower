package com.alfa.chaotictower.factory;

import com.alfa.chaotictower.entity.Block;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;
import java.util.Random;

public class BlockFactory {
    private static BlockFactory instance;
    private final Random random = new Random();

    private final Pool<Block> blockPool = new Pool<Block>() {
        @Override
        protected Block newObject() {
            return new Block();
        }
    };

    private BlockFactory() {}

    public static BlockFactory getInstance() {
        if (instance == null) {
            instance = new BlockFactory();
        }
        return instance;
    }

    public Block spawnBlock(World world, float x, float y) {
        Block block = blockPool.obtain();
        int type = random.nextInt(3);
        float width;
        float height;

        if (type == 0) {
            width = 2f;
            height = 1f;
        } else if (type == 1) {
            width = 1f;
            height = 2f;
        } else {
            width = 1.5f;
            height = 1.5f;
        }

        block.init(world, x, y, width, height);
        return block;
    }

    public void freeBlock(Block block) {
        blockPool.free(block);
    }
}
