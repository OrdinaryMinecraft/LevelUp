package ru.flametaichou.levelup.Util;

import net.minecraft.world.World;

public class WorldUtils {

    public static boolean isNight(World world) {
        long time = world.getWorldTime();
        if (time > 12500) {
            return true;
        } else {
            return false;
        }
    }
}
