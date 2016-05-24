/*******************************************************************************
 * Copyright (c) CovertJaguar, 2011-2016
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 ******************************************************************************/
package mods.railcraft.common.modules;

import mods.railcraft.api.carts.CartTools;
import mods.railcraft.api.core.RailcraftModule;
import mods.railcraft.api.crafting.RailcraftCraftingManager;
import mods.railcraft.api.fuel.FuelManager;
import mods.railcraft.api.helpers.Helpers;
import mods.railcraft.api.signals.SignalTools;
import mods.railcraft.client.sounds.SoundLimiterTicker;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.blocks.aesthetics.cube.EnumCube;
import mods.railcraft.common.blocks.machine.IEnumMachine;
import mods.railcraft.common.blocks.machine.MachineTileRegistery;
import mods.railcraft.common.blocks.machine.MultiBlockHelper;
import mods.railcraft.common.blocks.machine.alpha.EnumMachineAlpha;
import mods.railcraft.common.blocks.machine.beta.EnumMachineBeta;
import mods.railcraft.common.blocks.machine.delta.EnumMachineDelta;
import mods.railcraft.common.blocks.machine.epsilon.EnumMachineEpsilon;
import mods.railcraft.common.blocks.machine.gamma.EnumMachineGamma;
import mods.railcraft.common.blocks.signals.EnumSignal;
import mods.railcraft.common.blocks.tracks.BlockTrack;
import mods.railcraft.common.carts.*;
import mods.railcraft.common.commands.CommandAdmin;
import mods.railcraft.common.commands.CommandDebug;
import mods.railcraft.common.core.Railcraft;
import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.fluids.*;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.items.CrowbarHandler;
import mods.railcraft.common.items.EntityItemFireproof;
import mods.railcraft.common.items.ItemRail.EnumRail;
import mods.railcraft.common.items.ItemRailbed.EnumRailbed;
import mods.railcraft.common.items.RailcraftItems;
import mods.railcraft.common.items.RailcraftToolItems;
import mods.railcraft.common.items.enchantment.RailcraftEnchantments;
import mods.railcraft.common.plugins.buildcraft.BuildcraftPlugin;
import mods.railcraft.common.plugins.forge.CraftingPlugin;
import mods.railcraft.common.plugins.forge.FuelPlugin;
import mods.railcraft.common.plugins.forge.LootPlugin;
import mods.railcraft.common.plugins.forge.RailcraftRegistry;
import mods.railcraft.common.util.crafting.*;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.misc.RailcraftDamageSource;
import mods.railcraft.common.util.network.PacketBuilder;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.Level;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@RailcraftModule("core")
public class ModuleCore extends RailcraftModulePayload {
    private static void addLiquidFuels() {
        int bioHeat = (int) (16000 * RailcraftConfig.boilerBiofuelMultiplier());
        Fluid ethanol = Fluids.BIOETHANOL.get();
        if (ethanol != null)
            FuelManager.addBoilerFuel(ethanol, bioHeat); // Biofuel

        Fluid biofuel = Fluids.BIOFUEL.get();
        if (biofuel != null)
            FuelManager.addBoilerFuel(biofuel, bioHeat); // Biofuel

        Fluid fuel = Fluids.FUEL.get();
        if (fuel != null)
            FuelManager.addBoilerFuel(fuel, (int) (48000 * RailcraftConfig.boilerFuelMultiplier())); // Fuel

        Fluid coal = Fluids.COAL.get();
        if (coal != null)
            FuelManager.addBoilerFuel(coal, (int) (32000 * RailcraftConfig.boilerFuelMultiplier())); // Liquefaction Coal

        Fluid pyrotheum = Fluids.PYROTHEUM.get();
        if (pyrotheum != null)
            FuelManager.addBoilerFuel(pyrotheum, (int) (64000 * RailcraftConfig.boilerFuelMultiplier())); // Blazing Pyrotheum

        Fluid creosote = Fluids.CREOSOTE.get();
        if (creosote != null)
            FuelManager.addBoilerFuel(creosote, 4800); // Creosote
    }

