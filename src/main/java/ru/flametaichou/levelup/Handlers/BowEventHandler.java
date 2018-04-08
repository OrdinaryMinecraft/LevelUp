package ru.flametaichou.levelup.Handlers;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import ru.flametaichou.levelup.Entity.EntityCustomArrow;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.PlayerExtendedProperties;

import java.util.Random;

public final class BowEventHandler {
    public static final BowEventHandler INSTANCE = new BowEventHandler();

    private BowEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSpawn(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow) event.entity;
            if (arrow.shootingEntity instanceof EntityPlayer) {
                EntityPlayer archer = (EntityPlayer) arrow.shootingEntity;

                // Arrow speedup
                //int archerSkill = getArcherSkill(archer);
                //if (archerSkill != 0) {
                //    arrow.motionX *= 1.0F + archerSkill / 100F;
                //    arrow.motionY *= 1.0F + archerSkill / 100F;
                //    arrow.motionZ *= 1.0F + archerSkill / 100F;
                //}

                // Marksman class bonus
                if (PlayerExtendedProperties.getPlayerClass(archer) == PlayerClass.MARKSMAN && !arrow.isSneaking()) {

                    // Double shots
                    if (PlayerExtendedProperties.from(archer).loadDoubleShotCount() > 0) {

                        int countArrows = 0;
                        ItemStack arrowStack = null;
                        for (ItemStack s : archer.inventory.mainInventory)
                        {
                            if (s != null && s.getItem() == Items.arrow) {
                                countArrows = countArrows + s.stackSize;
                                arrowStack = s;
                            }
                        }

                        Random random = new Random();

                        if (countArrows > 0 || archer.capabilities.isCreativeMode) {
                            int bonusArrows = 2;
                            for (int i=0; i < bonusArrows; i++) {
                                EntityCustomArrow arrow2 = new EntityCustomArrow(arrow.worldObj, archer, 0);
                                arrow2.setDamage(arrow.getDamage());
                                arrow2.posX = arrow.posX + 0.1 + random.nextFloat() * (-0.1 - 0.1);
                                arrow2.posY = arrow.posY + 0.1 + random.nextFloat() * (-0.1 - 0.1);
                                arrow2.posZ = arrow.posZ + 0.1 + random.nextFloat() * (-0.1 - 0.1);
                                arrow2.rotationPitch = arrow.rotationPitch;
                                arrow2.rotationYaw = arrow.rotationYaw;
                                arrow2.motionX = arrow.motionX + 0.1 + random.nextFloat() * (-0.1 - 0.1);
                                arrow2.motionY = arrow.motionY + 0.1 + random.nextFloat() * (-0.1 - 0.1);
                                arrow2.motionZ = arrow.motionZ + 0.1 + random.nextFloat() * (-0.1 - 0.1);
                                arrow2.setIsCritical(arrow.getIsCritical());
                                if (PlayerExtendedProperties.from(archer).loadFireShotCount() <= 0) arrow2.setFire(2);
                                arrow2.setSneaking(true);

                                if (i != 0)
                                    if (!archer.capabilities.isCreativeMode) {
                                        archer.inventory.consumeInventoryItem(Items.arrow);
                                    }

                                arrow.worldObj.spawnEntityInWorld(arrow2);
                            }
                            if (arrow.isEntityAlive())
                            arrow.setDead();

                            PlayerExtendedProperties.from(archer).sendDoubleShotCount(PlayerExtendedProperties.from(archer).loadDoubleShotCount() - 1);
                        }
                    }

                    // Charged shots
                    if (PlayerExtendedProperties.from(archer).loadFireShotCount() <= 0) {
                        PlayerExtendedProperties.from(archer).saveFireShotCount(5);
                        arrow.setDamage(arrow.getDamage()*1.5);
                        arrow.setFire(2);
                        archer.worldObj.playSoundEffect(archer.posX, archer.posY, archer.posZ, "mob.ghast.fireball", 1.0F, 2F);
                        archer.getEntityWorld().playAuxSFX(2004, (int) archer.posX-1, (int) archer.posY+1, (int) archer.posZ, 0);
                    } else {
                        PlayerExtendedProperties.from(archer).saveFireShotCount(PlayerExtendedProperties.from(archer).loadFireShotCount() - 1);
                    }

                }
            }
        }
    }

    // Archery skill bonus
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBowUse(PlayerUseItemEvent.Start event) {
        if (event.item != null && event.item.getMaxStackSize() == 1 && event.item.getItemUseAction() == EnumAction.bow) {
            int archerySkill = PlayerExtendedProperties.getSkill(event.entityPlayer, PlayerSkill.ARCHERY);
            if (archerySkill != 0 && event.duration > archerySkill / 5) {
                event.duration -= (archerySkill / 5);
            }
        }
    }
}
