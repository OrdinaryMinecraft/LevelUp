package ru.flametaichou.levelup.Util;

import net.minecraft.world.World;

import java.util.Random;

public class WorldUtils {

    private static Random random = new Random();

    public static boolean isNight(World world) {
        long time = world.getWorldTime();
        if (time > 12500) {
            return true;
        } else {
            return false;
        }
    }

    public static int randomBetween(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }
}
