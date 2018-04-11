package ru.flametaichou.levelup.Handlers;

import com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.config.Property;
import ru.flametaichou.levelup.*;
import ru.flametaichou.levelup.Model.ExtPropPacket;
import ru.flametaichou.levelup.Model.PacketChannel;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.Util.ConfigHelper;
import ru.flametaichou.levelup.Util.EnumUtils;

public final class SkillPacketHandler {

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
        ConfigHelper.useServerProperties(properties);
    }
}
