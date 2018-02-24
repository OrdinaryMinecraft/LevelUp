package ru.flametaichou.levelup;

public final class ClassBonus {
    /**
     * The key used for registering skill data into players
     */
    public final static String SKILL_ID = "LevelUpSkills";
    /**
     * The sub keyGui used when registering each skill data
     */
    public final static String[] skillNames = {"Mining", "Sword", "Defense", "WoodCutting", "Looting", "Archery", "Athletics", "Swimming", "Sneaking", "Farming", "Fishing", "Digging", "XP"};
    /**
     * Total points given when choosing a class
     * Allocated in three skills for most classes
     */
    private static int bonusPoints = 20;
    /**
     * The maximum value for each skill
     */
    private static int maxSkillPoints = 50;

    public static int getBonusPoints() {
        return bonusPoints;
    }

    public static void setBonusPoints(int value) {
        if (value >= 0)
            bonusPoints = value <= maxSkillPoints * 2 ? value : maxSkillPoints * 2;
    }

    public static int getMaxSkillPoints() {
        return maxSkillPoints;
    }

    public static void setSkillMax(int value) {
        if (value > 0)
            ClassBonus.maxSkillPoints = value;
    }

    public static void addBonusToSkill(PlayerExtendedProperties properties, String name, int bonus, boolean isNew) {
        properties.addToSkill(name, bonus * (isNew ? 1 : -1));
    }

    private static void applyBonus(PlayerExtendedProperties properties, byte playerClass, boolean isNew) {
        CLASSES clas = CLASSES.from(playerClass);
        if (clas.isNone())
            return;
        if (clas.hasOnlyOneSkill()) {
            addBonusToSkill(properties, skillNames[clas.main], bonusPoints, isNew);
            return;
        }
        int small = bonusPoints / 4;
        int big = bonusPoints - 2 * small;//Make sure all points are allocated no matter what value bonus is
        addBonusToSkill(properties, skillNames[clas.main], big, isNew);
        addBonusToSkill(properties, skillNames[clas.sec1], small, isNew);
        addBonusToSkill(properties, skillNames[clas.sec2], small, isNew);
    }

    /**
     * Handle class change
     * First remove all bonus points from the old class,
     * then add all bonus points for the new one
     */
    public static void applyBonus(PlayerExtendedProperties properties, byte oldClass, byte newClass) {
        applyBonus(properties, oldClass, false);
        applyBonus(properties, newClass, true);
    }

    public static enum CLASSES {
        NONE(-1, -1, -1),
        MINER(0, 11, 2),
        SWORDSMAN(1, 2, 6),
        SMITH(0, 1, 2),
        ARCHAEOLOGIST(11, 4, 0),
        MARKSMAN(5, 8, 6),
        FARMER(9, 11, 3),
        LUMBERJACK(3, 2, 6),
        HUNTER(4, 1, 5),
        FISHERMAN(10, 7, 3),
        TRAVELLER(6, 7, 8),
        THIEF(4, 8, 6),
        SENTINEL(2, 1, 5),
        FREELANCE(12, 12, 12);
        private final int main, sec1, sec2;

        private CLASSES(int main, int sec1, int sec2) {
            this.main = main;
            this.sec1 = sec1;
            this.sec2 = sec2;
        }

        public static CLASSES from(byte b) {
            if (b < 0)
                return NONE;
            return values()[b];
        }

        public boolean isNone() {
            return this == NONE;
        }

        public boolean hasOnlyOneSkill() {
            return this.main == this.sec1 && this.main == this.sec2;
        }
    }
}
