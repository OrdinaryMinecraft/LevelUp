package ru.flametaichou.levelup;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

@SuppressWarnings("UnusedDeclaration")
public final class SkillClientProxy extends SkillProxy {

    @Override
    public void registerGui() {
        MinecraftForge.EVENT_BUS.register(LevelUpHUD.INSTANCE);
        FMLCommonHandler.instance().bus().register(SkillKeyHandler.INSTANCE);
    }

    @Override
    public EntityPlayer getPlayer() {
        return FMLClientHandler.instance().getClient().thePlayer;
    }
}
