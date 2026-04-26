package com.alfa.chaotictower.factory;

import com.alfa.chaotictower.entity.Block;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;
import java.util.Random;
import com.badlogic.gdx.utils.Array;

public class BlockFactory {
    private static BlockFactory instance;
    private final Random random = new Random();

    // Daftar koordinat offset untuk 7 Tetromino
    private final Array<Vector2[]> shapeDefinitions = new Array<>();

    private final Pool<Block> blockPool = new Pool<Block>() {
        @Override
        protected Block newObject() {
            return new Block();
        }
    };

    private BlockFactory() {
        // Mendefinisikan Bentuk Tetromino Dasar (Standard Composite Bodies)

        // 1. O Shape (Kuning - Square)
        shapeDefinitions.add(new Vector2[]{
            new Vector2(-0.5f, -0.5f), new Vector2(0.5f, -0.5f),
            new Vector2(-0.5f, 0.5f), new Vector2(0.5f, 0.5f)
        });

        // 2. I Shape (Cyan - Line)
        shapeDefinitions.add(new Vector2[]{
            new Vector2(-1.5f, 0), new Vector2(-0.5f, 0), new Vector2(0.5f, 0), new Vector2(1.5f, 0)
        });

        // 3. T Shape (Ungu - T-Shape)
        shapeDefinitions.add(new Vector2[]{
            new Vector2(-1, 0), new Vector2(0, 0), new Vector2(1, 0), new Vector2(0, 1)
        });

        // 4. L Shape (Oranye - L-Shape)
        shapeDefinitions.add(new Vector2[]{
            new Vector2(-1, 0), new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1)
        });

        // 5. J Shape (Biru - L-Shape Inverted)
        shapeDefinitions.add(new Vector2[]{
            new Vector2(-1, 1), new Vector2(-1, 0), new Vector2(0, 0), new Vector2(1, 0)
        });

        // 6. S Shape (Hijau - S-Shape)
        shapeDefinitions.add(new Vector2[]{
            new Vector2(-1, 0), new Vector2(0, 0), new Vector2(0, 1), new Vector2(1, 1)
        });

        // 7. Z Shape (Merah - Z-Shape)
        shapeDefinitions.add(new Vector2[]{
            new Vector2(-1, 1), new Vector2(0, 1), new Vector2(0, 0), new Vector2(1, 0)
        });
    }

    public static BlockFactory getInstance() {
        if (instance == null) {
            instance = new BlockFactory();
        }
        return instance;
    }

    public Block spawnBlock(World world, float x, float y) {
        Block block = blockPool.obtain();
        // Memilih bentuk acak dari 7 opsi
        Vector2[] offsets = shapeDefinitions.get(random.nextInt(shapeDefinitions.size));
        block.init(world, x, y, offsets);
        return block;
    }

    public void freeBlock(Block block) {
        blockPool.free(block);
    }
}
