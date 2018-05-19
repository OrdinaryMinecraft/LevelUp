package ru.flametaichou.levelup.Util;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IIcon;

public class PlayerUtils {

    public static long timeAfterLastAttack(EntityPlayer player) {
        return player.ticksExisted - player.getLastAttackerTime();
    }

    public static IIcon getIcon (Item item) {
        return item.getIconFromDamage(0);
    }

    public static boolean playerIsOp (EntityPlayerMP player) {
        return (player.canCommandSenderUseCommand(MinecraftServer.getServer().getOpPermissionLevel(), "gamemode"));
    }
}
