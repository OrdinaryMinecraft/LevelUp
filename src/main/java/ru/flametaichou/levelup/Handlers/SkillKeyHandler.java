package ru.flametaichou.levelup.Handlers;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;
import ru.flametaichou.levelup.GuiClasses;
import ru.flametaichou.levelup.GuiSkills;
import ru.flametaichou.levelup.LevelUpHUD;
import ru.flametaichou.levelup.PlayerExtendedProperties;

import java.util.ArrayList;
import java.util.List;

public final class SkillKeyHandler {
    public static final SkillKeyHandler INSTANCE = new SkillKeyHandler();
    public final KeyBinding keyGui = new KeyBinding("key.keys.gui", Keyboard.KEY_H, "key.categories.gui");
    public final KeyBinding keyActivate = new KeyBinding("key.keys.skill", Keyboard.KEY_F, "key.categories.gui");

    private SkillKeyHandler() {
        ClientRegistry.registerKeyBinding(keyGui);
        ClientRegistry.registerKeyBinding(keyActivate);
    }

    @SubscribeEvent
    public void keyDown(InputEvent.KeyInputEvent event) {
        if (keyGui.getIsKeyPressed() && Minecraft.getMinecraft().currentScreen == null && Minecraft.getMinecraft().thePlayer != null) {
            if (LevelUpHUD.canShowSkills()) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiSkills());
            } else if (LevelUpHUD.canSelectClass())
                Minecraft.getMinecraft().displayGuiScreen(new GuiClasses());
        }
        if (keyActivate.getIsKeyPressed() && Minecraft.getMinecraft().currentScreen == null && Minecraft.getMinecraft().thePlayer != null) {

            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            int playerClass = PlayerExtendedProperties.getPlayerClass(player);
            int skillColldown = 20 * 120;

            if (player.worldObj.getTotalWorldTime() - PlayerExtendedProperties.from(player).loadLastSkillActivation() > skillColldown) {
                if (playerClass == 1) {
                    int radius = 10;
                    int player_x = (int) player.posX;
                    int player_y = (int) player.posY;
                    int player_z = (int) player.posZ;

                    boolean flag = false;
                    List<Block> blocks = new ArrayList<Block>();
                    int abs_x = 0;
                    int abs_y = 0;
                    int abs_z = 0;
                    int min_dist = 999;
                    int dist = 0;
                    int ore_x = 0;
                    int ore_y = 0;
                    int ore_z = 0;
                    for (int x = player_x - radius; x <= player_x + radius; x++) {
                        for (int y = player_y - radius; y <= player_y + radius; y++) {
                            for (int z = player_z - radius; z <= player_z + radius; z++) {
                                Block block = player.worldObj.getBlock(x, y, z);
                                if (block.getUnlocalizedName().contains("ore") || block.getUnlocalizedName().contains("Ore")) {
                                    blocks.add(block);
                                    abs_x = Math.abs(player_x - x);
                                    abs_y = Math.abs(player_y - y);
                                    abs_z = Math.abs(player_z - z);
                                    dist = abs_x + abs_y + abs_z;
                                    if (dist < min_dist) {
                                        min_dist = dist;
                                        ore_x = x;
                                        ore_y = y;
                                        ore_z = z;
                                    }
                                }
                            }
                        }
                    }

                    if (blocks.isEmpty())
                        player.addChatComponentMessage(new ChatComponentTranslation("miner.ores.none"));
                    else {
                        player.addChatComponentMessage(new ChatComponentTranslation("miner.ores.ore"));
                        player.addChatComponentMessage(new ChatComponentTranslation("X:" + ore_x + " Y:" + ore_y + " Z:" + ore_z));
                    }
                }
                PlayerExtendedProperties.from(player).saveLastSkillActivation(player.worldObj.getTotalWorldTime());
            } else {
                long timeDiff = player.worldObj.getTotalWorldTime() - PlayerExtendedProperties.from(player).loadLastSkillActivation();
                player.addChatComponentMessage(new ChatComponentTranslation("key.message.cooldown", skillColldown / 20 - timeDiff / 20));
            }
        }
    }
}
