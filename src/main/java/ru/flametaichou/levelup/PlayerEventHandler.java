package ru.flametaichou.levelup;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public final class PlayerEventHandler {
    /**
     * Configurable flags related to breaking speed
     */
    public static boolean oldSpeedDigging = true, oldSpeedRedstone = true;
    /**
     * Configurable flags related to player death
     */
    public static float resetSkillOnDeath = 1.00F;
    public static boolean resetClassOnDeath = false;
    /**
     * If duplicated ores can be placed
     */
    public static boolean noPlaceDuplicate = true;
    /**
     * How much each level give in skill points
     */
    public static double xpPerLevel = 3.0D;
    /**
     * Level at which a player can choose a class, and get its first skill points
     */
    public final static int minLevel = 0;
    /**
     * Random additional loot for Fishing
     */
    private static ItemStack[] lootList = new ItemStack[]{new ItemStack(Items.bone), new ItemStack(Items.reeds), new ItemStack(Items.arrow), new ItemStack(Items.apple),
            new ItemStack(Items.bucket), new ItemStack(Items.boat), new ItemStack(Items.ender_pearl), new ItemStack(Items.fishing_rod), new ItemStack(Items.chainmail_chestplate), new ItemStack(Items.iron_ingot)};
    /**
     * Internal ore counter
     */
    private static Map<Block, Integer> blockToCounter = new IdentityHashMap<Block, Integer>();

    static {
        blockToCounter.put(Blocks.coal_ore, 0);
        blockToCounter.put(Blocks.lapis_ore, 1);
        blockToCounter.put(Blocks.redstone_ore, 2);
        blockToCounter.put(Blocks.iron_ore, 3);
        blockToCounter.put(Blocks.gold_ore, 4);
        blockToCounter.put(Blocks.emerald_ore, 5);
        blockToCounter.put(Blocks.diamond_ore, 6);
        blockToCounter.put(Blocks.quartz_ore, 7);
    }

    /**
     * Items given by Digging ground
     */
    private static ItemStack digLoot[] = {new ItemStack(Items.clay_ball, 8), new ItemStack(Items.bowl, 2), new ItemStack(Items.coal, 4), new ItemStack(Items.painting), new ItemStack(Items.stick, 4),
            new ItemStack(Items.string, 2)};
    private static ItemStack digLoot1[] = {new ItemStack(Items.stone_sword), new ItemStack(Items.stone_shovel), new ItemStack(Items.stone_pickaxe), new ItemStack(Items.stone_axe)};
    private static ItemStack digLoot2[] = {new ItemStack(Items.slime_ball, 2), new ItemStack(Items.redstone, 8), new ItemStack(Items.iron_ingot), new ItemStack(Items.gold_ingot)};
    private static ItemStack digLoot3[] = {new ItemStack(Items.diamond)};
    /**
     * Internal ores list for Mining
     */
    private static Set<Block> ores = Sets.newIdentityHashSet();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBreak(PlayerEvent.BreakSpeed event) {
        ItemStack itemstack = event.entityPlayer.getCurrentEquippedItem();
        if (itemstack != null)
            if (oldSpeedDigging && itemstack.getItem() instanceof ItemSpade) {
                if (event.block instanceof BlockDirt || event.block instanceof BlockGravel) {
                    event.newSpeed = event.newSpeed * itemstack.func_150997_a(event.block) / 0.5F;
                }
            } else if (oldSpeedRedstone && itemstack.getItem() instanceof ItemPickaxe && event.block instanceof BlockRedstoneOre) {
                event.newSpeed = event.newSpeed * itemstack.func_150997_a(event.block) / 3F;
            }
        if (event.block instanceof BlockStone || event.block == Blocks.cobblestone || event.block == Blocks.obsidian || (event.block instanceof BlockOre)) {
            event.newSpeed = event.newSpeed + getSkill(event.entityPlayer, 0) / 5 * 0.2F;
        } else if (event.block.getMaterial() == Material.wood) {
            event.newSpeed = event.newSpeed + getSkill(event.entityPlayer, 3) / 5 * 0.2F;
        }
    }
    
    /**
     * Track player deaths to reset values when appropriate,
     * and player final strikes on mobs to give bonus xp
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
        	//resetPlayerHP((EntityPlayer) event.entityLiving);
            if (resetClassOnDeath) {
                PlayerExtendedProperties.from((EntityPlayer) event.entityLiving).setPlayerClass((byte) 0);
            }
            if (resetSkillOnDeath > 0.00F) {
                PlayerExtendedProperties.from((EntityPlayer) event.entityLiving).takeSkillFraction(resetSkillOnDeath);
            }
        } else if (event.entityLiving instanceof EntityMob && event.source.getEntity() instanceof EntityPlayer) {
            LevelUp.giveBonusFightingXP((EntityPlayer) event.source.getEntity());
        }
    }
    
    /*
    public void resetPlayerHP(EntityPlayer player) {
    	player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(player.getMaxHealth());
    }
     */
    
    /**
     * Change fishing by adding some loots
     * Prevent flagged block placement
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        if (event.useItem != Event.Result.DENY)
            if (event.action == Action.RIGHT_CLICK_AIR) {
                EntityFishHook hook = event.entityPlayer.fishEntity;
                //if (hook != null && hook.field_146043_c == null && hook.field_146045_ax > 0)
                if (hook != null && hook.field_146043_c == null) {//Not attached to some random stuff, and within the time frame for catching
                    int loot = getFishingLoot(event.entityPlayer);
                    if (loot >= 0) {
                        ItemStack stack = event.entityPlayer.inventory.getCurrentItem();
                        int i = stack.stackSize;
                        int j = stack.getItemDamage();
                        stack.damageItem(loot, event.entityPlayer);
                        event.entityPlayer.swingItem();
                        event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem, stack);
                        if (event.entityPlayer.capabilities.isCreativeMode) {
                            stack.stackSize = i;
                            if (stack.isItemStackDamageable()) {
                                stack.setItemDamage(j);
                            }
                        }
                        if (stack.stackSize <= 0) {
                            event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem, null);
                            MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(event.entityPlayer, stack));
                        }
                        if (!event.entityPlayer.isUsingItem() && event.entityPlayer instanceof EntityPlayerMP) {
                            ((EntityPlayerMP) event.entityPlayer).sendContainerToPlayer(event.entityPlayer.inventoryContainer);
                        }
                        event.useItem = Event.Result.DENY;
                        if (!hook.worldObj.isRemote) {
                            EntityItem entityitem = new EntityItem(hook.worldObj, hook.posX, hook.posY, hook.posZ, lootList[loot]);
                            double d5 = hook.field_146042_b.posX - hook.posX;
                            double d6 = hook.field_146042_b.posY - hook.posY;
                            double d7 = hook.field_146042_b.posZ - hook.posZ;
                            double d8 = MathHelper.sqrt_double(d5 * d5 + d6 * d6 + d7 * d7);
                            double d9 = 0.1D;
                            entityitem.motionX = d5 * d9;
                            entityitem.motionY = d6 * d9 + MathHelper.sqrt_double(d8) * 0.08D;
                            entityitem.motionZ = d7 * d9;
                            hook.worldObj.spawnEntityInWorld(entityitem);
                            hook.field_146042_b.worldObj.spawnEntityInWorld(new EntityXPOrb(hook.field_146042_b.worldObj, hook.field_146042_b.posX, hook.field_146042_b.posY + 0.5D, hook.field_146042_b.posZ + 0.5D, event.entityPlayer.getRNG().nextInt(6) + 1));
                        }
                    }
                }
            } else if (event.action == Action.RIGHT_CLICK_BLOCK && noPlaceDuplicate && !event.world.isRemote) {
                ItemStack itemStack = event.entityPlayer.inventory.getCurrentItem();
                if (itemStack != null && itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("NoPlacing")) {
                	//event.useItem = Event.Result.DENY;
                	event.setCanceled(true);
                }
            }
    }
    
    @SubscribeEvent
    public void onPickupXP(PlayerPickupXpEvent event) {
    	LevelUp.giveBonusRandomXP(event.entityPlayer);
    }
    

    @SubscribeEvent
    public void onHarvest(BlockEvent.HarvestDropsEvent event) {
        if (event.harvester != null && !event.world.isRemote) {
            int skill;
            Random random = event.harvester.getRNG();
            if (event.block instanceof BlockOre || event.block instanceof BlockRedstoneOre || ores.contains(event.block)) {
                skill = getSkill(event.harvester, 0);
                if (!blockToCounter.containsKey(event.block)) {
                    blockToCounter.put(event.block, blockToCounter.size());
                }
                if (!event.isSilkTouching)
                    LevelUp.incrementOreCounter(event.harvester, blockToCounter.get(event.block));
                if (random.nextDouble() <= skill / 200D) {
                    boolean foundBlock = false;
                    for (ItemStack stack : event.drops) {
                        if (stack != null && event.block == Block.getBlockFromItem(stack.getItem())) {
                            writeNoPlacing(stack);
                            stack.stackSize += 1;
                            foundBlock = true;
                            break;
                        }
                    }
                    if (!foundBlock) {
                        Item ID = event.block.getItemDropped(event.blockMetadata, random, 0);
                        if (ID != null) {
                            int qutity = event.block.quantityDropped(event.blockMetadata, 0, random);
                            if (qutity > 0)
                                event.drops.add(new ItemStack(ID, qutity, event.block.damageDropped(event.blockMetadata)));
                        }
                    }
                }
            } else if (event.block instanceof BlockLog) {
                skill = getSkill(event.harvester, 3);
                if (random.nextDouble() <= skill / 150D) {
                    ItemStack planks = null;
                    for (ItemStack stack : event.drops) {
                        if (stack != null && event.block == Block.getBlockFromItem(stack.getItem())) {
                            planks = getPlanks(event.harvester, event.block, event.blockMetadata, stack.copy());
                            break;
                        }
                    }
                    if (planks != null)
                        event.drops.add(planks);
                }
                if (random.nextDouble() <= skill / 150D) {
                    event.drops.add(new ItemStack(Items.stick, 2));
                }
            } else if (event.block.getMaterial() == Material.ground) {
                skill = getSkill(event.harvester, 11);
                if (random.nextFloat() <= skill / 200F) {
                    ItemStack[] aitemstack4 = digLoot;
                    float f = random.nextFloat();
                    if (f <= 0.002F) {
                        aitemstack4 = digLoot3;
                    } else {
                        if (f <= 0.1F) {
                            aitemstack4 = digLoot2;
                        } else if (f <= 0.4F) {
                            aitemstack4 = digLoot1;
                        }
                    }
                    removeFromList(event.drops, event.block);
                    ItemStack itemstack = aitemstack4[random.nextInt(aitemstack4.length)];
                    final int size = itemstack.stackSize;
                    ItemStack toDrop = itemstack.copy();
                    toDrop.stackSize = 1;
                    if (toDrop.getMaxDamage() > 20) {
                        toDrop.setItemDamage(random.nextInt(80) + 20);
                    } else {
                        for (int i1 = 0; i1 < size - 1; i1++) {
                            if (random.nextFloat() < 0.5F) {
                                event.drops.add(toDrop.copy());
                            }
                        }
                    }
                    event.drops.add(toDrop);
                }
            } else if (event.block instanceof BlockGravel) {
                skill = getSkill(event.harvester, 11);
                if (random.nextInt(10) < skill / 5) {
                    removeFromList(event.drops, event.block);
                    event.drops.add(new ItemStack(Items.flint));
                }
            }
        }
    }

    private void removeFromList(ArrayList<ItemStack> drops, Block block) {
        Iterator<ItemStack> itr = drops.iterator();
        while (itr.hasNext()) {
            ItemStack drop = itr.next();
            if (drop != null && block == Block.getBlockFromItem(drop.getItem())) {
                itr.remove();
            }
        }
    }

    /**
     * Convenience method to write the "no-placement" flag onto a block
     */
    private void writeNoPlacing(ItemStack toDrop) {
        if (!noPlaceDuplicate)
            return;
        NBTTagCompound tagCompound = toDrop.getTagCompound();
        if (tagCompound == null)
            tagCompound = new NBTTagCompound();
        tagCompound.setBoolean("NoPlacing", true);
        toDrop.setTagCompound(tagCompound);
    }

    /**
     * Converts a log block into craftable planks, if possible
     *
     * @return default planks if no crafting against the log is possible
     */
    private ItemStack getPlanks(EntityPlayer player, Block block, int meta, ItemStack drop) {
        if (block != Blocks.log) {
            InventoryCrafting craft = new ContainerPlayer(player.inventory, !player.worldObj.isRemote, player).craftMatrix;
            craft.setInventorySlotContents(1, drop);
            ItemStack planks = CraftingManager.getInstance().findMatchingRecipe(craft, player.worldObj);
            if (planks != null) {
                planks.stackSize = 2;
                return planks;
            }
        }
        return new ItemStack(Blocks.planks, 2, meta & 3);
    }

    /**
     * Adds additional drops for Farming when breaking crops
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!event.world.isRemote && event.getPlayer() != null) {
            if (event.block instanceof BlockCrops || event.block instanceof BlockStem) {//BlockNetherWart ?
                if(!((IGrowable)event.block).func_149851_a(event.world, event.x, event.y, event.z, false)) {//Fully grown
                    doCropDrops(event);
                }
            }else if(event.block instanceof BlockMelon){
                doCropDrops(event);
            }
        }
    }

    private void doCropDrops(BlockEvent.BreakEvent event){
        Random random = event.getPlayer().getRNG();
        int skill = getSkill(event.getPlayer(), 9);
        if (random.nextInt(10) < skill / 5) {
            Item ID = event.block.getItemDropped(event.blockMetadata, random, 0);
            if(ID == null){
                if(event.block == Blocks.pumpkin_stem){
                    ID = Items.pumpkin_seeds;
                }else if(event.block == Blocks.melon_stem){
                    ID = Items.melon_seeds;
                }
            }
            if (ID != null)
                event.world.spawnEntityInWorld(new EntityItem(event.world, event.x, event.y, event.z, new ItemStack(ID, 1, event.block.damageDropped(event.blockMetadata))));
        }
    }

    /**
     * Register base skill data to players
     */
    @SubscribeEvent
    public void onPlayerConstruction(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            IExtendedEntityProperties skills = event.entity.getExtendedProperties(ClassBonus.SKILL_ID);
            if (skills == null) {
                skills = new PlayerExtendedProperties();
                event.entity.registerExtendedProperties(ClassBonus.SKILL_ID, skills);
            }
        }
    }

    /**
     * Copy skill data when needed
     */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.wasDeath || !resetClassOnDeath || resetSkillOnDeath < 1.00F) {
            NBTTagCompound data = new NBTTagCompound();
            PlayerExtendedProperties.from(event.original).saveNBTData(data);
            PlayerExtendedProperties.from(event.entityPlayer).loadNBTData(data);
        }
    }

    /**
     * Keep track of registered ores blocks, for mining xp compatibility
     */
    @SubscribeEvent
    public void onOreRegister(OreDictionary.OreRegisterEvent event) {
        if (event.Name.startsWith("ore") && event.Ore != null && event.Ore.getItem() != null) {
            Block ore = Block.getBlockFromItem(event.Ore.getItem());
            if (ore != Blocks.air && !(ore instanceof BlockOre || ore instanceof BlockRedstoneOre)) {
                ores.add(ore);
            }
        }
    }

    /**
     * Helper to get a random slot value for the fish drop list
     *
     * @return -1 if no drop is required
     */
    public static int getFishingLoot(EntityPlayer player) {
        if (player.getRNG().nextDouble() > (getSkill(player, 10) / 5) * 0.05D) {
            return -1;
        } else {
            return player.getRNG().nextInt(lootList.length);
        }
    }

    /**
     * Helper to retrieve skill points from the index
     */
    public static int getSkill(EntityPlayer player, int id) {
        return PlayerExtendedProperties.getSkillFromIndex(player, id);
    }
}
