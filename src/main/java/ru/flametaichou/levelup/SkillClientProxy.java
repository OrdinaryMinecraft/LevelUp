package ru.flametaichou.levelup;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import ru.flametaichou.levelup.Handlers.SkillKeyHandler;
import ru.flametaichou.levelup.gui.LevelUpHUD;

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
