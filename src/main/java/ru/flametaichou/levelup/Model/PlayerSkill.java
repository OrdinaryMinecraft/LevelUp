package ru.flametaichou.levelup.Model;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import ru.flametaichou.levelup.LevelUp;

public enum PlayerSkill {
    EXP(0, Items.experience_bottle),
    MINING(1, Items.iron_pickaxe),
    SWORDS(2, Items.iron_sword),
    VITALITY(3, LevelUp.iconHearth),
    LOOTING(4, LevelUp.iconThief),
    ARCHERY(5, Items.bow),
    ATHLETICS(6, LevelUp.iconAthletics),
    SWIMMING(7, LevelUp.iconSwimming),
    SNEAKING(8, LevelUp.iconSneaking),
    FARMING(9, Items.iron_hoe),
    FISHING(10, Items.fishing_rod),
    BLACKSMITHING(11, LevelUp.iconSmith);

    private final int id;
    private IIcon icon;

    private PlayerSkill(int id, Item item) {
        this.id = id;
        this.icon = item.getIconFromDamage(0);
    }

    public int getId() {
        return this.id;
    }

    public IIcon getIcon() {
        return icon;
    }
}
