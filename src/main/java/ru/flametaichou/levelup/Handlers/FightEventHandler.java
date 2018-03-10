package ru.flametaichou.levelup.Handlers;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import ru.flametaichou.levelup.LevelUp;
import ru.flametaichou.levelup.PlayerExtendedProperties;

public final class FightEventHandler {
    public static final FightEventHandler INSTANCE = new FightEventHandler();
    final Random random = new Random();

    private FightEventHandler() {
    }
    
    @SubscribeEvent
    public void onAttacked(LivingAttackEvent event) {
        DamageSource damagesource = event.source;

        // air bars
    	if (damagesource.equals(DamageSource.drown)) {
    		if (event.entityLiving instanceof EntityPlayer) {
    			EntityPlayer player = (EntityPlayer) event.entityLiving;
                if (PlayerExtendedProperties.from(player).loadAirData() > 0) {
                	event.setCanceled(true);
                	player.setAir(300);
                	PlayerExtendedProperties.from(player).sendAirData(PlayerExtendedProperties.from(player).loadAirData()-1);
                    player.addChatComponentMessage(new ChatComponentTranslation("water.count", PlayerExtendedProperties.from(player).loadAirData()));
                    }
                }
        }
    }

    @SubscribeEvent
    public void onHurting(LivingHurtEvent event) {
        DamageSource damagesource = event.source;
        
        if (!damagesource.isMagicDamage()) {
	        float i = event.ammount;
	        float damage = event.ammount;
	        if (damagesource.getEntity() instanceof EntityPlayer) {
	            EntityPlayer entityplayer = (EntityPlayer) damagesource.getEntity();
                EntityLivingBase victim = event.entityLiving;

	            //Swordsman splash bonus
                int playerClass = PlayerExtendedProperties.getPlayerClass(entityplayer);
                if (playerClass == 2 && Math.random() <= 0.20 &&
                        (entityplayer.getHeldItem().getItem().getUnlocalizedName().contains("sword") || entityplayer.getHeldItem().getItem().getUnlocalizedName().contains("Sword"))) {
                    int radius = 1;
                    List e = victim.worldObj.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getBoundingBox(victim.posX-radius, victim.posY-radius, victim.posZ-radius, (victim.posX + radius),(victim.posY + radius),(victim.posZ + radius)));
                    if (e.size() > 0) {
                        entityplayer.worldObj.playSoundEffect(entityplayer.posX, entityplayer.posY, entityplayer.posZ, "mob.irongolem.hit", 1F, 2F + random.nextFloat() * 0.2F);

                        for (int j = 0; j <= e.size() - 1; j++) {
                            EntityLiving em = (EntityLiving) e.get(j);
                            damagesource.setMagicDamage();
                            em.attackEntityFrom(damagesource, damage * 0.3F);
                        }

                    }
                }

                //Swords skill
                //Archery skill
	            if (LevelUp.debugMode) entityplayer.addChatComponentMessage(new ChatComponentTranslation("Damage without mod " + String.valueOf(i)));
	            if (damagesource instanceof EntityDamageSourceIndirect) {
	                if (damagesource.damageType.equals("arrow")) {
                        i = i * (1.0F + BowEventHandler.getArcherSkill(entityplayer) / 100F);
	                }
	                if (getDistance(event.entityLiving, entityplayer) < 256F && entityplayer.isSneaking() && !canSeePlayer(event.entityLiving) && !entityIsFacing(event.entityLiving, entityplayer)) {
	                    i = i * 1.5F;
	                    entityplayer.addChatComponentMessage(new ChatComponentTranslation("sneak.attack", 1.5));
	                }
	            } else {
	                if (entityplayer.getCurrentEquippedItem() != null) {
	                    int j = getSwordSkill(entityplayer);
	                    if (entityplayer.getRNG().nextDouble() <= j / 200D) {
	                        i *= 2.0F;
                            entityplayer.addChatComponentMessage(new ChatComponentTranslation("critical.attack", 2));
	                        entityplayer.onCriticalHit(event.entityLiving);
	                    }
	                    i = i * (1.0F + j / 5 / 20F);
	                }
	                if (entityplayer.isSneaking() && !canSeePlayer(event.entityLiving) && !entityIsFacing(event.entityLiving, entityplayer)) {
	                    i = i * 2.0F;
	                    entityplayer.addChatComponentMessage(new ChatComponentTranslation("sneak.attack", 2));
	                }
	            }
	            if (LevelUp.debugMode) entityplayer.addChatComponentMessage(new ChatComponentTranslation("Damage with mod " + String.valueOf(i)));
	        }
	        /*
	         * Defense Skill
	         * 
	         * if (event.entityLiving instanceof EntityPlayer) {
	            EntityPlayer player = (EntityPlayer) event.entityLiving;
	            if (LevelUp.debugMode) player.addChatComponentMessage(new ChatComponentTranslation("Damage taken without mod " + String.valueOf(i)));
	            int j = getDefenseSkill(player);
	            player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(player.getMaxHealth() + 2);
	            if (!damagesource.isUnblockable()) {
	                //i *= 1.0F - j / 5 / 20F;
	                i *= 1.0F - j / 100F;
	            }
	            if (player.isBlocking() && player.getRNG().nextFloat() < j / 100F) {
	                i *= 0F;
	            }
	            if (LevelUp.debugMode) player.addChatComponentMessage(new ChatComponentTranslation("Damage taken with mod " + String.valueOf(i)));
	        }
	        */
	        
	        //if ((i >= event.entityLiving.getHealth()) && (!event.isCanceled())) {
	        	//event.entityLiving.setHealth(1);
	        	//event.entityLiving.onDeath(damagesource);
	        //	event.ammount = event.entityLiving.getHealth();
	        //}
	        //else {
	        	//event.ammount = i;
	        //}
	        if (damagesource.getEntity() instanceof EntityPlayer) {
	        	damagesource.setMagicDamage();
	            event.entityLiving.attackEntityFrom(damagesource, i-damage);
	        }
        }
    	//event.entityLiving.setHealth(event.entityLiving.getHealth()-i);
        
        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entityLiving;
            if (getDefenseSkill(player) != 0) {
                int j = getDefenseSkill(player);
                if (player.getRNG().nextDouble() <= j / 200D) {
                	//heal
                	if (player.getHealth() != player.getMaxHealth())
                		player.setHealth(player.getHealth()+2);

                	player.getEntityWorld().playAuxSFX(2005, (int) player.posX-1, (int) player.posY+1, (int) player.posZ, 0);
                	player.worldObj.playSoundEffect(player.posX, player.posY, player.posZ, "random.levelup", 2.0F, 1.5F + random.nextFloat() * 0.2F);
                	
                	PlayerExtendedProperties.from(player).sendEffectData(true);
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onTargetSet(LivingSetAttackTargetEvent event) {
        if (event.target instanceof EntityPlayer && event.entityLiving instanceof EntityMob) {
            if (event.target.isSneaking() && !entityHasVisionOf(event.entityLiving, (EntityPlayer) event.target)
                    && event.entityLiving.func_142015_aE() != event.entityLiving.ticksExisted) {
                ((EntityMob) event.entityLiving).setAttackTarget(null);
            }
        }
    }

    private static int getDefenseSkill(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 2);
    }

    private int getSwordSkill(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 1);
    }

