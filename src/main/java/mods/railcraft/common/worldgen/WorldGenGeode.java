/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.worldgen;

import mods.railcraft.common.blocks.ore.BlockOre;
import mods.railcraft.common.blocks.ore.EnumOre;
import mods.railcraft.common.plugins.forge.OreDictPlugin;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class WorldGenGeode extends WorldGenerator {

    private static final int DISTANCE_OUTER_SQ = 8 * 8;
    private static final int DISTANCE_ORE_SQ = 5 * 5;
    private static final int DISTANCE_INNER_SQ = 4 * 4;
    private final Block blockStone;
    private final int meta;
    public final Set<Block> ores = new HashSet<Block>();
    private final Block blockOre;

    public WorldGenGeode(Block block, int meta) {
        super();
        this.blockStone = block;
        this.meta = meta;

        ores.add(Blocks.COAL_ORE);
        ores.add(Blocks.IRON_ORE);
        ores.add(Blocks.GOLD_ORE);
        ores.add(Blocks.DIAMOND_ORE);
        ores.add(Blocks.EMERALD_ORE);
        ores.add(Blocks.LAPIS_ORE);
        ores.add(Blocks.QUARTZ_ORE);
        ores.add(Blocks.REDSTONE_ORE);
        ores.add(Blocks.LIT_REDSTONE_ORE);

        ores.addAll(OreDictPlugin.getOreBlocks());

        blockOre = BlockOre.getBlock();
    }

    @Override
    public boolean generate(World world, Random rand, int x, int y, int z) {
        for (int i = -8; i < 8; i++) {
            for (int j = -8; j < 8; j++) {
                for (int k = -8; k < 8; k++) {
                    int distSq = i * i + j * j + k * k;
                    if (distSq <= DISTANCE_INNER_SQ)
                        placeAir(world, rand, x + i, y + j, z + k);
                    else if (distSq <= DISTANCE_OUTER_SQ)
                        placeStone(world, rand, x + i, y + j, z + k);
                    if (blockOre != null && distSq > DISTANCE_INNER_SQ && distSq <= DISTANCE_ORE_SQ)
                        placeOre(world, rand, x + i, y + j, z + k);
                }
            }
        }
        return true;
    }

    private void placeAir(World world, Random rand, int x, int y, int z) {
//        if (!world.isBlockLoaded(x, y, z)) {
//            return;
//        }
        if (isReplaceable(world, x, y, z))
            world.setBlock(x, y, z, Blocks.AIR, 0, 2);
    }

    private void placeStone(World world, Random rand, int x, int y, int z) {
//        if (!world.isBlockLoaded(x, y, z)) {
//            return;
//        }
        if (isReplaceable(world, x, y, z))
            world.setBlock(x, y, z, blockStone, meta, 2);
    }

    private void placeOre(World world, Random rand, int x, int y, int z) {
//        if (!world.isBlockLoaded(x, y, z)) {
//            return;
//        }
        if (WorldPlugin.getBlock(world, x, y, z) == blockStone) {
            double chance = rand.nextDouble();
            if (chance <= 0.002 && EnumOre.DARK_DIAMOND.isEnabled())
                world.setBlock(x, y, z, blockOre, EnumOre.DARK_DIAMOND.ordinal(), 2);
            else if (chance <= 0.004 && EnumOre.DARK_EMERALD.isEnabled())
                world.setBlock(x, y, z, blockOre, EnumOre.DARK_EMERALD.ordinal(), 2);
            else if (chance <= 0.01 && EnumOre.DARK_LAPIS.isEnabled())
                world.setBlock(x, y, z, blockOre, EnumOre.DARK_LAPIS.ordinal(), 2);
        }
    }

    private boolean isReplaceable(World world, int x, int y, int z) {
        Block existing = WorldPlugin.getBlock(world, x, y, z);
        if (existing == null)
            return false;
        if (existing.isReplaceableOreGen(world, x, y, z, Blocks.STONE))
            return true;
        if (existing.isReplaceableOreGen(world, x, y, z, Blocks.DIRT))
            return true;
        if (existing.isReplaceableOreGen(world, x, y, z, Blocks.GRAVEL))
            return true;
        if (existing.isReplaceableOreGen(world, x, y, z, Blocks.SAND))
            return true;
        if (existing.getMaterial() == Material.water)
            return true;
        return ores.contains(existing);
    }

}
