/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.signals;

import mods.railcraft.common.blocks.ItemBlockRailcraftMultiType;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemSignal extends ItemBlockRailcraftMultiType {

    public ItemSignal(Block block) {
        super(block);
        setUnlocalizedName("railcraft.signal");
    }

    public ISignalTileDefinition getStructureType(ItemStack stack) {
        return EnumSignal.fromOrdinal(stack.getItemDamage());
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return getStructureType(stack).getTag();
    }

    @Override
    public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {
        Block oldBlock = world.getBlock(x, y, z);

        if (oldBlock == Blocks.SNOW_LAYER)
            side = 1;
        else if (oldBlock != Blocks.VINE && oldBlock != Blocks.TALLGRASS && oldBlock != Blocks.DEADBUSH && !oldBlock.isReplaceable(world, x, y, z)) {
            if (side == 0)
                --y;

            if (side == 1)
                ++y;

            if (side == 2)
                --z;

            if (side == 3)
                ++z;

            if (side == 4)
                --x;

            if (side == 5)
                ++x;
        }

        return world.canPlaceEntityOnSide(field_150939_a, x, y, z, false, side, (Entity) null, stack) && (!getStructureType(stack).needsSupport() || world.isSideSolid(x, y - 1, z, EnumFacing.UP));
    }
}
