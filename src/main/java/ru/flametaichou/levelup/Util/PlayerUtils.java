package ru.flametaichou.levelup.Util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

public class PlayerUtils {

    public static long timeAfterLastAttack(EntityPlayer player) {
        return player.ticksExisted - player.getLastAttackerTime();
    }

    public static IIcon getIcon (Item item) {
        return item.getIconFromDamage(0);
    }
}
