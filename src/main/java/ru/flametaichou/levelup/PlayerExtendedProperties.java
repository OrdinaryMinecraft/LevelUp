package ru.flametaichou.levelup;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import ru.flametaichou.levelup.Handlers.SkillPacketHandler;
import ru.flametaichou.levelup.Model.ExtPropPacket;

import java.util.HashMap;
import java.util.Map;

public final class PlayerExtendedProperties implements IExtendedEntityProperties {
    private byte playerClass;
    private int airBars;
    private boolean clearEffects;
    private long lastSkillActivation;
    private int doubleShotCount;
    private int fireShotCount;
    private Map<String, Integer> skillMap = new HashMap<String, Integer>();
    private Map<String, int[]> counterMap = new HashMap<String, int[]>();
    public final static String[] counters = {"ore", "craft", "bonus"};

    public PlayerExtendedProperties() {
        for (String name : ClassBonus.skillNames)
            skillMap.put(name, 0);
        counterMap.put(counters[0], new int[]{0, 0, 0, 0});
        counterMap.put(counters[1], new int[]{0, 0, 0, 0});
        counterMap.put(counters[2], new int[]{0, 0, 0});//ore bonus, craft bonus, kill bonus
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setByte("Class", playerClass);
        compound.setInteger("AirBars", skillMap.get("Swimming") / 5);
        compound.setBoolean("ClearEffects", clearEffects);
        compound.setLong("LastSkillActivation", lastSkillActivation);
        compound.setInteger("DoubleShotCount", doubleShotCount);
        compound.setInteger("FireShotCount", fireShotCount);
        for (String name : ClassBonus.skillNames) {
            compound.setInteger(name, skillMap.get(name));
        }
        for (String cat : counters) {
            compound.setIntArray(cat, counterMap.get(cat));
        }
    }

    public ExtPropPacket loadExtendedProperties () {
        ExtPropPacket packet = new ExtPropPacket();
        packet.doubleShotData = doubleShotCount;
        packet.skillCooldownData = lastSkillActivation;
        packet.effectData = clearEffects;
        packet.airData = airBars;
        return packet;
    }

    public void sendExtPropsToServer() {
        FMLProxyPacket packet = SkillPacketHandler.getExtPropPacket(Side.SERVER, loadExtendedProperties().toString());
        LevelUp.extPropertiesChannel.sendToServer(packet);
    }

    /*
     * Полоски воздуха
     */

    public void sendAirData(int air) {
        saveAirData(air);
        sendExtPropsToServer();
    }

    public void saveAirData(int air) {
        airBars = air;
    }

    public int getAirData() {
        return airBars;
    }

    public int loadAirData() {
        return airBars;
    }

    /*
     * Очистка от негативных эффектов
     */

    public void sendEffectData(boolean clear) {
        saveEffectData(clear);
        sendExtPropsToServer();
    }

    public void saveEffectData(boolean clear) {
        clearEffects = clear;
    }

    public boolean loadEffectData() {
        return clearEffects;
    }

    /*
     * Время последнего использования классовой способности
     */

    public void sendLastSkillActivation(long l) {
        saveLastSkillActivation(l);
        sendExtPropsToServer();
    }

    public void saveLastSkillActivation(long l) {
        lastSkillActivation = l;
    }

    public long loadLastSkillActivation() {
        return lastSkillActivation;
    }

    /*
     * Количество двойных выстрелов для лучника
     */

    public void sendDoubleShotCount(int dsc) {
        saveDoubleShotCount(dsc);
        sendExtPropsToServer();
    }

    public void saveDoubleShotCount(int dsc) {
        doubleShotCount = dsc;
    }

    public int loadDoubleShotCount() {
        return doubleShotCount;
    }

    /*
     * Количество огненных выстрелов для лучника
     */

    public void saveFireShotCount(int fsc) {
        fireShotCount = fsc;
    }

    public int loadFireShotCount() {
        return fireShotCount;
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        playerClass = compound.getByte("Class");
        airBars = compound.getInteger("AirBars");
        clearEffects = compound.getBoolean("ClearEffects");
        lastSkillActivation = compound.getLong("LastSkillActivation");
        doubleShotCount = compound.getInteger("DoubleShotCount");
        fireShotCount = compound.getInteger("FireShotCount");
        for (String name : ClassBonus.skillNames) {
            skillMap.put(name, compound.getInteger(name));
        }
        for (String cat : counters) {
            counterMap.put(cat, compound.getIntArray(cat));
        }
    }

    @Override
    public void init(Entity entity, World world) {
    }

    public static PlayerExtendedProperties from(EntityPlayer player) {
        return ((PlayerExtendedProperties) player.getExtendedProperties(ClassBonus.SKILL_ID));
    }

    public void addToSkill(String name, int value) {
        skillMap.put(name, skillMap.get(name) + value);
    }

    public int getSkillFromIndex(String name) {
        return skillMap.get(name);
    }

    public static int getSkillFromIndex(EntityPlayer player, int id) {
        return from(player).getSkillFromIndex(ClassBonus.skillNames[id]);
    }

    public int getSkillPoints() {
        int total = 0;
        for (String skill : ClassBonus.skillNames) {
            total += getSkillFromIndex(skill);
        }
        return total;
    }

    public boolean hasClass() {
        return playerClass != 0;
    }

    public static byte getPlayerClass(EntityPlayer player) {
        return from(player).playerClass;
    }

    public void setPlayerClass(byte newClass) {
        if (newClass != playerClass) {
            ClassBonus.applyBonus(this, playerClass, newClass);
            capSkills();
            playerClass = newClass;
        }
    }

    public static Map<String, int[]> getCounterMap(EntityPlayer player) {
        return from(player).counterMap;
    }

    /**
     * Проверяет, не превышен ли лимит прокачки навыков
     */
    public void capSkills() {
        for (String name : ClassBonus.skillNames) {
            if (name.equals("XP"))
                continue;
            int j = skillMap.get(name);
            if (j > ClassBonus.getMaxSkillPoints()) {
                skillMap.put(name, ClassBonus.getMaxSkillPoints());
            }
        }
    }

    public void takeSkillFraction(float ratio) {
        final byte clas = playerClass;
        if (clas != 0) {
            ClassBonus.applyBonus(this, clas, (byte) 0);
            playerClass = 0;
        }
        for (String name : ClassBonus.skillNames) {
            final int value = skillMap.get(name);
            int remove = (int) (value * ratio);
            if (remove > 0) {
                skillMap.put(name, value - remove);
            }
        }
        if (clas != 0) {
            ClassBonus.applyBonus(this, (byte) 0, clas);
            playerClass = clas;
        }
        capSkills();
    }

    public void convertPointsToXp(boolean resetClass) {
        final byte clas = playerClass;
        setPlayerClass((byte) 0);
        skillMap.put("XP", getSkillPoints());
        setPlayerData(new int[ClassBonus.skillNames.length - 1]);
        if (!resetClass)
            setPlayerClass(clas);
    }

    public void setPlayerData(int[] data) {
        for (int i = 0; i < ClassBonus.skillNames.length && i < data.length; i++) {
            skillMap.put(ClassBonus.skillNames[i], data[i]);
        }
    }

    public int[] getPlayerData(boolean withClass) {
        int[] data = new int[ClassBonus.skillNames.length + (withClass ? 1 : 0)];
        for (int i = 0; i < ClassBonus.skillNames.length; i++)
            data[i] = getSkillFromIndex(ClassBonus.skillNames[i]);
        if (withClass)
            data[data.length - 1] = playerClass;
        return data;
    }
}
