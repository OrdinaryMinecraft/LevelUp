package ru.flametaichou.levelup.Model;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

public enum PlayerClass {
    // ids must be unique and ordered
    NONE(0, null, null, null, Items.coal),
    MINER(1, PlayerSkill.MINING, PlayerSkill.BLACKSMITHING, PlayerSkill.VITALITY, Items.iron_pickaxe),
    SWORDSMAN(2, PlayerSkill.SWORDS, PlayerSkill.VITALITY, PlayerSkill.ATHLETICS, Items.iron_sword),
    SMITH(3, PlayerSkill.BLACKSMITHING, PlayerSkill.MINING, PlayerSkill.VITALITY, Items.iron_ingot),
    MARKSMAN(4, PlayerSkill.ARCHERY, PlayerSkill.SNEAKING, PlayerSkill.ATHLETICS, Items.bow),
    HUNTER(5, PlayerSkill.LOOTING, PlayerSkill.SWORDS, PlayerSkill.ARCHERY, Items.bow),
    PEASANT(6, PlayerSkill.FARMING, PlayerSkill.FISHING, PlayerSkill.SWIMMING, Items.iron_hoe),
    TRAVELLER(7, PlayerSkill.ATHLETICS, PlayerSkill.SWIMMING, PlayerSkill.VITALITY, Items.golden_boots),
    THIEF(8, PlayerSkill.SNEAKING, PlayerSkill.LOOTING, PlayerSkill.ATHLETICS, Items.gold_nugget),
    SENTINEL(9, PlayerSkill.VITALITY, PlayerSkill.SWORDS, PlayerSkill.ARCHERY, Items.iron_chestplate);
    private final int id;
    private final PlayerSkill skill1, skill2, skill3;
    private IIcon icon;

    private PlayerClass(int id, PlayerSkill skill1, PlayerSkill skill2, PlayerSkill skill3, Item item) {
        this.id = id;
        this.skill1 = skill1;
        this.skill2 = skill2;
        this.skill3 = skill3;
        this.icon = item.getIconFromDamage(0);
    }

    public int getId() {
        return this.id;
    }

    public PlayerSkill getSkill1() {
        return this.skill1;
    }

    public PlayerSkill getSkill2() {
        return this.skill2;
    }

    public PlayerSkill getSkill3() {
        return this.skill3;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public IIcon getIcon() {
        return icon;
    }
}
