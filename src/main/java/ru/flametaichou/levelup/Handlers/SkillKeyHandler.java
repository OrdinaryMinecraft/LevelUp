package ru.flametaichou.levelup.Handlers;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;
import ru.flametaichou.levelup.*;
import ru.flametaichou.levelup.Model.OreCount;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Util.ConfigHelper;
import ru.flametaichou.levelup.Util.PlayerUtils;
import ru.flametaichou.levelup.gui.GuiClasses;
import ru.flametaichou.levelup.gui.GuiSkills;
import ru.flametaichou.levelup.gui.LevelUpHUD;

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
            } else {
                Minecraft.getMinecraft().displayGuiScreen(new GuiClasses());
            }
        }
        if (keyActivate.getIsKeyPressed() && Minecraft.getMinecraft().currentScreen == null && Minecraft.getMinecraft().thePlayer != null) {

            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            PlayerClass playerClass = PlayerExtendedProperties.getPlayerClass(player);

            // List of classes without active skill
            if (playerClass != PlayerClass.NONE &&
                    playerClass != PlayerClass.SMITH &&
                    playerClass != PlayerClass.HUNTER &&
                    playerClass != PlayerClass.PEASANT) {

                boolean skillUsed = false;
                int skillColldown = 20 * ConfigHelper.activeSkillCooldown;

                if ((player.worldObj.getTotalWorldTime() - PlayerExtendedProperties.from(player).loadLastSkillActivation() > skillColldown) ||  player.capabilities.isCreativeMode) {
                    // Miner class bonus
                    if (playerClass == PlayerClass.MINER) {
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
                        String block_name = "";
                        List<OreCount> ores = new ArrayList<OreCount>();
                        for (int x = player_x - radius; x <= player_x + radius; x++) {
                            for (int y = player_y - radius; y <= player_y + radius; y++) {
                                for (int z = player_z - radius; z <= player_z + radius; z++) {
                                    Block block = player.worldObj.getBlock(x, y, z);
                                    String[] parts = block.getUnlocalizedName().toLowerCase().split("\\.");
                                    for (String part : parts) {
                                        if (part.startsWith("ore") || part.endsWith("ore")) {
                                            boolean flagAdd = false;
                                            for (OreCount ore : ores) {
                                                if (block.getLocalizedName().equals(ore.getOreName())) {
                                                    ore.setCount(ore.getCount() + 1);
                                                    flagAdd = true;
                                                }
                                            }
                                            if (!flagAdd) {
                                                ores.add(new OreCount(block.getLocalizedName(), 1));
                                            }
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
                                                block_name = block.getLocalizedName();
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        FMLProxyPacket packet = SkillPacketHandler.getOtherPacket(Side.SERVER, "minerBuff");
                        LevelUp.otherChannel.sendToServer(packet);

                        if (blocks.isEmpty())
                            player.addChatComponentMessage(new ChatComponentTranslation("miner.ores.none"));
                        else {
                            player.addChatComponentMessage(new ChatComponentTranslation("miner.ores.total"));
                            for (OreCount ore : ores) {
                                player.addChatComponentMessage(new ChatComponentTranslation("· " + ore.getOreName() + " (" + ore.getCount() + ")"));
                            }
                            player.addChatComponentMessage(new ChatComponentTranslation("miner.ores.ore"));
                            player.addChatComponentMessage(new ChatComponentTranslation("· X:" + ore_x + " Y:" + ore_y + " Z:" + ore_z + " (" + block_name + ")"));
                        }
                        skillUsed = true;
                    }

                    // Marksman class bonus
                    if (playerClass == PlayerClass.MARKSMAN) {
                        PlayerExtendedProperties.from(player).sendDoubleShotCount(3, player.worldObj.isRemote);
                        skillUsed = true;
                    }

                    // Swordsman class bonus
                    if (playerClass == PlayerClass.SWORDSMAN) {
                        FMLProxyPacket packet = SkillPacketHandler.getOtherPacket(Side.SERVER, "swordsmanBuff");
                        LevelUp.otherChannel.sendToServer(packet);
                        skillUsed = true;
                    }

                    // Thief class bonus
                    if (playerClass == PlayerClass.THIEF) {
                        // Find entity or block player looking at
                        MovingObjectPosition objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
                        if (objectMouseOver != null) {
                            if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                                if (objectMouseOver.entityHit instanceof EntityPlayer) {
                                    EntityPlayer victim = (EntityPlayer) objectMouseOver.entityHit;
                                    // Steal from player
                                    FMLProxyPacket packet = SkillPacketHandler.getOtherPacket(Side.SERVER, "steal/player/" + victim.getDisplayName());
                                    LevelUp.otherChannel.sendToServer(packet);
                                    skillUsed = true;
                                }
                            } else if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                                TileEntity te = player.worldObj.getTileEntity(objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ);
                                Block block = player.worldObj.getBlock(objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ);
                                if (te != null) {
                                    // if (block instanceof BlockContainer || block instanceof IInventory)
                                    if (te instanceof IInventory) {
                                        // Steal from container
                                        FMLProxyPacket packet = SkillPacketHandler.getOtherPacket(Side.SERVER, "steal/block/" + objectMouseOver.blockX + "/" + objectMouseOver.blockY + "/" + objectMouseOver.blockZ);
                                        LevelUp.otherChannel.sendToServer(packet);
                                        skillUsed = true;
                                    }
                                }
                            }
                        }
                    }

                    // Sentinel class bonus
                    if (playerClass == PlayerClass.SENTINEL) {
                        FMLProxyPacket packet = SkillPacketHandler.getOtherPacket(Side.SERVER, "sentinelBuff");
                        LevelUp.otherChannel.sendToServer(packet);
                        skillUsed = true;
                    }

                    // Traveller class bonus
                    if (playerClass == PlayerClass.TRAVELLER) {
                        FMLProxyPacket packet = SkillPacketHandler.getOtherPacket(Side.SERVER, "travellerBuff");
                        LevelUp.otherChannel.sendToServer(packet);
                        skillUsed = true;
                    }

                    if (skillUsed) {
                        PlayerExtendedProperties.from(player).sendLastSkillActivation(player.worldObj.getTotalWorldTime(), player.worldObj.isRemote);
                    }
                } else {
                    long timeDiff = player.worldObj.getTotalWorldTime() - PlayerExtendedProperties.from(player).loadLastSkillActivation();
                    player.addChatComponentMessage(new ChatComponentTranslation("key.message.cooldown", skillColldown / 20 - timeDiff / 20));
                }
            }
        }
    }
}
