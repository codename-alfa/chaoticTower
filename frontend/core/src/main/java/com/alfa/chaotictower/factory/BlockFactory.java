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

    
    private final Map<Integer, List<Integer>> playerBags = new HashMap<>();

    
    private final List<Integer> sharedPuzzleSequence = new ArrayList<>();

    
    
    
    private final Pool<Block> blockPool = new Pool<Block>() {
        @Override
        protected Block newObject() {
            return new Block();
        }
    };

    private BlockFactory() {
        
        shapeDefinitions.add(new Vector2[]{                                     
            new Vector2(-0.5f, -0.5f), new Vector2(0.5f, -0.5f),
            new Vector2(-0.5f,  0.5f), new Vector2(0.5f,  0.5f)
        });
        shapeDefinitions.add(new Vector2[]{                                     
            new Vector2(-1.5f, 0), new Vector2(-0.5f, 0),
            new Vector2( 0.5f, 0), new Vector2( 1.5f, 0)
        });
        shapeDefinitions.add(new Vector2[]{                                     
            new Vector2(-1, 0), new Vector2(0, 0), new Vector2(1, 0),
            new Vector2( 0, 1)
        });
        shapeDefinitions.add(new Vector2[]{                                     
            new Vector2(-1, 0), new Vector2(0, 0), new Vector2(1, 0),
            new Vector2( 1, 1)
        });
        shapeDefinitions.add(new Vector2[]{                                     
            new Vector2(-1, 1), new Vector2(-1, 0),
            new Vector2( 0, 0), new Vector2( 1, 0)
        });
        shapeDefinitions.add(new Vector2[]{                                     
            new Vector2(-1, 0), new Vector2(0, 0),
            new Vector2( 0, 1), new Vector2(1, 1)
        });
        shapeDefinitions.add(new Vector2[]{                                     
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

    private void ensureSharedPuzzleSequenceSize(int index) {
        while (sharedPuzzleSequence.size() <= index) {
            List<Integer> bag = new ArrayList<>();
            for (int i = 0; i < shapeDefinitions.size; i++) {
                bag.add(i);
            }
            Collections.shuffle(bag, random);
            sharedPuzzleSequence.addAll(bag);
        }
    }

    
    public int peekNextType(int ownerId) {
        return peekNextType(ownerId, false, 0);
    }

    public int peekNextType(int ownerId, boolean isPuzzleMode, int blocksSpawnedCount) {
        if (isPuzzleMode) {
            ensureSharedPuzzleSequenceSize(blocksSpawnedCount);
            return sharedPuzzleSequence.get(blocksSpawnedCount);
        }
        List<Integer> bag = getBag(ownerId);
        return bag.get(0);
    }

    
    public Vector2[] getShapeOffsets(int typeIndex) {
        return shapeDefinitions.get(typeIndex);
    }

    public Block spawnBlock(World world, float x, float y, int ownerId, float scale) {
        return spawnBlock(world, x, y, ownerId, scale, false, 0);
    }

    public Block spawnBlock(World world, float x, float y, int ownerId, float scale, boolean isPuzzleMode, int blocksSpawnedCount) {
        int typeIndex;
        if (isPuzzleMode) {
            ensureSharedPuzzleSequenceSize(blocksSpawnedCount);
            typeIndex = sharedPuzzleSequence.get(blocksSpawnedCount);
        } else {
            List<Integer> bag = getBag(ownerId);
            typeIndex = bag.remove(0);
        }

        Block block = blockPool.obtain();
        
        Vector2[] offsets = shapeDefinitions.get(typeIndex);
        block.init(world, x, y, offsets, ownerId, typeIndex, scale);
        return block;
    }

    
    public void resetBags() {
        playerBags.clear();
        sharedPuzzleSequence.clear();
    }

    
    public void freeBlock(Block block) {
        blockPool.free(block);
    }
}
