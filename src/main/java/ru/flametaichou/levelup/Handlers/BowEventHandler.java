package ru.flametaichou.levelup.Handlers;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import ru.flametaichou.levelup.LevelUp;
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

                //Archery skill
                int archerSkill = getArcherSkill(archer);
                if (archerSkill != 0) {
                    arrow.motionX *= 1.0F + archerSkill / 100F;
                    arrow.motionY *= 1.0F + archerSkill / 100F;
                    arrow.motionZ *= 1.0F + archerSkill / 100F;
                }

                if (PlayerExtendedProperties.getPlayerClass(archer) == 5 && !arrow.isSneaking()) {

                    //Double shots
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

                        if (countArrows > 0 || archer.capabilities.isCreativeMode) {
                            Random random = new Random();
                            int bonusArrows = 1;
                            for (int i=0; i < bonusArrows; i++) {
                                EntityArrow arrow2 = new EntityArrow(arrow.worldObj, archer, 0);
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

                                if (!archer.capabilities.isCreativeMode) {
                                    archer.inventory.consumeInventoryItem(Items.arrow);
                                }

                                arrow.worldObj.spawnEntityInWorld(arrow2);
                            }

                            PlayerExtendedProperties.from(archer).sendDoubleShotCount(PlayerExtendedProperties.from(archer).loadDoubleShotCount() - 1);
                        }
                    }

                    //Charged shots
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

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBowUse(PlayerUseItemEvent.Start event) {
        if (event.item != null && event.item.getMaxStackSize() == 1 && event.item.getItemUseAction() == EnumAction.bow) {
            int archer = getArcherSkill(event.entityPlayer);
            if (archer != 0 && event.duration > archer / 5)
                event.duration -= (archer / 5);
        }
    }

    public static int getArcherSkill(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 5);
    }
}
