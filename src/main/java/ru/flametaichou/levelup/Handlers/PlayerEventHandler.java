package ru.flametaichou.levelup.Handlers;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.FishingHooks;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.oredict.OreDictionary;
import ru.flametaichou.levelup.ClassBonus;
import ru.flametaichou.levelup.Items.ItemExpOrb;
import ru.flametaichou.levelup.LevelUp;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.PlayerExtendedProperties;
import ru.flametaichou.levelup.Util.ConfigHelper;

import java.util.*;

public final class PlayerEventHandler {
    /**
     * Configurable flags related to breaking speed
     */
    public static boolean oldSpeedDigging = true, oldSpeedRedstone = true;
    /**
     * Random additional loot for Fishing
     */
    private static List<String> bonusFishingLootList;

    /**
     * Items given by Digging ground
     */
    private static ItemStack digLoot[] = {new ItemStack(Items.clay_ball), new ItemStack(Items.coal)};
    private static ItemStack digLoot1[] = {new ItemStack(Items.gunpowder)};
    private static ItemStack digLoot2[] = {new ItemStack(Items.slime_ball), new ItemStack(Items.redstone)};
    private static ItemStack digLoot3[] = {new ItemStack(Items.glowstone_dust),new ItemStack(Items.gold_nugget)};
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
            event.newSpeed = event.newSpeed + getSkill(event.entityPlayer, PlayerSkill.MINING) / 5 * 0.2F;
        }
