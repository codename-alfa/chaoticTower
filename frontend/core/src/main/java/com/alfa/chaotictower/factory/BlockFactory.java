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
    private final Array<Vector2[]> shapeDefinitions = new Array<>();

    // Pool hanya mengelola Java object (Block).
    // Lifecycle Box2D body sepenuhnya dikelola oleh PlayingScreen
    // melalui world.destroyBody() sebelum freeBlock() dipanggil.
    private final Pool<Block> blockPool = new Pool<Block>() {
        @Override
        protected Block newObject() {
            return new Block();
        }
    };

    private BlockFactory() {
        // 7 Tetromino: O, I, T, L, J, S, Z
        shapeDefinitions.add(new Vector2[]{                                     // O
            new Vector2(-0.5f, -0.5f), new Vector2(0.5f, -0.5f),
            new Vector2(-0.5f,  0.5f), new Vector2(0.5f,  0.5f)
        });
        shapeDefinitions.add(new Vector2[]{                                     // I
            new Vector2(-1.5f, 0), new Vector2(-0.5f, 0),
            new Vector2( 0.5f, 0), new Vector2( 1.5f, 0)
        });
        shapeDefinitions.add(new Vector2[]{                                     // T
            new Vector2(-1, 0), new Vector2(0, 0), new Vector2(1, 0),
            new Vector2( 0, 1)
        });
        shapeDefinitions.add(new Vector2[]{                                     // L
            new Vector2(-1, 0), new Vector2(0, 0), new Vector2(1, 0),
            new Vector2( 1, 1)
        });
        shapeDefinitions.add(new Vector2[]{                                     // J
            new Vector2(-1, 1), new Vector2(-1, 0),
            new Vector2( 0, 0), new Vector2( 1, 0)
        });
        shapeDefinitions.add(new Vector2[]{                                     // S
            new Vector2(-1, 0), new Vector2(0, 0),
            new Vector2( 0, 1), new Vector2(1, 1)
        });
        shapeDefinitions.add(new Vector2[]{                                     // Z
            new Vector2(-1, 1), new Vector2(0, 1),
            new Vector2( 0, 0), new Vector2(1, 0)
        });
    }

    public static BlockFactory getInstance() {
        if (instance == null) {
            instance = new BlockFactory();
        }
        return instance;
    }

    public Block spawnBlock(World world, float x, float y, int ownerId) {
        Block block = blockPool.obtain();
        // Offset di-pass by reference (read-only di Block.init()) — aman, tanpa alokasi
        Vector2[] offsets = shapeDefinitions.get(random.nextInt(shapeDefinitions.size));
        block.init(world, x, y, offsets, ownerId);
        return block;
    }

    /**
     * Kembalikan block ke pool.
     * PENTING: Pastikan PlayingScreen sudah memanggil world.destroyBody(block.body)
     * dan meng-null block.body SEBELUM memanggil metode ini.
     * Jika tidak, body akan leak di Box2D world.
     */
    public void freeBlock(Block block) {
        blockPool.free(block);
    }
}
