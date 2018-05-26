package ru.flametaichou.levelup.Handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.world.NoteBlockEvent;
import ru.flametaichou.levelup.*;
import ru.flametaichou.levelup.Model.ExtPropPacket;
import ru.flametaichou.levelup.Model.PacketChannel;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.Util.ConfigHelper;
import ru.flametaichou.levelup.Util.EnumUtils;
import ru.flametaichou.levelup.Util.PlayerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SkillPacketHandler {

    private Random random = new Random();
    private static double basicStealChanse = 0.2;

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        if (event.packet.channel().equals(PacketChannel.LEVELUPCLASSES.name()))
            handleClassChange(event.packet.payload().readByte(), ((NetHandlerPlayServer) event.handler).playerEntity);
        else if (event.packet.channel().equals(PacketChannel.LEVELUPSKILLS.name())) {
            handlePacket(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity);
            // Increase HP
            FMLEventHandler.INSTANCE.updatePlayerHP(((NetHandlerPlayServer) event.handler).playerEntity);
        } if (event.packet.channel().equals(PacketChannel.LEVELUPEXTPROP.name())) {
            handleExtPropsChange(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity);
        } if (event.packet.channel().equals(PacketChannel.LEVELUPOTHER.name())) {
            handleOtherPacket(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity);
        }
    }

    private void handleClassChange(byte newClass, EntityPlayerMP entityPlayerMP) {
        if (newClass >= 0) {
            PlayerExtendedProperties.from(entityPlayerMP).setPlayerClass(EnumUtils.getPlayerClassFromId(newClass));
            FMLEventHandler.INSTANCE.loadPlayer(entityPlayerMP);
        }
    }

    private void handleExtPropsChange(FMLProxyPacket pckt, EntityPlayer entityPlayerMP) {
        ByteBuf buf = pckt.payload();
        String packetString = "";
        for (byte bt : buf.array()) {
            if (bt != 0)
                packetString += (char) bt;
        }
        ExtPropPacket packet = ExtPropPacket.fromString(packetString);
        PlayerExtendedProperties.from(entityPlayerMP).saveAirData(packet.airData);
        PlayerExtendedProperties.from(entityPlayerMP).saveEffectData(packet.effectData);
        PlayerExtendedProperties.from(entityPlayerMP).saveDoubleShotCount(packet.doubleShotData);
        PlayerExtendedProperties.from(entityPlayerMP).saveLastSkillActivation(packet.skillCooldownData);
    }

    private void handleOtherPacket(FMLProxyPacket pckt, EntityPlayer entityPlayerMP) {
        ByteBuf buf = pckt.payload();
        String packetString = "";
        for (byte bt : buf.array()) {
            if (bt != 0)
                packetString += (char) bt;
        }
        if (packetString.equals("swordsmanBuff")) {
            entityPlayerMP.worldObj.playSoundEffect(entityPlayerMP.posX, entityPlayerMP.posY, entityPlayerMP.posZ, "mob.irongolem.throw", 1.0F, 0.5F);
            entityPlayerMP.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 20, 9, true));
            entityPlayerMP.addPotionEffect(new PotionEffect(Potion.damageBoost.id, 20, 1, true));
        } else if (packetString.contains("addExp")) {
            String[] parts = packetString.split("/");
            entityPlayerMP.addExperienceLevel(Integer.parseInt(parts[1]));
            entityPlayerMP.worldObj.playSoundEffect(entityPlayerMP.posX, entityPlayerMP.posY, entityPlayerMP.posZ, "random.levelup", 1.5F, 1.5F);
        } else if (packetString.contains("steal")) {
            String[] parts = packetString.split("/");
            if (parts[1].equals("player")) {
                // Steam from player
                EntityPlayer victim = entityPlayerMP.worldObj.getPlayerEntityByName(parts[2]);
                int inventorySlots = victim.inventory.mainInventory.length;
                List<Integer> slotsArray = new ArrayList<Integer>();
                for (int i = 0; i < inventorySlots; i++) {
                    if (victim.inventory.mainInventory[i] != null && victim.inventory.mainInventory[i].isStackable()) {
                        slotsArray.add(i);
                    }
                }
                //int slot = random.nextInt(inventorySlots);
                //ItemStack stealingItem = victim.inventory.mainInventory[slot];
                if (slotsArray.size() < 1) {
                    entityPlayerMP.addChatComponentMessage(new ChatComponentTranslation("stealing.player.empty"));
                    return;
                }
                // more than 10 secs after attack to steal
                if (victim.capabilities.isCreativeMode || PlayerUtils.timeAfterLastAttack(victim) <= 200) {
                    entityPlayerMP.addChatComponentMessage(new ChatComponentTranslation("stealing.player.cannot"));
                    return;
                }

                double stealChanse = basicStealChanse;
                // If player is invisible +20% chance
                if (entityPlayerMP.isInvisible()) {
                    stealChanse = stealChanse + 0.1;
                }
                // If player is in shadow +20% chance
                if (PlayerUtils.playerIsInShadow(entityPlayerMP)) {
                    stealChanse = stealChanse + 0.1;
                }
                // If player is on sun -10% chance
                if (PlayerUtils.playerIsOnSun(entityPlayerMP)) {
                    stealChanse = stealChanse - 0.1;
                }
                // If victim does not see player x2 chance
                if (victim.canEntityBeSeen(entityPlayerMP)) {
                    stealChanse = stealChanse * 2;
                }
                if (stealChanse > 1) {
                    stealChanse = 1;
                }

                int slot = random.nextInt(slotsArray.size());
                ItemStack stealingItem = victim.inventory.mainInventory[slotsArray.get(slot)];
                if (Math.random() <= stealChanse) {
                    ItemStack stealingItemCopy = stealingItem.copy();
                    stealingItemCopy.stackSize = 1;
                    victim.inventory.consumeInventoryItem(stealingItem.getItem());
                    entityPlayerMP.inventory.addItemStackToInventory(stealingItemCopy);
                    entityPlayerMP.addChatComponentMessage(new ChatComponentTranslation("stealing.player.success", stealingItem.getItem().getItemStackDisplayName(stealingItem)));
                    entityPlayerMP.worldObj.playSoundEffect(entityPlayerMP.posX, entityPlayerMP.posY, entityPlayerMP.posZ, "mob.irongolem.throw", 1.5F, 1.5F);
                } else {
                    entityPlayerMP.addChatComponentMessage(new ChatComponentTranslation("stealing.player.fail"));
                    victim.addChatComponentMessage(new ChatComponentTranslation("stealing.player.victim", entityPlayerMP.getDisplayName()));
                    entityPlayerMP.worldObj.playSoundEffect(entityPlayerMP.posX, entityPlayerMP.posY, entityPlayerMP.posZ, "mob.chicken.hurt", 1.2F, 1.2F);
                }
            } else if (parts[1].equals("block")) {
                // Steam from block
                Block block = entityPlayerMP.worldObj.getBlock(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
                Integer blockId = Block.getIdFromBlock(block);
                if (!ConfigHelper.stealBlackList.contains(blockId.toString())) {
                    IInventory container = (IInventory) entityPlayerMP.worldObj.getTileEntity(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
                    System.out.print(container);
                    Integer inventorySlots = container.getSizeInventory();

                    List<Integer> slotsArray = new ArrayList<Integer>();
                    for (int i = 0; i < inventorySlots; i++) {
                        if (container.getStackInSlot(i) != null && container.getStackInSlot(i).isStackable()) {
                            slotsArray.add(i);
                        }
                    }
                    if (slotsArray.size() < 1) {
                        entityPlayerMP.addChatComponentMessage(new ChatComponentTranslation("stealing.block.empty"));
                        return;
                    }

                    //int slot = random.nextInt(inventorySlots);
                    //ItemStack stealingItem = container.getStackInSlot(slot);

                    int slot = random.nextInt(slotsArray.size());
                    ItemStack stealingItem = container.getStackInSlot(slotsArray.get(slot));
                    // 20%
                    if (Math.random() <= basicStealChanse) {
                        ItemStack stealingItemCopy = stealingItem.copy();
                        stealingItemCopy.stackSize = 1;
                        container.decrStackSize(slot, 1);
                        entityPlayerMP.inventory.addItemStackToInventory(stealingItemCopy);
                        entityPlayerMP.addChatComponentMessage(new ChatComponentTranslation("stealing.block.success", stealingItem.getItem().getItemStackDisplayName(stealingItem)));
                        entityPlayerMP.worldObj.playSoundEffect(entityPlayerMP.posX, entityPlayerMP.posY, entityPlayerMP.posZ, "mob.irongolem.throw", 1.5F, 1.5F);
                    } else {
                        entityPlayerMP.addChatComponentMessage(new ChatComponentTranslation("stealing.block.fail"));
                        entityPlayerMP.worldObj.playSoundEffect(entityPlayerMP.posX, entityPlayerMP.posY, entityPlayerMP.posZ, "random.chestclosed", 1.0F, 1.0F);
                    }
                } else {
                    entityPlayerMP.addChatComponentMessage(new ChatComponentTranslation("stealing.block.cannot"));
                }
            }
        } else if (packetString.equals("sentinelBuff")) {
            entityPlayerMP.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 200, 2, true));
            entityPlayerMP.addPotionEffect(new PotionEffect(Potion.resistance.id, 200, 2, true));
            int radius = 10;

            List nearestMobsList = entityPlayerMP.worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getBoundingBox(entityPlayerMP.posX-radius, entityPlayerMP.posY-radius, entityPlayerMP.posZ-radius, (entityPlayerMP.posX + radius),(entityPlayerMP.posY + radius),(entityPlayerMP.posZ + radius)));
            if (nearestMobsList.size() > 0) {
                for (Object obj : nearestMobsList) {
                    EntityMob mob = (EntityMob) obj;
                    mob.setRevengeTarget(entityPlayerMP);
                    mob.onUpdate();
                }
            }

            entityPlayerMP.addChatComponentMessage(new ChatComponentTranslation("provocation.player"));
            entityPlayerMP.worldObj.playSoundEffect(entityPlayerMP.posX, entityPlayerMP.posY, entityPlayerMP.posZ, "mob.irongolem.death", 1.2F, 1.2F);
        } else if (packetString.equals("minerBuff")) {
            entityPlayerMP.worldObj.playSoundEffect(entityPlayerMP.posX, entityPlayerMP.posY, entityPlayerMP.posZ, "note.snare", 1.2F, 1.2F);
        } else if (packetString.equals("travellerBuff")) {
            entityPlayerMP.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 300, 2, true));
            entityPlayerMP.addPotionEffect(new PotionEffect(Potion.jump.id, 300, 1, true));
            entityPlayerMP.worldObj.playSoundEffect(entityPlayerMP.posX, entityPlayerMP.posY, entityPlayerMP.posZ, "fireworks.launch", 1.5F, 1.5F);
        }
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        if (event.packet.channel().equals(PacketChannel.LEVELUPINIT.name()))
            handlePacket(event.packet, LevelUp.proxy.getPlayer());
        else if (event.packet.channel().equals(PacketChannel.LEVELUPCFG.name()))
            handleConfig(event.packet);
    }

    private void handlePacket(FMLProxyPacket packet, EntityPlayer player) {
        ByteBuf buf = packet.payload();
        byte button = buf.readByte();
        int[] data = null;
        int sum = 0;

        if (packet.channel().equals(PacketChannel.LEVELUPINIT.name()) || button == -1) {
            data = new int[PlayerSkill.values().length];
            for (int i = 0; i < data.length; i++) {
                data[i] = buf.readInt();
                sum += data[i];
            }
        }

        PlayerExtendedProperties properties = PlayerExtendedProperties.from(player);
        if (packet.channel().equals(PacketChannel.LEVELUPSKILLS.name())) {
            if (properties.hasClass())
                if (data != null && button == -1 && sum == 0) {
                    if (data[PlayerSkill.EXP.getId()] != 0 && -data[PlayerSkill.EXP.getId()] <= properties.getSkillFromIndex(PlayerSkill.EXP)) {
                        for (int index = 0; index < data.length; index++) {
                            if (data[index] != 0) {
                                properties.addToSkill(PlayerSkill.valueOf(PlayerSkill.values()[index].name()), data[index]);
                            }
                        }
                        FMLEventHandler.INSTANCE.loadPlayer(player);
                    }
                }
        } else if (packet.channel().equals(PacketChannel.LEVELUPINIT.name()) && data != null) {
            properties.setPlayerClass(EnumUtils.getPlayerClassFromId(button));
            properties.setPlayerData(data);
        }
        FMLEventHandler.INSTANCE.updatePlayerHP(player);
    }

    public static FMLProxyPacket getPacket(Side side, PacketChannel channel, byte id, int... dat) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(id);
        if ((id < 0 || channel.getId() == 0) && dat != null) {
            for (int da : dat)
                buf.writeInt(da);
        }
        FMLProxyPacket pkt = new FMLProxyPacket(buf, channel.name());
        pkt.setTarget(side);
        return pkt;
    }

    public static FMLProxyPacket getConfigPacket(int[] dat) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(dat[0]);
        buf.writeInt(dat[1]);
        buf.writeInt(dat[2]);
        buf.writeInt(dat[3]);
        buf.writeInt(dat[4]);
        FMLProxyPacket pkt = new FMLProxyPacket(buf, PacketChannel.LEVELUPCFG.name());
        pkt.setTarget(Side.CLIENT);
        return pkt;
    }

    public static FMLProxyPacket getExtPropPacket(Side side, String data) {
        char[] dat = data.toCharArray();
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < dat.length; i++) {
            buf.writeByte(dat[i]);
        }
        FMLProxyPacket pkt = new FMLProxyPacket(buf, PacketChannel.LEVELUPEXTPROP.name());
        pkt.setTarget(side);
        return pkt;
    }

    public static FMLProxyPacket getOtherPacket(Side side, String data) {
        char[] dat = data.toCharArray();
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < dat.length; i++) {
            buf.writeByte(dat[i]);
        }
        FMLProxyPacket pkt = new FMLProxyPacket(buf, PacketChannel.LEVELUPOTHER.name());
        pkt.setTarget(side);
        return pkt;
    }


    private void handleConfig(FMLProxyPacket packet) {
        ByteBuf buf = packet.payload();
        int[] properties = ConfigHelper.getServerProperties();
        properties[0] = buf.readInt();
        properties[1] = buf.readInt();
        properties[2] = buf.readInt();
        properties[3] = buf.readInt();
        properties[4] = buf.readInt();
        ConfigHelper.useServerProperties(properties);
    }
}
