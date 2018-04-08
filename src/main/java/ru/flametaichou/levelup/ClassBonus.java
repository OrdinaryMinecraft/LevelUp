package ru.flametaichou.levelup;

import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.Util.ConfigHelper;
import ru.flametaichou.levelup.Util.EnumUtils;

public final class ClassBonus {
    /**
     * The key used for registering skill data into players
     */
    public final static String SKILL_ID = "LevelUpSkills";

    public static void addBonusToSkill(PlayerExtendedProperties properties, PlayerSkill skill, int bonus, boolean isNew) {
        properties.addToSkill(skill, bonus * (isNew ? 1 : -1));
    }

    private static void applyBonus(PlayerExtendedProperties properties, PlayerClass playerClass, boolean isNew) {
        if (playerClass.isNone())
            return;
        addBonusToSkill(properties, playerClass.getSkill1(), 5, isNew);
        addBonusToSkill(properties, playerClass.getSkill2(), 3, isNew);
        addBonusToSkill(properties, playerClass.getSkill3(), 1, isNew);
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
