package com.alfa.chaotictower.factory;

import com.alfa.chaotictower.entity.Block;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.badlogic.gdx.utils.Array;

public class BlockFactory {

    private static BlockFactory instance;
    private final Random random = new Random();
    private final Array<Vector2[]> shapeDefinitions = new Array<>();

    /** Per-player 7-bag queues. Key = ownerId, Value = remaining indices in current bag. */
    private final Map<Integer, List<Integer>> playerBags = new HashMap<>();

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

    /**
     * Returns the bag for the given player, refilling with a new shuffled
     * set of 0..6 if the bag is empty.
     */
    private List<Integer> getBag(int ownerId) {
        List<Integer> bag = playerBags.get(ownerId);
        if (bag == null || bag.isEmpty()) {
            bag = new ArrayList<>();
            for (int i = 0; i < shapeDefinitions.size; i++) {
                bag.add(i);
            }
            Collections.shuffle(bag, random);
            playerBags.put(ownerId, bag);
        }
        return bag;
    }

    /**
     * Peek at the next block type for a given player without consuming it.
     * Used for the "NEXT" block preview in the HUD.
     * @return the tetromino type index (0-6)
     */
    public int peekNextType(int ownerId) {
        List<Integer> bag = getBag(ownerId);
        return bag.get(0);
    }

    /**
     * Returns the tile offsets for a given tetromino type (for rendering previews).
     */
    public Vector2[] getShapeOffsets(int typeIndex) {
        return shapeDefinitions.get(typeIndex);
    }

    public Block spawnBlock(World world, float x, float y, int ownerId) {
        List<Integer> bag = getBag(ownerId);
        int typeIndex = bag.remove(0);

        Block block = blockPool.obtain();
        // Offset di-pass by reference (read-only di Block.init()) — aman, tanpa alokasi
        Vector2[] offsets = shapeDefinitions.get(typeIndex);
        block.init(world, x, y, offsets, ownerId, typeIndex);
        return block;
    }

    /**
     * Resets the bags for all players. Call when starting a new game.
     */
    public void resetBags() {
        playerBags.clear();
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
