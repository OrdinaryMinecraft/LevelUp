package ru.flametaichou.levelup.Model;

public enum PlayerSkill {
    EXP(0),
    MINING(1),
    SWORDS(2),
    VITALITY(3),
    LOOTING(4),
    ARCHERY(5),
    ATHLETICS(6),
    SWIMMING(7),
    SNEAKING(8),
    FARMING(9),
    FISHING(10),
    BLACKSMITHING(11);

    private final int id;

    private PlayerSkill(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