    public ModuleCore() {
        setEnabledEventHandler(new ModuleEventHandler() {
            @Override
            public void construction() {
                LinkageManager.reset();
                CartTools.transferHelper = TrainTransferHelper.INSTANCE;

                Railcraft.rootCommand.addChildCommand(new CommandDebug());
                Railcraft.rootCommand.addChildCommand(new CommandAdmin());

                RailcraftCraftingManager.cokeOven = new CokeOvenCraftingManager();
                RailcraftCraftingManager.blastFurnace = new BlastFurnaceCraftingManager();
                RailcraftCraftingManager.rockCrusher = new RockCrusherCraftingManager();
                RailcraftCraftingManager.rollingMachine = new RollingMachineCraftingManager();

                SignalTools.packetBuilder = PacketBuilder.instance();

                RailcraftFluids.preInitFluids();
                MinecraftForge.EVENT_BUS.register(RailcraftFluids.getTextureHook());
                MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);
                MinecraftForge.EVENT_BUS.register(RailcraftDamageSource.EVENT_HANDLER);

                Helpers.structures = new MultiBlockHelper();

                EntityItemFireproof.register();

                RecipeSorter.register("railcraft:rotor.repair", RotorRepairRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
                RecipeSorter.register("railcraft:locomotive.painting", LocomotivePaintingRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
                RecipeSorter.register("railcraft:routing.table.copy", RoutingTableCopyRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
                RecipeSorter.register("railcraft:routing.ticket.copy", RoutingTicketCopyRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
                RecipeSorter.register("railcraft:cart.filter", CartFilterRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

                OreDictionary.registerOre("chestWood", Blocks.CHEST);
                OreDictionary.registerOre("craftingTableWood", Blocks.CRAFTING_TABLE);

                add(
                        RailcraftItems.crowbarIron,
                        RailcraftItems.crowbarSteel,
                        RailcraftItems.magGlass,
                        RailcraftItems.goggles,
                        RailcraftItems.overalls,
                        RailcraftItems.nugget,
                        RailcraftItems.notepad
                );
            }

            @Override
            public void preInit() {
                NetworkRegistry.INSTANCE.registerGuiHandler(Railcraft.getMod(), new GuiHandler());

                LootPlugin.init();

                RailcraftEnchantments.registerEnchantment();

                RailcraftToolItems.initializeToolsArmor();

                EntityEnderman.setCarriable(Blocks.GRAVEL, false);

                BuildcraftPlugin.init();


                MinecraftForge.EVENT_BUS.register(CrowbarHandler.instance());
                MinecraftForge.EVENT_BUS.register(MinecartHooks.getInstance());
                MinecraftForge.EVENT_BUS.register(LinkageHandler.getInstance());
                MinecraftForge.EVENT_BUS.register(new CraftingHandler());
                MinecraftForge.EVENT_BUS.register(new SoundLimiterTicker());
                MinecraftForge.EVENT_BUS.register(new Object() {
                    @SubscribeEvent
                    public void logout(PlayerEvent.PlayerLoggedOutEvent event) {
                        if (event.player.ridingEntity instanceof EntityMinecart) {
                            Entity p = event.player;
                            EntityMinecart cart = (EntityMinecart) event.player.ridingEntity;
                            if (Train.getTrain(cart).size() > 1)
                                CartUtils.dismount(cart, p.posX, p.posY + 1, p.posZ);
                        }
                    }
                });

                if (RailcraftConfig.useCollisionHandler()) {
                    if (EntityMinecart.getCollisionHandler() != null)
                        Game.log(Level.WARN, "Existing Minecart Collision Handler detected, overwriting. Please check your configs to ensure this is desired behavior.");
                    EntityMinecart.setCollisionHandler(MinecartHooks.getInstance());
                }

                Set<Item> testSet = new HashSet<Item>();
                if (!RailcraftConfig.useOldRecipes()) {
                    testSet.add(Item.getItemFromBlock(Blocks.RAIL));
                    testSet.add(Item.getItemFromBlock(Blocks.GOLDEN_RAIL));
                    testSet.add(Item.getItemFromBlock(Blocks.DETECTOR_RAIL));
                    testSet.add(Item.getItemFromBlock(Blocks.ACTIVATOR_RAIL));
                }

                if (!RailcraftConfig.getRecipeConfig("railcraft.cart.vanilla.furnace"))
                    testSet.add(Items.FURNACE_MINECART);

//        MiscTools.addShapelessRecipe(new ItemStack(Item.coal, 20), Block.dirt);
                Iterator it = CraftingManager.getInstance().getRecipeList().iterator();
                while (it.hasNext()) {
                    IRecipe r = (IRecipe) it.next();
                    ItemStack output = null;
                    try {
                        output = r.getRecipeOutput();
                    } catch (Exception ignored) {
                    }
                    if (output != null)
                        if (testSet.contains(output.getItem()))
                            it.remove();
                }

                // Items
                replaceVanillaCart(EnumCart.COMMAND_BLOCK, Items.COMMAND_BLOCK_MINECART, "MinecartCommandBlock", 40);
                Items.COMMAND_BLOCK_MINECART.setCreativeTab(CreativeTabs.tabTransport);
                replaceVanillaCart(EnumCart.BASIC, Items.MINECART, "MinecartRideable", 42);
                replaceVanillaCart(EnumCart.CHEST, Items.CHEST_MINECART, "MinecartChest", 43);
                replaceVanillaCart(EnumCart.FURNACE, Items.FURNACE_MINECART, "MinecartFurnace", 44);
                replaceVanillaCart(EnumCart.TNT, Items.TNT_MINECART, "MinecartTNT", 45);
                replaceVanillaCart(EnumCart.HOPPER, Items.HOPPER_MINECART, "MinecartHopper", 46);

                LootPlugin.addLoot(EnumCart.BASIC.getCartItem(), 1, 1, LootPlugin.Type.RAILWAY, "cart.basic");
                LootPlugin.addLoot(EnumCart.CHEST.getCartItem(), 1, 1, LootPlugin.Type.RAILWAY, "cart.chest");
                LootPlugin.addLoot(EnumCart.TNT.getCartItem(), 1, 3, LootPlugin.Type.RAILWAY, "cart.tnt");
                LootPlugin.addLoot(new ItemStack(Blocks.RAIL), 8, 32, LootPlugin.Type.RAILWAY, "track.basic");
                LootPlugin.addLoot(EnumCart.HOPPER.getCartItem(), 1, 1, LootPlugin.Type.RAILWAY, "cart.hopper");

                float h = BlockTrack.HARDNESS;
                Blocks.RAIL.setHardness(h).setHarvestLevel("crowbar", 0);
                Blocks.GOLDEN_RAIL.setHardness(h).setHarvestLevel("crowbar", 0);
                Blocks.DETECTOR_RAIL.setHardness(h).setHarvestLevel("crowbar", 0);
                Blocks.ACTIVATOR_RAIL.setHardness(h).setHarvestLevel("crowbar", 0);

                // Define Recipes
                if (RailcraftConfig.getRecipeConfig("railcraft.cart.bronze")) {
                    IRecipe recipe = new ShapedOreRecipe(new ItemStack(Items.MINECART), false,
                            "I I",
                            "III",
                            'I', "ingotBronze");
                    CraftingPlugin.addRecipe(recipe);
                }

                if (RailcraftConfig.getRecipeConfig("railcraft.cart.steel")) {
                    IRecipe recipe = new ShapedOreRecipe(new ItemStack(Items.MINECART, 2), false,
                            "I I",
                            "III",
                            'I', "ingotSteel");
                    CraftingPlugin.addRecipe(recipe);
                }

                // Old rails
                if (!RailcraftConfig.useOldRecipes()) {
                    ItemStack stackRailNormal = new ItemStack(Blocks.RAIL, 32);
                    ItemStack stackRailBooster = new ItemStack(Blocks.GOLDEN_RAIL, 16);
                    ItemStack stackRailDetector = new ItemStack(Blocks.DETECTOR_RAIL, 16);
                    ItemStack stackRailActivator = new ItemStack(Blocks.ACTIVATOR_RAIL, 16);

                    Object woodRailbed = RailcraftItems.railbed.getRecipeObject(EnumRailbed.WOOD);
                    CraftingPlugin.addRecipe(stackRailNormal,
                            "I I",
                            "I#I",
                            "I I",
                            'I', RailcraftItems.rail.getRecipeObject(EnumRail.STANDARD),
                            '#', woodRailbed);
                    CraftingPlugin.addRecipe(stackRailBooster,
                            "I I",
                            "I#I",
                            "IrI",
                            'I', RailcraftItems.rail.getRecipeObject(EnumRail.ADVANCED),
                            '#', woodRailbed,
                            'r', "dustRedstone");
                    CraftingPlugin.addRecipe(stackRailDetector,
                            "IsI",
                            "I#I",
                            "IrI",
                            'I', RailcraftItems.rail.getRecipeObject(EnumRail.STANDARD),
                            '#', Blocks.STONE_PRESSURE_PLATE,
                            'r', "dustRedstone",
                            's', woodRailbed);
                    CraftingPlugin.addRecipe(stackRailActivator,
                            "ItI",
                            "I#I",
                            "ItI",
                            'I', RailcraftItems.rail.getRecipeObject(EnumRail.STANDARD),
                            '#', woodRailbed,
                            't', new ItemStack(Blocks.REDSTONE_TORCH));

                    CraftingPlugin.addShapelessRecipe(RailcraftItems.rail.getStack(1, EnumRail.STANDARD),
                            Blocks.RAIL,
                            Blocks.RAIL,
                            Blocks.RAIL,
                            Blocks.RAIL,
                            Blocks.RAIL,
                            Blocks.RAIL,
                            Blocks.RAIL,
                            Blocks.RAIL);
                }

                MachineTileRegistery.registerTileEntities();
            }

            private void replaceVanillaCart(EnumCart cartType, Item original, String entityTag, int entityId) {
                cartType.registerEntity();

                Class<? extends Entity> minecartClass = EntityList.stringToClassMapping.remove(entityTag);

                CartUtils.classReplacements.put(minecartClass, cartType);
                CartUtils.vanillaCartItemMap.put(original, cartType);

                EntityList.idToClassMapping.remove(entityId);
                EntityList.addMapping(cartType.getCartClass(), entityTag, entityId);

                BlockDispenser.dispenseBehaviorRegistry.putObject(original, new BehaviorDefaultDispenseItem());

                original.setMaxStackSize(RailcraftConfig.getMinecartStackSize());
                cartType.setCartItem(new ItemStack(original));
            }

            @Override
            public void init() {
                if (RailcraftConfig.useCreosoteFurnaceRecipes() || !EnumMachineAlpha.COKE_OVEN.isAvailable()) {
                    CraftingPlugin.addFurnaceRecipe(new ItemStack(Items.COAL, 1, 0), FluidContainers.getCreosoteOilBottle(2), 0.0F);
                    CraftingPlugin.addFurnaceRecipe(new ItemStack(Items.COAL, 1, 1), FluidContainers.getCreosoteOilBottle(1), 0.0F);
                }

                // Finish initializing ItemRegistry
                for (EnumSignal type : EnumSignal.values()) {
                    if (type.isEnabled())
                        RailcraftRegistry.register(type.getItem());
                }

                for (EnumCube type : EnumCube.values()) {
                    if (type.isEnabled())
                        RailcraftRegistry.register(type.getItem());
                }

                Set<IEnumMachine> machines = new HashSet<IEnumMachine>();
                machines.addAll(EnumSet.allOf(EnumMachineAlpha.class));
                machines.addAll(EnumSet.allOf(EnumMachineBeta.class));
                machines.addAll(EnumSet.allOf(EnumMachineGamma.class));
                machines.addAll(EnumSet.allOf(EnumMachineDelta.class));
                machines.addAll(EnumSet.allOf(EnumMachineEpsilon.class));

                for (IEnumMachine machine : machines) {
                    if (machine.isAvailable())
                        RailcraftRegistry.register(machine.getItem());
                }
            }

            @Override
            public void postInit() {
                RailcraftFluids.postInitFluids();
                RailcraftBlocks.definePostRecipes();
                RailcraftItems.definePostRecipes();

                GameRegistry.registerFuelHandler(FuelPlugin.getFuelHandler());

                addLiquidFuels();

                FluidHelper.nerfWaterBottle();

//----------------------------------------------
// Boiler Test Setup
// ---------------------------------------------
//        StandardTank tankWater = new StandardTank(FluidHelper.BUCKET_VOLUME * 1000);
//        StandardTank tankSteam = new StandardTank(FluidHelper.BUCKET_VOLUME * 1000);
//        tankWater.setFluid(Fluids.WATER.get(tankWater.getCapacity()));
//        SteamBoiler boiler = new SteamBoiler(tankWater, tankSteam);
//        class TestProvider implements IFuelProvider {
//
//            public int fuel = 3200;
//
//            @Override
//            public double getMoreFuel() {
//                if (fuel > 0) {
//                    fuel--;
//                    return 1;
//                }
//                return 0;
//            }
//
//            @Override
//            public double getHeatStep() {
//                return Steam.HEAT_STEP;
//            }
//
//        }
//        TestProvider provider = new TestProvider();
//        boiler.setFuelProvider(provider);
//        int ticks = 0;
//        while (provider.fuel > 0 || boiler.burnTime > boiler.getFuelPerCycle(1) || boiler.getHeat() > 20) {
//            boiler.tick(1);
//            ticks++;
//        }
//        System.out.printf("Ran for %d ticks.%n", ticks);
//        System.out.printf("Steam Produced=%s%n", tankSteam.getFluidAmount());
//        System.exit(0);
            }
        });
    }

}