    public static boolean canSeePlayer(EntityLivingBase entityLiving) {
        EntityPlayer entityplayer = entityLiving.worldObj.getClosestVulnerablePlayerToEntity(entityLiving, 16D);
        return entityplayer != null && entityLiving.canEntityBeSeen(entityplayer) && (!entityplayer.isSneaking() || entityHasVisionOf(entityLiving, entityplayer));
    }

    public static float getDistance(EntityLivingBase entityLiving, EntityLivingBase entityliving1) {
        return MathHelper.floor_double_long((entityliving1.posX - entityLiving.posX) * (entityliving1.posX - entityLiving.posX) + (entityliving1.posZ - entityLiving.posZ)
                * (entityliving1.posZ - entityLiving.posZ));
    }

    @SuppressWarnings("UnusedDeclaration")
    public static float getPointDistance(double d, double d1, double d2, double d3) {
        return MathHelper.floor_double_long((d2 - d) * (d2 - d) + (d3 - d1) * (d3 - d1));
    }

    public static boolean compareAngles(float f, float f1, float f2) {
        if (MathHelper.abs(f - f1) < f2) {
            return true;
        }
        if (f + f2 >= 360F) {
            if ((f + f2) - 360F > f1) {
                return true;
            }
        }
        if (f1 + f2 >= 360F) {
            if ((f1 + f2) - 360F > f) {
                return true;
            }
        }
        return false;
    }

    public static boolean entityHasVisionOf(EntityLivingBase entityLiving, EntityPlayer player) {
        if (entityLiving == null || player == null) {
            return false;
        }
        if (getDistance(entityLiving, player) > 256F - PlayerExtendedProperties.from(player).getSkillFromIndex("Sneaking") / 5 * 12.8F) {
            return false;
        }
        return entityLiving.canEntityBeSeen(player) && entityIsFacing(player, entityLiving);
    }

    public static boolean entityIsFacing(EntityLivingBase entityLiving, EntityLivingBase entityliving1) {
        if (entityLiving == null || entityliving1 == null) {
            return false;
        }
        float f = -(float) (entityliving1.posX - entityLiving.posX);
        float f1 = (float) (entityliving1.posZ - entityLiving.posZ);
        float f2 = entityLiving.rotationYaw;
        if (f2 < 0.0F) {
            float f3 = (MathHelper.floor_float(MathHelper.abs(f2) / 360F) + 1.0F) * 360F;
            f2 = f3 + f2;
        } else {
            while (f2 > 360F) {
                f2 -= 360F;
            }
        }
        float f4 = (float) ((Math.atan2(f, f1) * 180F) / Math.PI);
        if (f < 0.0F) {
            f4 = 360F + f4;
        }
        return compareAngles(f2, f4, 22.5F);
    }
}
