package ru.flametaichou.levelup.Util;

import net.minecraft.entity.player.EntityPlayer;

public class PlayerUtils {

    public static long timeAfterLastAttack(EntityPlayer player) {
        return player.ticksExisted - player.getLastAttackerTime();
    }
}