//        else if (event.block.getMaterial() == Material.wood) {
//            event.newSpeed = event.newSpeed + getSkill(event.entityPlayer, 3) / 5 * 0.2F;
//        }
    }

    /**
     * Track player deaths to reset values when appropriate,
     * and player final strikes on mobs to give bonus xp
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
        	//resetPlayerHP((EntityPlayer) event.entityLiving);
            if (ConfigHelper.resetClassOnDeath) {
                PlayerExtendedProperties.from((EntityPlayer) event.entityLiving).setPlayerClass(PlayerClass.NONE);
            }
            if (ConfigHelper.resetSkillsOnDeath) {
                PlayerExtendedProperties.from((EntityPlayer) event.entityLiving).convertSkillsToSkillPoints(false);
            }
            if (ConfigHelper.percentSkillOnDeath > 0) {
                PlayerExtendedProperties.from((EntityPlayer) event.entityLiving).takeSkillPointsFromPlayer(ConfigHelper.percentSkillOnDeath);
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
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.world.getBlock(event.x,event.y,event.z);
            // Сохраняем данные для Кузнечного дела
            if (block == Blocks.anvil && event.world.isRemote) {
                PlayerExtendedProperties.from(event.entityPlayer).saveLatestExp(event.entityPlayer.experienceLevel);
            }
        }

        if (event.useItem != Event.Result.DENY)
            /*
            * Fishing skill bonus
            */
            if (event.action == Action.RIGHT_CLICK_AIR) {
                EntityFishHook hook = event.entityPlayer.fishEntity;
                // Происходит при вытаскивании удочки
                if (hook != null && hook.field_146043_c == null) {
                    int loot = getBonusFishingLoot(event.entityPlayer);
                    Random random = new Random();
                    if (!hook.worldObj.isRemote) {
                        // Проверяем "победу". Гениально!
                        if (hook.isSneaking()) {
                            EntityItem entityitem = new EntityItem(hook.worldObj, hook.posX, hook.posY, hook.posZ, FishingHooks.getRandomFishable(random,1));
                            double d1 = hook.field_146042_b.posX - hook.posX;
                            double d3 = hook.field_146042_b.posY - hook.posY;
                            double d5 = hook.field_146042_b.posZ - hook.posZ;
                            double d7 = (double) MathHelper.sqrt_double(d1 * d1 + d3 * d3 + d5 * d5);
                            double d9 = 0.1D;
                            entityitem.motionX = d1 * d9;
                            entityitem.motionY = d3 * d9 + (double) MathHelper.sqrt_double(d7) * 0.08D;
                            entityitem.motionZ = d5 * d9;
                            hook.worldObj.spawnEntityInWorld(entityitem);

                            // Здесь добавляем к пойманному луту бонусный
                            EntityItem bonusLoot = null;
                            if (loot >= 0)
                                bonusLoot = new EntityItem(hook.worldObj, hook.posX, hook.posY, hook.posZ, new ItemStack (Item.getItemById(Integer.parseInt(bonusFishingLootList.get(loot)))));
                            if (bonusLoot != null) {
                                bonusLoot.motionX = d1 * d9;
                                bonusLoot.motionY = d3 * d9 + (double) MathHelper.sqrt_double(d7) * 0.08D;
                                bonusLoot.motionZ = d5 * d9;
                                hook.worldObj.spawnEntityInWorld(bonusLoot);
                                event.entityPlayer.addChatComponentMessage(new ChatComponentTranslation("rare.fish"));
                            }

                            // Peasant class bonus
                            bonusLoot = null;
                            PlayerClass pClass = PlayerExtendedProperties.getPlayerClass(event.entityPlayer);
                            if (pClass == PlayerClass.PEASANT) {
                                double d = Math.random();
                                if (d < 0.03) {
                                    bonusLoot = new EntityItem(hook.worldObj, hook.posX, hook.posY, hook.posZ, new ItemStack(LevelUp.fishingLootBox));
                                    bonusLoot.motionX = d1 * d9;
                                    bonusLoot.motionY = d3 * d9 + (double) MathHelper.sqrt_double(d7) * 0.08D;
                                    bonusLoot.motionZ = d5 * d9;
                                    hook.worldObj.spawnEntityInWorld(bonusLoot);
                                    event.entityPlayer.addChatComponentMessage(new ChatComponentTranslation("fishing.lootbox"));
                                }
                            }
                            hook.field_146042_b.worldObj.spawnEntityInWorld(new EntityXPOrb(hook.field_146042_b.worldObj, hook.field_146042_b.posX, hook.field_146042_b.posY + 0.5D, hook.field_146042_b.posZ + 0.5D, random.nextInt(6) + 1));
                            LevelUp.giveBonusFishingXP(event.entityPlayer);
                        }
                    }

                    //Тут ломаем удочку
                    ItemStack item = event.entityPlayer.getHeldItem();
                    int damage = item.getItemDamage();
                    //item.damageItem(1, event.entityPlayer);
                    if (!event.entityPlayer.capabilities.isCreativeMode) {
                        if (item.isItemStackDamageable())
                            item.setItemDamage(damage+1);
                    }
//                    if (stack.stackSize <= 0) {
//                        event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem, null);
//                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(event.entityPlayer, stack));
//                    }
//                    if (!event.entityPlayer.isUsingItem() && event.entityPlayer instanceof EntityPlayerMP) {
//                         (EntityPlayerMP) event.entityPlayer).sendContainerToPlayer(event.entityPlayer.inventoryContainer);
//                    }
//                    event.useItem = Event.Result.DENY;
                }
            } else if (event.action == Action.RIGHT_CLICK_BLOCK && !event.world.isRemote) {
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

                int expDrop = event.block.getExpDrop(event.world, event.blockMetadata, event.fortuneLevel);
                LevelUp.giveBonusMiningXP(event.harvester, expDrop / 3);

                skill = getSkill(event.harvester, PlayerSkill.MINING);
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
            } else if (event.block.getMaterial() == Material.ground) {
                skill = getSkill(event.harvester, PlayerSkill.MINING);
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

    @SubscribeEvent
    public void onTooltipItem(ItemTooltipEvent event) {
        if (event.itemStack.getTagCompound() != null) {
            if (event.itemStack.getTagCompound().getString("Smith") != "") {
                String lore = StatCollector.translateToLocal("tooltip.smith") + " " + event.itemStack.getTagCompound().getString("Smith");
                event.toolTip.add(lore);
            }
            if (event.itemStack.getTagCompound().getInteger("BonusDamage") != 0) {
                String lore = StatCollector.translateToLocal("tooltip.damage") + " " + event.itemStack.getTagCompound().getInteger("BonusDamage");
                event.toolTip.add(lore);
            }
            if (event.itemStack.getTagCompound().getInteger("BonusCrit") != 0) {
                String lore = StatCollector.translateToLocal("tooltip.crit") + " " + event.itemStack.getTagCompound().getInteger("BonusCrit") + "%";
                event.toolTip.add(lore);
            }
            if (event.itemStack.getTagCompound().getString("Owner") != "") {
                String lore = StatCollector.translateToLocal("tooltip.owner") + " " + event.itemStack.getTagCompound().getString("Owner");
                event.toolTip.add(lore);
            }
        }
    }

    @SubscribeEvent
    public void onAnvilRepair(AnvilRepairEvent event) {

        PlayerClass playerClass = PlayerExtendedProperties.getPlayerClass(event.entityPlayer);
        if (!event.entityPlayer.worldObj.isRemote) {
            // Blacksmithing skill
            // Smith class bonus
            if (getSkill(event.entityPlayer, PlayerSkill.BLACKSMITHING) > 0) {
                if (event.right.getItem().isItemTool(event.right)) {
                    int amount = 0;
                    if (playerClass == PlayerClass.SMITH) writeItemInfo(event.right, event.entityPlayer, "smith", amount);
                    if (event.right.getItem() instanceof ItemArmor) {
                        //Armor
                    } else if (event.right.getItem() instanceof ItemTool) {
                        //Tools
                    } else {
                        //Swords, Bows, etc
                        if (Math.random() <= getSkill(event.entityPlayer, PlayerSkill.BLACKSMITHING) / 100D) {
                            if (playerClass == PlayerClass.SMITH)
                                amount = 2;
                            else
                                amount = 1;
                            writeItemInfo(event.right, event.entityPlayer, "damage", amount);
                        }

                        if (Math.random() <= getSkill(event.entityPlayer, PlayerSkill.BLACKSMITHING) / 100D) {
                            if (playerClass == PlayerClass.SMITH)
                                amount = 20;
                            else
                                amount = 15;
                            writeItemInfo(event.right, event.entityPlayer, "crit", amount);
                        }
                    }
                }
            }
        } else {
            if (getSkill(event.entityPlayer, PlayerSkill.BLACKSMITHING) >= 5) {
                // Returning exp from repair
                if (Math.random() < 0.25) {
                    int lvlDiff = PlayerExtendedProperties.from(event.entityPlayer).loadLatestExp() - event.entityPlayer.experienceLevel;
                    int multiplier = getSkill(event.entityPlayer, PlayerSkill.BLACKSMITHING) / 5 * 5;
                    int returnedExp = lvlDiff / 100 * multiplier;
                    if (lvlDiff >= 6 && returnedExp < 1)
                        returnedExp = 1;
                    FMLProxyPacket packet = SkillPacketHandler.getOtherPacket(Side.SERVER, "addExp/" + returnedExp);
                    LevelUp.otherChannel.sendToServer(packet);
                    event.entityPlayer.addChatComponentMessage(new ChatComponentTranslation("blacksmith.xp.return", returnedExp));
                }
            }
            PlayerExtendedProperties.from(event.entityPlayer).saveLatestExp(event.entityPlayer.experienceLevel);
        }
    }

    public static void writeItemInfo(ItemStack craftedItem, EntityPlayer player, String type, int amount) {
        ItemStack item = craftedItem;
        for (ItemStack s : player.inventory.mainInventory) {
            if (s != null && s.getItem() == craftedItem.getItem() && s.getTagCompound() != null && s.getTagCompound().equals(craftedItem.getTagCompound())) {
                item = s;
            }
        }

        if (item != null) {
            NBTTagCompound tagCompound = item.getTagCompound();
            if (tagCompound == null)
                tagCompound = new NBTTagCompound();
            item.setTagCompound(tagCompound);
            if (type.equals("smith"))
                tagCompound.setString("Smith", player.getDisplayName());
            else if (type.equals("damage"))
                tagCompound.setInteger("BonusDamage", amount);
            else if (type.equals("crit"))
                tagCompound.setInteger("BonusCrit", amount);
            item.setTagCompound(tagCompound);
        }
    }

    // write "NoPlacing" flag onto a block to prevent dupes
    private void writeNoPlacing(ItemStack toDrop) {
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
        // Farming skill bonus
        int skill = getSkill(event.getPlayer(), PlayerSkill.FARMING);
        if (random.nextInt(10) < skill / 5) {
            Item ID = event.block.getItemDropped(event.blockMetadata, random, 0);
            if(ID == null){
                if(event.block == Blocks.pumpkin_stem){
                    ID = Items.pumpkin_seeds;
                }else if(event.block == Blocks.melon_stem){
                    ID = Items.melon_seeds;
                }
            }
            if (ID != null) {
                event.world.spawnEntityInWorld(new EntityItem(event.world, event.x, event.y, event.z, new ItemStack(ID, 1, event.block.damageDropped(event.blockMetadata))));
                event.getPlayer().addChatComponentMessage(new ChatComponentTranslation("farming.double"));
            }
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
        if (!event.wasDeath || !ConfigHelper.resetClassOnDeath || ConfigHelper.percentSkillOnDeath < 100) {
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
     * @return -1 if no drop is required
     */
    public static int getBonusFishingLoot(EntityPlayer player) {
        // Fishing skill bonus
        if (player.getRNG().nextDouble() > (getSkill(player, PlayerSkill.FISHING) / 5) * 0.01D) {
            return -1;
        } else {
            return player.getRNG().nextInt(bonusFishingLootList.size());
        }
    }

    /**
     * Helper to retrieve skill points from the index
     */
    public static int getSkill(EntityPlayer player, PlayerSkill playerSkill) {
        return PlayerExtendedProperties.getSkill(player, playerSkill);
    }

    /**
     * Add items to fishing loot itemlist
     */
    public static void addItemsToFishingList(List<String> itemlist) {
        if (bonusFishingLootList == null)
            bonusFishingLootList = new ArrayList<String>(itemlist.size());
        bonusFishingLootList = itemlist;
    }
}
