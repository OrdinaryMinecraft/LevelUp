package ru.flametaichou.levelup.Handlers;

import java.util.*;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import ru.flametaichou.levelup.*;
import ru.flametaichou.levelup.Model.PacketChannel;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.Util.ConfigHelper;
import ru.flametaichou.levelup.Util.PlayerUtils;
import ru.flametaichou.levelup.Util.WorldUtils;

public final class FMLEventHandler {
    /**
     * Movement data for Athletics
     */
    private static final UUID speedID = UUID.fromString("4f7637c8-6106-4050-96cb-e47f83bfa415");
    /**
     * Movement data for Sneaking
     */
    private static final UUID sneakID = UUID.fromString("a4dc0b04-f78a-43f6-8805-5ebfbab10b18");
    /**
     * Number of ticks a furnace run
     */
    public static final FMLEventHandler INSTANCE = new FMLEventHandler();
    /**
     * Blocks that could be crops, but should be left alone by Farming skill
     */
    private List<String> blackListedCrops;
    final Random random = new Random();
    boolean flagSetCounter;
    final int [] negativeEffects = {2, 4, 9, 15, 17, 18, 19, 20};
    
    private FMLEventHandler() {
    }
    
    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (event.phase == TickEvent.Phase.START) {
            EntityPlayer player = event.player;

            // Clear effects
            if (PlayerExtendedProperties.from(player).loadEffectData()) {
                removeEffects(player);
                PlayerExtendedProperties.from(player).sendEffectData(false, player.worldObj.isRemote);
            }

            if (player.worldObj.getWorldTime() % 50 == 0 && !player.worldObj.isRemote) {
                // Miner class bonus
                if (PlayerExtendedProperties.getPlayerClass(player) == PlayerClass.MINER && player.posY < 50) {
                    player.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 80, 0, true));
                }
                // Hunter class bonus
                if (PlayerExtendedProperties.getPlayerClass(player) == PlayerClass.HUNTER && WorldUtils.isNight(event.player.worldObj)) {
                    player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 300, -1, true));
                }
                // Thief class bonus
                if (PlayerExtendedProperties.getPlayerClass(player) == PlayerClass.THIEF && player.isSneaking()) {
                    // if (!player.worldObj.isDaytime() || !player.worldObj.canBlockSeeTheSky((int) player.posX, (int) player.posY, (int) player.posZ)) {
                    if (PlayerUtils.playerIsInShadow(player)) {
                        if (!player.isInvisible())
                            player.worldObj.playSoundEffect(player.posX, player.posY, player.posZ, "mob.enderdragon.wings", 1.1F, 1.1F);
                        player.addPotionEffect(new PotionEffect(Potion.invisibility.id, 80, 0, true));
                    }
                }
                // Sentinel class bonus
                if (PlayerExtendedProperties.getPlayerClass(player) == PlayerClass.SENTINEL && PlayerUtils.timeAfterLastAttack(player) > 600) {
                    player.addPotionEffect(new PotionEffect(22, 200, 1, true));
                }
                // Traveller class bonus
                if (PlayerExtendedProperties.getPlayerClass(player) == PlayerClass.TRAVELLER) {
                    if (!player.getActivePotionEffects().isEmpty()) {
                        boolean hungerFound = false;
                        Collection effectsList = player.getActivePotionEffects();
                        for (Object obj : effectsList) {
                            PotionEffect effect = (PotionEffect) obj;
                            if (effect.getPotionID() == Potion.hunger.id) {
                                hungerFound = true;
                            }
                        }
                        if (hungerFound) {
                            player.removePotionEffect(Potion.hunger.id);
                        }
                    }
                }
                // Orb Pickup
                if (player.inventory.hasItem(LevelUp.expOrb)) {
                    if (!PlayerUtils.playerIsOp((EntityPlayerMP) player) && !MinecraftServer.getServer().isSinglePlayer()) {
                        for (ItemStack itemstack : player.inventory.mainInventory) {
                            if (itemstack != null && itemstack.getItem() != null && itemstack.getItem() == LevelUp.expOrb) {
                                if (itemstack.getTagCompound() == null ||
                                        (itemstack.getTagCompound() != null &&
                                                (itemstack.getTagCompound().getString("Owner") == null ||
                                                        itemstack.getTagCompound().getString("Owner").equals("")))) {
                                    writeOwner(itemstack, player.getDisplayName());
                                }
                            }
                        }
                    }
                }
            }

            // Swimming skill bonus
    		if (player.isInWater())
    		{
    			int bonus = PlayerExtendedProperties.getSkill(player, PlayerSkill.SWIMMING);
    			float multiplier = Math.max(0.0F,bonus);
    			player.moveFlying(player.moveStrafing * multiplier, player.moveForward * multiplier, 0.02f);
        		flagSetCounter = false;
    		}
    		else if (!flagSetCounter) {
    			int bonus = PlayerExtendedProperties.getSkill(player, PlayerSkill.SWIMMING);
    			PlayerExtendedProperties.from(player).sendAirData(bonus / 5, player.worldObj.isRemote);
    			flagSetCounter = true;
    		}

            // Farming skill bonus
            int skill = PlayerExtendedProperties.getSkill(player, PlayerSkill.FARMING);
            if (!player.worldObj.isRemote && player.getCurrentEquippedItem() != null &&
                    player.getCurrentEquippedItem().getItem() instanceof ItemHoe &&
                    skill != 0 && player.getRNG().nextFloat() <= skill / 2500F) {
                // Ломаем мотыгу
                ItemStack item = player.getHeldItem();
                int damage = item.getItemDamage();
                //item.damageItem(1, event.entityPlayer);
                if (!player.capabilities.isCreativeMode) {
                    if (item.isItemStackDamageable())
                        item.setItemDamage(damage+1);
                }
                growCropsAround(player.worldObj, skill / 2, player);
            }

            // Athletics skill bonus
            IAttributeInstance atinst = player.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            AttributeModifier mod;
            skill = PlayerExtendedProperties.getSkill(player, PlayerSkill.ATHLETICS);
            if (skill != 0) {
                mod = new AttributeModifier(speedID, "SprintingSkillSpeed", skill / 100F, 2);
                if (player.isSprinting()) {
                    if (atinst.getModifier(speedID) == null) {
                        atinst.applyModifier(mod);
                    }
                } else if (atinst.getModifier(speedID) != null) {
                    atinst.removeModifier(mod);
                }
                if (player.fallDistance > 0) {
                    player.fallDistance *= 1 - skill / 5 / 100F;
                }
            }
            // Sneaking skill bonus
            skill = PlayerExtendedProperties.getSkill(player, PlayerSkill.SNEAKING);
            if (skill != 0) {
                mod = new AttributeModifier(sneakID, "SneakingSkillSpeed", 2 * skill / 100F, 2);
                if (player.isSneaking()) {
                    if (atinst.getModifier(sneakID) == null) {
                        atinst.applyModifier(mod);
                    }
                } else if (atinst.getModifier(sneakID) != null) {
                    atinst.removeModifier(mod);
                }
            }

            /*
             * Fishing skill bonus
             */
            int skillFishing = PlayerExtendedProperties.getSkill(player, PlayerSkill.FISHING);
            if (skillFishing > 0)
            if (event.player.fishEntity != null) {
                EntityFishHook hook = event.player.fishEntity;
                // Если поправок не в воде (до поклевки, находясь на поверхности воды
                // считается что поплавок находится не в воде)
                if (!hook.isInWater()) {
                    if (hook.worldObj.getWorldTime() % 15 == 0) { //15 - время, за которое можно успеть схватить рыбу на крючке
                        if (hook.motionY > 0) hook.setSneaking(false);
                    }
                    // Тут проверяется, летит ли еще поплавок в воду, или он уже остановился и занял позицию
                    if (Math.abs(hook.motionX - hook.motionZ) <= 0.05) {
                        Random random = new Random();
                        if (!hook.worldObj.isRemote) {
                            if (random.nextInt(2000) + 1 >= 2000-skillFishing) {
                                hook.motionY -= 0.20000000298023224D;
                                // Это победа
                                hook.setSneaking(true);
                                hook.playSound("random.splash", 0.25F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F);
                                float f1 = (float) MathHelper.floor_double(hook.boundingBox.minY);
                                WorldServer worldserver = (WorldServer) hook.worldObj;
                                worldserver.func_147487_a("bubble", hook.posX, (double) (f1 + 1.0F), hook.posZ, (int) (1.0F + hook.width * 20.0F), (double) hook.width, 0.0D, (double) hook.width, 0.20000000298023224D);
                                worldserver.func_147487_a("wake", hook.posX, (double) (f1 + 1.0F), hook.posZ, (int) (1.0F + hook.width * 20.0F), (double) hook.width, 0.0D, (double) hook.width, 0.20000000298023224D);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void removeEffects(EntityPlayer player) {
    	for (int i = 0; i < negativeEffects.length; i++) {
    		player.removePotionEffect(negativeEffects[i]);
    	}
    }
    
    /**
     * Apply bonemeal on non-black-listed blocks around player
     */
    private void growCropsAround(World world, int range, EntityPlayer player) {
        int posX = (int) player.posX;
        int posY = (int) player.posY;
        int posZ = (int) player.posZ;
        int dist = range / 2 + 2;
        for (int x = posX - dist; x < posX + dist + 1; x++) {
            for (int z = posZ - dist; z < posZ + dist + 1; z++) {
                for (int y = posY - dist; y < posY + dist + 1; y++) {
                	//if (random.nextInt(3) == 1)
                    if (Math.random() * 100 <= PlayerExtendedProperties.getSkill(player, PlayerSkill.FARMING))
                    if (world.isAirBlock(x, y + 1, z)) {
                        Block block = world.getBlock(x, y, z);
                        Integer blockid = Block.getIdFromBlock(block);
                        if (block instanceof IPlantable && !blackListedCrops.contains(blockid.toString())) {
                            Block soil = world.getBlock(x, y - 1, z);
                            if (!soil.isAir(world, x, y - 1, z) && soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, (IPlantable) block)) {
                                ItemDye.applyBonemeal(new ItemStack(Items.dye, 1, 15), world, x, y, z, player);
                                world.playAuxSFX(2005, x, y, z, 0);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Converts given black-listed names into blocks for the internal black-list
     */
    public void addCropsToBlackList(List<String> blackList) {
        if (blackListedCrops == null)
            blackListedCrops = new ArrayList<String>(blackList.size());
        blackListedCrops = blackList;
    }

    /**
     * Add more output when smelting food for Cooking and other items for Smelting
     */
    @SubscribeEvent
    public void onSmelting(PlayerEvent.ItemSmeltedEvent event) {
    	LevelUp.takenFromSmelting(event.player, event.smelting);
        PlayerClass playerClass = PlayerExtendedProperties.getPlayerClass(event.player);
    	if (!event.player.worldObj.isRemote) {
            ItemStack add = null;
            if (event.smelting.getItemUseAction() == EnumAction.eat) {

                // Peasant class bonus
                if (playerClass == PlayerClass.PEASANT) {
                    if (Math.random() <= 0.15) {
                        add = event.smelting.copy();
                        event.player.addChatComponentMessage(new ChatComponentTranslation("peasant.smelting.double"));
                    }
                }
            }
//            // Убрано из-за практически дюпа в Metallurgy когда слитки можно переделывать в пыль и снова переплавлять
//            else if (playerClass == PlayerClass.SMITH) {
//                if (Math.random() <= 0.15) {
//                    add = event.smelting.copy();
//                    event.player.addChatComponentMessage(new ChatComponentTranslation("smith.smelting.double"));
//                }
//            }
            //TODO: добавить обработку shift
            EntityItem entityitem = ForgeHooks.onPlayerTossEvent(event.player, add, true);
            if (entityitem != null) {
                entityitem.delayBeforeCanPickup = 0;
                entityitem.func_145797_a(event.player.getCommandSenderName());
            }
        }
    }

    /**
     * Track player changing dimension to update skill points data
     */
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        loadPlayer(event.player);
    }

    /**
     * Track player crafting
     */
    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {

        PlayerClass playerClass = PlayerExtendedProperties.getPlayerClass(event.player);
        if (!event.player.worldObj.isRemote) {
            ItemStack add = null;

            // Smith class bonus
            if (playerClass == PlayerClass.SMITH) {
                List<ItemStack> itemStacks = new ArrayList<ItemStack>();
                boolean containsIngots = false;
                for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
                    if (Objects.nonNull(event.craftMatrix.getStackInSlot(i))) {
                        itemStacks.add(event.craftMatrix.getStackInSlot(i));
                        if (event.craftMatrix.getStackInSlot(i).getItem().getUnlocalizedName().toLowerCase().contains("ingot")) {
                            containsIngots = true;
                        }
                    }
                }

                // To prevent uncraft ingot blocks like iron block, gold block, etc. dupe
                if (event.crafting.getItem().getUnlocalizedName().toLowerCase().contains("block")) {
                    containsIngots = false;
                }

                if (event.crafting.getItem() instanceof ItemTool || event.crafting.getItem() instanceof ItemArmor || containsIngots) {
                    if (Math.random() <= 0.30) {
                        //int slot = random.nextInt(itemStacks.size()) + 1;
                        int slot = WorldUtils.randomBetween(0, itemStacks.size() - 1);
                        add = itemStacks.get(slot);
                        event.player.addChatComponentMessage(new ChatComponentTranslation("smith.crafting.return"));
                    }
                }
            }

            if (Objects.nonNull(add)) {
                EntityItem entityitem = ForgeHooks.onPlayerTossEvent(event.player, add, true);
                if (entityitem != null) {
                    entityitem.delayBeforeCanPickup = 0;
                    entityitem.func_145797_a(event.player.getCommandSenderName());
                }
            }
        }

//        if(e.crafting.getItem().equals(Tools.RubyAxe)){
//            e.player.addStat(Achievements.achievementRubyAxe, 1);
//        }
    }

    /**
     * Track player respawn to update skill points data
     */
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        loadPlayer(event.player);
    }

    /**
     * Vitality skill bonus
     * Increase HP
     */
    public void updatePlayerHP(EntityPlayer player) {
    	int skill = PlayerExtendedProperties.getSkill(player, PlayerSkill.VITALITY);
        player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20 + (2 * skill / 5));
    }

    /**
     * Track player login to update skill points data and some configuration values
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            loadPlayer(event.player);
            LevelUp.configChannel.sendTo(SkillPacketHandler.getConfigPacket(ConfigHelper.getServerProperties()), (EntityPlayerMP) event.player);
            updatePlayerHP(event.player);
        }
    }

    /**
     * Help build the packet to send to client for updating skill point data
     */
    public void loadPlayer(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            PlayerClass pClass = PlayerExtendedProperties.getPlayerClass(player);
            int[] data = PlayerExtendedProperties.from(player).getPlayerData(false);
            LevelUp.initChannel.sendTo(SkillPacketHandler.getPacket(Side.CLIENT, PacketChannel.LEVELUPINIT, (byte) pClass.getId(), data), (EntityPlayerMP) player);
            FMLProxyPacket packet = SkillPacketHandler.getExtPropPacket(Side.CLIENT, PlayerExtendedProperties.from(player).loadExtendedProperties().toString());
            LevelUp.extPropertiesChannel.sendTo(packet, (EntityPlayerMP) player);
        }
    }

    // write "Owner" flag onto a expOrb
    private void writeOwner(ItemStack toDrop, String playerName) {
        NBTTagCompound tagCompound = toDrop.getTagCompound();
        if (tagCompound == null)
            tagCompound = new NBTTagCompound();
        tagCompound.setString("Owner", playerName);
        toDrop.setTagCompound(tagCompound);
    }

}
