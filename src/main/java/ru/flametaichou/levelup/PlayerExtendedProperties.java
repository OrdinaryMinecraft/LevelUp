package ru.flametaichou.levelup;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import ru.flametaichou.levelup.Handlers.SkillPacketHandler;
import ru.flametaichou.levelup.Model.ExtPropPacket;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.Util.ConfigHelper;
import ru.flametaichou.levelup.Util.EnumUtils;

import java.util.HashMap;
import java.util.Map;

public final class PlayerExtendedProperties implements IExtendedEntityProperties {
    private PlayerClass playerClass = PlayerClass.NONE;
    private int airBars;
    private boolean clearEffects;
    private long lastSkillActivation;
    private int doubleShotCount;
    private int fireShotCount;
    private int latestExp;
    private Map<PlayerSkill, Integer> skillMap = new HashMap<PlayerSkill, Integer>();

    public PlayerExtendedProperties() {
        for (PlayerSkill skill : PlayerSkill.values())
            skillMap.put(skill, 0);
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setString("Class", playerClass.name());
        compound.setInteger("AirBars", skillMap.get(PlayerSkill.SWIMMING) / 5);
        compound.setBoolean("ClearEffects", clearEffects);
        compound.setLong("LastSkillActivation", lastSkillActivation);
        compound.setInteger("DoubleShotCount", doubleShotCount);
        compound.setInteger("FireShotCount", fireShotCount);
        for (PlayerSkill skill : PlayerSkill.values()) {
            compound.setInteger(skill.name(), skillMap.get(skill));
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
     * Работает только на стороне клиента (?)
     */

    public void saveFireShotCount(int fsc) {
        fireShotCount = fsc;
    }

    public int loadFireShotCount() {
        return fireShotCount;
    }

    /*
     * Сохраненное количество опыта для кузнечного дела
     * Работает только на стороне клиента
     */

    public void saveLatestExp(int exp) {
        latestExp = exp;
    }

    public int loadLatestExp() {
        return latestExp;
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        playerClass = PlayerClass.valueOf(compound.getString("Class"));
        airBars = compound.getInteger("AirBars");
        clearEffects = compound.getBoolean("ClearEffects");
        lastSkillActivation = compound.getLong("LastSkillActivation");
        doubleShotCount = compound.getInteger("DoubleShotCount");
        fireShotCount = compound.getInteger("FireShotCount");
        for (PlayerSkill skill : PlayerSkill.values()) {
            skillMap.put(skill, compound.getInteger(skill.name()));
        }
    }

    @Override
    public void init(Entity entity, World world) {
    }

    public static PlayerExtendedProperties from(EntityPlayer player) {
        return ((PlayerExtendedProperties) player.getExtendedProperties(ClassBonus.SKILL_ID));
    }

    public void addToSkill(PlayerSkill skill, int value) {
        skillMap.put(skill, skillMap.get(skill) + value);
    }

    public int getSkillFromIndex(PlayerSkill skill) {
        return skillMap.get(skill);
    }

    public static int getSkill(EntityPlayer player, PlayerSkill playerSkill) {
        return from(player).getSkillFromIndex(playerSkill);
    }

    public int getSkillPoints() {
        int total = 0;
        for (PlayerSkill skill : PlayerSkill.values()) {
            total += getSkillFromIndex(skill);
        }
        return total;
    }

    public boolean hasClass() {
        boolean result = false;
        if (playerClass != null && playerClass != PlayerClass.NONE)
            result = true;
        return result;
    }

    public static PlayerClass getPlayerClass(EntityPlayer player) {
        return from(player).playerClass;
    }

    public void setPlayerClass(PlayerClass newClass) {
        if (newClass != playerClass) {
            ClassBonus.applyBonus(this, playerClass, newClass);
            capSkills();
            playerClass = newClass;
        }
    }

    /**
     * Проверяет, не превышен ли лимит прокачки навыков
     */
    public void capSkills() {
        for (PlayerSkill skill : PlayerSkill.values()) {
            if (skill == PlayerSkill.EXP)
                continue;
            int j = skillMap.get(skill);
            if (j > ConfigHelper.maxSkillPoints) {
                skillMap.put(skill, ConfigHelper.maxSkillPoints);
            }
        }
    }

    public void takeSkillPointsFromPlayer(int ratio) {
        final PlayerClass pClass = playerClass;
        if (pClass != PlayerClass.NONE) {
            ClassBonus.applyBonus(this, pClass, PlayerClass.NONE);
            playerClass = PlayerClass.NONE;
        }
        for (PlayerSkill skill : PlayerSkill.values()) {
            final int value = skillMap.get(skill);
            int remove = (value * ratio) / 100;
            if (remove > 0) {
                skillMap.put(skill, value - remove);
            }
        }
        if (pClass != PlayerClass.NONE) {
            ClassBonus.applyBonus(this, PlayerClass.NONE, pClass);
            playerClass = pClass;
        }
        capSkills();
    }

    public void convertPointsToXp(boolean resetClass) {
        final PlayerClass pClass = playerClass;
        setPlayerClass(PlayerClass.NONE);
        skillMap.put(PlayerSkill.EXP, getSkillPoints());
        setPlayerData(new int[PlayerSkill.EXP.getId()]);
        if (!resetClass)
            setPlayerClass(pClass);
    }

    public void setPlayerData(int[] data) {
        for (int i = 0; i < PlayerSkill.values().length && i < data.length; i++) {
            skillMap.put(EnumUtils.getPlayerSkillFromId(i), data[i]);
        }
    }

    public int[] getPlayerData(boolean withClass) {
        int[] data = new int[PlayerSkill.values().length + (withClass ? 1 : 0)];
        for (int i = 0; i < PlayerSkill.values().length; i++) {
            data[i] = getSkillFromIndex(EnumUtils.getPlayerSkillFromId(i));
        }
        if (withClass)
            data[data.length - 1] = playerClass.getId();
        return data;
    }
}
