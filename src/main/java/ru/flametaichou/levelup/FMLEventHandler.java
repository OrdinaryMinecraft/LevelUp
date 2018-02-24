package ru.flametaichou.levelup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

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
    //private static final int maxFurnaceCookTime = 200;
    public static final FMLEventHandler INSTANCE = new FMLEventHandler();
    /**
     * Blocks that could be crops, but should be left alone by Farming skill
     */
    private List<String> blackListedCrops;
    final Random random = new Random();
    int waterCounter;
    int waterCounterPrev;
    boolean flagCounter;
    boolean flagSetCounter;
    final int [] negativeEffects = {2, 4, 9, 15, 17, 18, 19, 20};
    
    private FMLEventHandler() {
    }
    
    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (event.phase == TickEvent.Phase.START) {
            EntityPlayer player = event.player;
            //clear effects
            if (PlayerExtendedProperties.from(player).loadEffectData()) {
            	removeEffects(player);
            	PlayerExtendedProperties.from(player).saveEffectData(false);
            }
            //underwater bonus
    		if (player.isInWater())
    		{
    			int bonus = getSkill(player, 7);
    			float multiplier = Math.max(0.0F,bonus);
    			player.moveFlying(player.moveStrafing * multiplier, player.moveForward * multiplier, 0.02f);
        		flagSetCounter = false;
    		}
    		else if (!flagSetCounter) {
    			int bonus = getSkill(player, 7);
    			PlayerExtendedProperties.from(player).saveAirData(bonus / 5);
    			flagSetCounter = true;
    		}
   
            //Give points on levelup
            if (PlayerExtendedProperties.getPlayerClass(player) != 0 && player.isEntityAlive()) {
                double diff = PlayerEventHandler.xpPerLevel * (player.experienceLevel - PlayerEventHandler.minLevel) + ClassBonus.getBonusPoints() - PlayerExtendedProperties.from(player).getSkillPoints();
                if (diff >= 1.0D) {
                    PlayerExtendedProperties.from(player).addToSkill("XP", (int) Math.floor(diff));
                }
            }
            
            //Farming grow crops
            int skill;
            if (!player.worldObj.isRemote && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemHoe && (skill = getSkill(player, 9)) != 0 && player.getRNG().nextFloat() <= skill / 2500F) {
                growCropsAround(player.worldObj, skill / 4, player);
            }
            //Athletics speed
            IAttributeInstance atinst = player.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            AttributeModifier mod;
            skill = getSkill(player, 6);
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
            //Sneaking speed
            skill = getSkill(player, 8);
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

            //Fishing
            int skillFishing = getSkill(player, 10);
            if (skillFishing > 0)
            if (event.player.fishEntity != null) {
                EntityFishHook hook = event.player.fishEntity;
                //Если поправок не в воде (до поклевки, находясь на поверхности воды
                //считается что поплавок находится не в воде)
                if (!hook.isInWater()) {
                    if (hook.worldObj.getWorldTime() % 15 == 0) { //15 - время, за которое можно успеть схватить рыбу на крючке
                        if (hook.motionY > 0) hook.setSneaking(false);
                    }
                    //Тут проверяется, летит ли еще поплавок в воду, или он уже остановился и занял позицию
                    if (Math.abs(hook.motionX - hook.motionZ) <= 0.05) {
                        Random random = new Random();
                        if (!hook.worldObj.isRemote) {
                            if (random.nextInt(2000) + 1 >= 2000-skillFishing) {
                                hook.motionY -= 0.20000000298023224D;
                                //Это победа
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
    
    public void removeEffects (EntityPlayer player) {
    	/*if (player.getActivePotionEffect(Potion.blindness) != null)
			player.removePotionEffect(Potion.blindness.id);
		if (player.getActivePotionEffect(Potion.digSlowdown) != null)
			player.removePotionEffect(Potion.digSlowdown.id);
		if (player.getActivePotionEffect(Potion.moveSlowdown) != null)
			player.removePotionEffect(Potion.moveSlowdown.id);
		if (player.getActivePotionEffect(Potion.confusion) != null)
			player.removePotionEffect(Potion.confusion.id);
		if (player.getActivePotionEffect(Potion.hunger) != null)
			player.removePotionEffect(Potion.hunger.id);
		if (player.getActivePotionEffect(Potion.weakness) != null)
			player.removePotionEffect(Potion.weakness.id);
		if (player.getActivePotionEffect(Potion.poison) != null)
			player.removePotionEffect(Potion.poison.id);
		if (player.getActivePotionEffect(Potion.wither) != null)
			player.removePotionEffect(Potion.wither.id);*/
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
                	if (random.nextInt(3) == 1)
                    if (world.isAirBlock(x, y + 1, z)) {
                        Block block = world.getBlock(x, y, z);
                        Integer blockid = Block.getIdFromBlock(block);
                        if (block instanceof IPlantable && !blackListedCrops.contains(blockid.toString())) {
                            Block soil = world.getBlock(x, y - 1, z);
                            if (!soil.isAir(world, x, y - 1, z) && soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, (IPlantable) block)) {
                                ItemDye.applyBonemeal(new ItemStack(Items.dye, 1, 15), world, x, y, z, player);
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
     * Helper to retrieve skill points from the index
     */
    public static int getSkill(EntityPlayer player, int id) {
        return PlayerExtendedProperties.getSkillFromIndex(player, id);
    }

    /**
     * Add more output when smelting food for Cooking and other items for Smelting
     */
    @SubscribeEvent
    public void onSmelting(PlayerEvent.ItemSmeltedEvent event) {
    	LevelUp.takenFromSmelting(event.player, event.smelting);
        /* if (!event.player.worldObj.isRemote) {
            Random random = event.player.getRNG();
            ItemStack add = null;
            if (event.smelting.getItemUseAction() == EnumAction.eat) {
                if (random.nextFloat() <= getSkill(event.player, 7) / 200F) {
                    add = event.smelting.copy();
                    System.out.println("test");
                }
            } else if (random.nextFloat() <= getSkill(event.player, 4) / 200F) {
                add = event.smelting.copy();
            }
            EntityItem entityitem = ForgeHooks.onPlayerTossEvent(event.player, add, true);
            if (entityitem != null) {
                entityitem.delayBeforeCanPickup = 0;
                entityitem.func_145797_a(event.player.getCommandSenderName());
            }
        }
        */
    }

    /**
     * Track player changing dimension to update skill points data
     */
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        loadPlayer(event.player);
    }

    /**
     * Track player respawn to update skill points data
     */
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        loadPlayer(event.player);
    }
    
    //Increase HP
    public void updatePlayerHP (EntityPlayer player) {
    	int skill = getSkill(player, 2);
        player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20 + (2 * skill / 5));
    }

    /**
     * Track player login to update skill points data and some configuration values
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            loadPlayer(event.player);
            LevelUp.configChannel.sendTo(SkillPacketHandler.getConfigPacket(LevelUp.instance.getServerProperties()), (EntityPlayerMP) event.player);
            updatePlayerHP(event.player);
        }
    }

    /**
     * Help build the packet to send to client for updating skill point data
     */
    public void loadPlayer(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            byte cl = PlayerExtendedProperties.getPlayerClass(player);
            int[] data = PlayerExtendedProperties.from(player).getPlayerData(false);
            LevelUp.initChannel.sendTo(SkillPacketHandler.getPacket(Side.CLIENT, 0, cl, data), (EntityPlayerMP) player);
        }
    }
}
