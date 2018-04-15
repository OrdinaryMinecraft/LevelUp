package ru.flametaichou.levelup.gui;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import ru.flametaichou.levelup.Handlers.SkillPacketHandler;
import ru.flametaichou.levelup.LevelUp;
import ru.flametaichou.levelup.Model.PacketChannel;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Util.EnumUtils;
import ru.flametaichou.levelup.Util.PlayerUtils;

public final class GuiClasses extends GuiScreen {
    private boolean closedWithButton = false;
    private PlayerClass pClass = PlayerClass.NONE;
    private ItemStack stack;
    private GuiTextField text;
    private static final ResourceLocation texture = new ResourceLocation(LevelUp.ID, "textures/gui/lvlup-gui.png");

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        drawDefaultBackground();
        this.text.drawTextBox();
        if (pClass == PlayerClass.NONE) {
            drawString(fontRendererObj, StatCollector.translateToLocalFormatted("gui.class.tooltip"), this.text.xPosition + 3, this.text.yPosition + 3, 0xffffff);
        } else {
            drawString(fontRendererObj, StatCollector.translateToLocal("class." + pClass.name() + ".tooltip1"), this.text.xPosition + 3, this.text.yPosition + 3, 0xffffff);
            drawString(fontRendererObj, StatCollector.translateToLocal("class." + pClass.name() + ".tooltip2"), this.text.xPosition + 3, this.text.yPosition + 15, 0xffffff);
            drawString(fontRendererObj, StatCollector.translateToLocal("class." + pClass.name() + ".tooltip3"), this.text.xPosition + 3, this.text.yPosition + 27, 0xffffff);
        }
        drawCenteredString(fontRendererObj, StatCollector.translateToLocalFormatted("gui.class.title", StatCollector.translateToLocal("class." + pClass.name() + ".name")), width / 2, height / 6 + 174, 0xffffff);
        for (Object obj : buttonList) {
            GuiButton button = (GuiButton) obj;
            if (button.id != 0 && button.id != 100){
                PlayerClass buttonClass = EnumUtils.getPlayerClassFromId(button.id);
                mc.getTextureManager().bindTexture(texture);
                drawTexturedModalRect(button.xPosition-24, button.yPosition-4, 0,0, 61, 28);
                mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
                drawTexturedModelRectFromIcon(button.xPosition-21, button.yPosition, PlayerUtils.getIcon(buttonClass.getIcon()), 20, 20);
            }

        }
        super.drawScreen(i, j, f);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        closedWithButton = false;
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 + 96, height / 6 + 168, 96, 20, StatCollector.translateToLocal("gui.done")));
        buttonList.add(new GuiButton(100, width / 2 - 192, height / 6 + 168, 96, 20, StatCollector.translateToLocal("gui.cancel")));
        for (int j = 1; j <= 9; j = j + 3) {
            for (int i = 0; i < 3; i++) {
                int xPosition = width / 2 - 160 + i * 112;
                int yPosition = 18 + 32 * (j - 1) / 3;
                GuiButton button = new GuiButton(i + j, xPosition+20, yPosition, 76, 20, StatCollector.translateToLocal("class." + EnumUtils.getPlayerClassFromId(i + j) + ".name"));
                buttonList.add(button);
            }
        }
        int textX = width / 2 - 160;
        int textY = 18 + 32 * 4;
        this.text = new GuiTextField(this.fontRendererObj, textX-4, textY, width - textX * 2, 40);
        text.setMaxStringLength(255);
        text.setText("");
        this.text.setFocused(false);
        // buttonList.add(new GuiButton(13, width / 2 - 48, 146, 96, 20, StatCollector.translateToLocal("class13.name")));
    }

    @Override
    public void onGuiClosed() {
        if (closedWithButton && pClass != PlayerClass.NONE) {
            FMLProxyPacket packet = SkillPacketHandler.getPacket(Side.SERVER, PacketChannel.LEVELUPCLASSES, (byte) pClass.getId());
            LevelUp.classChannel.sendToServer(packet);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) {
            closedWithButton = true;
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else if (guibutton.id == 100) {
            closedWithButton = false;
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else {
            for (Object obj : buttonList) {
                GuiButton button = (GuiButton) obj;
                button.enabled = true;
            }
            guibutton.enabled = false;
            pClass = EnumUtils.getPlayerClassFromId(guibutton.id);
            //text.setText(StatCollector.translateToLocal("class." + pClass.name() + ".tooltip"));
        }
    }
}
