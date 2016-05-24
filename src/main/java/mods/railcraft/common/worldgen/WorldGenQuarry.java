/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.worldgen;

import mods.railcraft.common.plugins.forge.OreDictPlugin;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class WorldGenQuarry extends WorldGenerator {

    private static final int DISTANCE_OUTER_SQ = 8 * 8;
    private final Block blockStone;
    private final int meta;
    public final Set<Block> replaceable = new HashSet<Block>();

    public WorldGenQuarry(Block block, int meta) {
        this.blockStone = block;
        this.meta = meta;

        replaceable.add(Blocks.COAL_ORE);
        replaceable.add(Blocks.IRON_ORE);
        replaceable.add(Blocks.GOLD_ORE);
        replaceable.add(Blocks.DIAMOND_ORE);
        replaceable.add(Blocks.EMERALD_ORE);
        replaceable.add(Blocks.LAPIS_ORE);
        replaceable.add(Blocks.QUARTZ_ORE);
        replaceable.add(Blocks.REDSTONE_ORE);
        replaceable.add(Blocks.LIT_REDSTONE_ORE);
        replaceable.add(Blocks.DIRT);
        replaceable.add(Blocks.GRAVEL);
        replaceable.add(Blocks.GRASS);
        replaceable.add(Blocks.CLAY);

        replaceable.addAll(OreDictPlugin.getOreBlocks());
    }

    @Override
    public boolean generate(World world, Random rand, int x, int y, int z) {
//        Game.log(Level.INFO, "Generating Quarry at {0}, {1}, {2}", x, y, z);
        boolean clearTop = true;
        for (int i = -8; i < 8; i++) {
            for (int k = -8; k < 8; k++) {
                for (int j = 1; j < 4 && j + y < world.getActualHeight() - 1; j++) {
                    int distSq = i * i + k * k;
                    if (distSq <= DISTANCE_OUTER_SQ)
                        if (isLiquid(world, x + i, y + j, z + k)) {
                            clearTop = false;
                            break;
                        }
                }
            }
        }
        if (clearTop)
            for (int i = -8; i < 8; i++) {
                for (int k = -8; k < 8; k++) {
                    for (int j = 1; j < 4 && j + y < world.getActualHeight() - 1; j++) {
                        int distSq = i * i + k * k;
                        if (distSq <= DISTANCE_OUTER_SQ)
                            if (!placeAir(world, rand, x + i, y + j, z + k))
                                break;
                    }
                }
            }
        for (int i = -8; i < 8; i++) {
            for (int k = -8; k < 8; k++) {
                for (int j = -8; j < 1 && j + y < world.getActualHeight() - 1; j++) {
                    int distSq = i * i + k * k + j * j;
                    if (distSq <= DISTANCE_OUTER_SQ)
                        placeStone(world, rand, x + i, y + j, z + k);
                }
            }
        }

        return true;
    }

    private boolean isLiquid(World world, int x, int y, int z) {
        Block block = WorldPlugin.getBlock(world, x, y, z);
        return block instanceof BlockLiquid || block instanceof IFluidBlock;
    }

    private boolean placeAir(World world, Random rand, int x, int y, int z) {
//        if (!world.isBlockLoaded(x, y, z)) {
//            return false;
//        }
        if (WorldPlugin.getBlock(world, x, y + 1, z) != Blocks.AIR)
            return false;
        if (isLiquid(world, x, y, z))
            return false;
        
        if (WorldPlugin.getBlock(world, x + 1, y + 1, z) != Blocks.AIR)
            return false;
        if (WorldPlugin.getBlock(world, x - 1, y + 1, z) != Blocks.AIR)
            return false;
        if (WorldPlugin.getBlock(world, x, y + 1, z + 1) != Blocks.AIR)
            return false;
        if (WorldPlugin.getBlock(world, x, y + 1, z - 1) != Blocks.AIR)
            return false;
        
        world.setBlock(x, y, z, Blocks.AIR, 0, 2);
        return true;
    }

    private void placeStone(World world, Random rand, int x, int y, int z) {
//        if (!world.isBlockLoaded(x, y, z)) {
//            return;
//        }
        //Removes tallgrass
        if (WorldPlugin.getBlock(world, x, y + 1, z) == Blocks.TALLGRASS)
            world.setBlock(x, y + 1, z, Blocks.AIR, 0, 2);
        
        if (isReplaceable(world, x, y, z))
            world.setBlock(x, y, z, blockStone, meta, 2);
    }

    private boolean isReplaceable(World world, int x, int y, int z) {
        Block existing = WorldPlugin.getBlock(world, x, y, z);
        if (existing == null)
            return false;
        if (existing.isReplaceableOreGen(world, x, y, z, Blocks.STONE))
            return true;
        return replaceable.contains(existing);
    }

}
