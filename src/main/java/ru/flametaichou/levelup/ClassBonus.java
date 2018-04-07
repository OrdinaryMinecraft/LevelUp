package ru.flametaichou.levelup;

import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.Util.EnumUtils;

public final class ClassBonus {
    /**
     * The key used for registering skill data into players
     */
    public final static String SKILL_ID = "LevelUpSkills";

    private static int bonusPoints = 5;
    private static int maxSkillPoints = 50;

    public static int getBonusPoints() {
        return bonusPoints;
    }

    public static int getMaxSkillPoints() {
        return maxSkillPoints;
    }

    public static void setBonusPoints(int value) {
        if (value > 0)
            bonusPoints = value;
    }

    public static void setSkillMax(int value) {
        if (value > 0)
            ClassBonus.maxSkillPoints = value;
    }

    public static void addBonusToSkill(PlayerExtendedProperties properties, PlayerSkill skill, int bonus, boolean isNew) {
        properties.addToSkill(skill, bonus * (isNew ? 1 : -1));
    }

    private static void applyBonus(PlayerExtendedProperties properties, PlayerClass playerClass, boolean isNew) {
        if (playerClass.isNone())
            return;
        addBonusToSkill(properties, playerClass.getSkill1(), bonusPoints, isNew);
        addBonusToSkill(properties, playerClass.getSkill2(), bonusPoints, isNew);
        addBonusToSkill(properties, playerClass.getSkill3(), bonusPoints, isNew);
    }

    /**
     * Handle class change
     * First remove all bonus points from the old class,
     * then add all bonus points for the new one
     */
    public static void applyBonus(PlayerExtendedProperties properties, PlayerClass oldClass, PlayerClass newClass) {
        applyBonus(properties, oldClass, false);
        applyBonus(properties, newClass, true);
    }
}
