package ru.flametaichou.levelup.Util;

import ru.flametaichou.levelup.Model.PacketChannel;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;

public class EnumUtils {

    public static PlayerClass getPlayerClassFromId(int id) {
        PlayerClass pClass = null;
        if (id == 0) pClass = PlayerClass.NONE;
        if (id == 1) pClass = PlayerClass.MINER;
        if (id == 2) pClass = PlayerClass.SWORDSMAN;
        if (id == 3) pClass = PlayerClass.SMITH;
        if (id == 4) pClass = PlayerClass.MARKSMAN;
        if (id == 5) pClass = PlayerClass.HUNTER;
        if (id == 6) pClass = PlayerClass.PEASANT;
        if (id == 7) pClass = PlayerClass.TRAVELLER;
        if (id == 8) pClass = PlayerClass.THIEF;
        if (id == 9) pClass = PlayerClass.SENTINEL;
        return pClass;
    }

    public static PlayerSkill getPlayerSkillFromId(int id) {
        PlayerSkill pSkill = null;
        if (id == 0) pSkill = PlayerSkill.EXP;
        if (id == 1) pSkill = PlayerSkill.MINING;
        if (id == 2) pSkill = PlayerSkill.SWORDS;
        if (id == 3) pSkill = PlayerSkill.VITALITY;
        if (id == 4) pSkill = PlayerSkill.LOOTING;
        if (id == 5) pSkill = PlayerSkill.ARCHERY;
        if (id == 6) pSkill = PlayerSkill.ATHLETICS;
        if (id == 7) pSkill = PlayerSkill.SWIMMING;
        if (id == 8) pSkill = PlayerSkill.SNEAKING;
        if (id == 9) pSkill = PlayerSkill.FARMING;
        if (id == 10) pSkill = PlayerSkill.FISHING;
        if (id == 11) pSkill = PlayerSkill.BLACKSMITHING;
        return pSkill;
    }

    public static PacketChannel getPacketChannelById(int id) {
        PacketChannel packetChannel = null;
        if (id == 0) packetChannel = PacketChannel.LEVELUPINIT;
        if (id == 1) packetChannel = PacketChannel.LEVELUPCLASSES;
        if (id == 2) packetChannel = PacketChannel.LEVELUPSKILLS;
        if (id == 3) packetChannel = PacketChannel.LEVELUPCFG;
        if (id == 4) packetChannel = PacketChannel.LEVELUPEXTPROP;
        if (id == 5) packetChannel = PacketChannel.LEVELUPOTHER;
        return packetChannel;
    }
}
