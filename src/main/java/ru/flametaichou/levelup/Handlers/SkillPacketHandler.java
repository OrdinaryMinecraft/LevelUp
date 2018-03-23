package ru.flametaichou.levelup.Handlers;

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
import ru.flametaichou.levelup.ClassBonus;
import ru.flametaichou.levelup.LevelUp;
import ru.flametaichou.levelup.Model.ExtPropPacket;
import ru.flametaichou.levelup.PlayerExtendedProperties;

public final class SkillPacketHandler {
    public static final String[] CHAN = {"LEVELUPINIT", "LEVELUPCLASSES", "LEVELUPSKILLS", "LEVELUPCFG", "LEVELUPEXTPROP", "LEVELUPOTHER"};

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        if (event.packet.channel().equals(CHAN[1]))
            handleClassChange(event.packet.payload().readByte(), ((NetHandlerPlayServer) event.handler).playerEntity);
        else if (event.packet.channel().equals(CHAN[2])) {
            handlePacket(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity);
            //increase HP
            FMLEventHandler.INSTANCE.updatePlayerHP(((NetHandlerPlayServer) event.handler).playerEntity);
        } if (event.packet.channel().equals(CHAN[4])) {
            handleExtPropsChange(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity);
        } if (event.packet.channel().equals(CHAN[5])) {
            handleOtherPacket(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity);
        }
    }

    private void handleClassChange(byte newClass, EntityPlayerMP entityPlayerMP) {
        if (newClass >= 0) {
            PlayerExtendedProperties.from(entityPlayerMP).setPlayerClass(newClass);
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
        //FMLEventHandler.INSTANCE.loadPlayer(entityPlayerMP);
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
        if (event.packet.channel().equals(CHAN[0]))
            handlePacket(event.packet, LevelUp.proxy.getPlayer());
        else if (event.packet.channel().equals(CHAN[3]))
            handleConfig(event.packet);
    }

    private void handlePacket(FMLProxyPacket packet, EntityPlayer player) {
        ByteBuf buf = packet.payload();
        byte button = buf.readByte();
        int[] data = null;
        int sum = 0;
        if (packet.channel().equals(CHAN[0]) || button == -1) {
            data = new int[ClassBonus.skillNames.length];
            for (int i = 0; i < data.length; i++) {
                data[i] = buf.readInt();
                sum += data[i];
            }
        }
        PlayerExtendedProperties properties = PlayerExtendedProperties.from(player);
        if (packet.channel().equals(CHAN[2])) {
            if (properties.hasClass())
                if (data != null && button == -1 && sum == 0) {
                    if (data[data.length - 1] != 0 && -data[data.length - 1] <= properties.getSkillFromIndex("XP")) {
                        for (int index = 0; index < data.length; index++) {
                            if (data[index] != 0) {
                                properties.addToSkill(ClassBonus.skillNames[index], data[index]);
                            }
                        }
                        FMLEventHandler.INSTANCE.loadPlayer(player);
                    }
                }
        } else if (packet.channel().equals(CHAN[0]) && data != null) {
            properties.setPlayerClass(button);
            properties.setPlayerData(data);
        }
        FMLEventHandler.INSTANCE.updatePlayerHP(player);
    }

    public static FMLProxyPacket getPacket(Side side, int channel, byte id, int... dat) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(id);
        if ((id < 0 || channel == 0) && dat != null) {
            for (int da : dat)
                buf.writeInt(da);
        }
        FMLProxyPacket pkt = new FMLProxyPacket(buf, CHAN[channel]);
        pkt.setTarget(side);
        return pkt;
    }

    public static FMLProxyPacket getConfigPacket(Property... dat) {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < dat.length; i++) {
            if (i == 2) {
                buf.writeDouble(dat[i].getDouble());
            } else if (i < 4) {
                buf.writeInt(dat[i].getInt());
            } else {
                buf.writeBoolean(dat[i].getBoolean());
            }
        }
        FMLProxyPacket pkt = new FMLProxyPacket(buf, CHAN[3]);
        pkt.setTarget(Side.CLIENT);
        return pkt;
    }

    public static FMLProxyPacket getExtPropPacket(Side side, String data) {
        char[] dat = data.toCharArray();
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < dat.length; i++) {
            buf.writeByte(dat[i]);
        }
        FMLProxyPacket pkt = new FMLProxyPacket(buf, CHAN[4]);
        pkt.setTarget(side);
        return pkt;
    }

    public static FMLProxyPacket getOtherPacket(Side side, String data) {
        char[] dat = data.toCharArray();
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < dat.length; i++) {
            buf.writeByte(dat[i]);
        }
        FMLProxyPacket pkt = new FMLProxyPacket(buf, CHAN[5]);
        pkt.setTarget(side);
        return pkt;
    }


    private void handleConfig(FMLProxyPacket packet) {
        ByteBuf buf = packet.payload();
        Property[] properties = LevelUp.instance.getServerProperties();
        for (int i = 0; i < properties.length; i++) {
            if (i == 2) {
                properties[i].set(buf.readDouble());
            } else if (i < 4) {
                properties[i].set(buf.readInt());
            } else {
                properties[i].set(buf.readBoolean());
            }
        }
        LevelUp.instance.useServerProperties();
    }
}
