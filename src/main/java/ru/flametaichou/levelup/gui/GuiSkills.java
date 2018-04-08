package ru.flametaichou.levelup.gui;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import ru.flametaichou.levelup.Handlers.SkillPacketHandler;
import ru.flametaichou.levelup.LevelUp;
import ru.flametaichou.levelup.Model.PacketChannel;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.PlayerExtendedProperties;
import ru.flametaichou.levelup.Util.ConfigHelper;
import ru.flametaichou.levelup.Util.EnumUtils;

public final class GuiSkills extends GuiScreen {
    private boolean closedWithButton;
    private final static int offset = 70;
    private final int[] skills = new int[PlayerSkill.values().length];
    private int[] skillsPrev = null;
    PlayerClass pClass = PlayerClass.NONE;
    private static final ResourceLocation texture = new ResourceLocation(LevelUp.ID, "textures/gui/lvlup-gui.png");


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
        } else if (guibutton.id == 101) {
            for (int i = 0; i < skills.length; i++)
                skills[i] = 0;
        } else if (guibutton.id < skills.length) {
            if (getSkillOffset(PlayerSkill.EXP.getId()) > 0 && getSkillOffset(guibutton.id) < ConfigHelper.maxSkillPoints) {
                skills[guibutton.id]++;
                skills[PlayerSkill.EXP.getId()]--;
            }
        }
        initGui();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        drawDefaultBackground();
        if (pClass == PlayerClass.NONE)
            pClass = PlayerExtendedProperties.getPlayerClass(mc.thePlayer);
        else {
            drawCenteredString(fontRendererObj, StatCollector.translateToLocalFormatted("hud.skill.text2", StatCollector.translateToLocal("class." + pClass.name() + ".name")), width / 2, 2, 0xffffff);
        }
        // x < 4 - количество строк
        for (int x = 0; x < 6; x++) {

            int xPosition = width / 2 - offset - 50;
            int yPosition = 20 + 32 * x;

            mc.getTextureManager().bindTexture(texture);
            drawTexturedModalRect(xPosition, yPosition, 0, 32, 50, 24);
            if (x != 5)
                drawTexturedModalRect(xPosition + 40, yPosition + 10, 0, 58, 160, 35);
            else
                drawTexturedModalRect(xPosition + 40, yPosition + 10, 0, 58, 81, 35);
            mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
            drawTexturedModelRectFromIcon(xPosition + 2, yPosition + 2, EnumUtils.getPlayerSkillFromId(x + 1).getIcon(), 20, 20);
            drawCenteredString(fontRendererObj, StatCollector.translateToLocal("skill." + EnumUtils.getPlayerSkillFromId(x + 1) + ".name"), xPosition + 85, yPosition, 0xffffff);


        }
        for (int x = 6; x < 11; x++) {

            int xPosition = width / 2 + offset;
            int yPosition = 20 + 32 * (x - 6);

            mc.getTextureManager().bindTexture(texture);
            drawTexturedModalRect(xPosition, yPosition, 0, 32, 50, 24);
            mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
            drawTexturedModelRectFromIcon(xPosition + 28, yPosition + 2, EnumUtils.getPlayerSkillFromId(x + 1).getIcon(), 20, 20);
            drawCenteredString(fontRendererObj, StatCollector.translateToLocal("skill." + EnumUtils.getPlayerSkillFromId(x + 1) + ".name"), xPosition - 35, yPosition, 0xffffff);
        }
        String s = "";
        String s1 = "";
        String s2 = "";
        for (Object button : buttonList) {
            ((GuiButton) button).visible = true;
        }
        for (Object button : buttonList) {
            PlayerSkill skill = EnumUtils.getPlayerSkillFromId(((GuiButton) button).id);
            if (skill != null) {
                if (skill.getId() < 1 || skill.getId() > 99) {
                    continue;
                }
                if (skill.getId() > 20) {
                    skill = null;
                }
                mc.getTextureManager().bindTexture(texture);
                if (((GuiButton) button).mousePressed(mc, i, j)) {
                    for (Object btn : buttonList) {
                        if (((GuiButton) button).id <= 6 && (((GuiButton) btn).id == 7 || ((GuiButton) btn).id == 8))
                            ((GuiButton) btn).visible = false;
                        if (((GuiButton) button).id > 6 && (((GuiButton) btn).id == 1 || ((GuiButton) btn).id == 2))
                            ((GuiButton) btn).visible = false;
                    }
                    s = StatCollector.translateToLocal("skill." + skill + ".tooltip1");
                    s1 = StatCollector.translateToLocal("skill." + skill + ".tooltip2");
                    s2 = StatCollector.translateToLocal("skill." + skill + ".tooltip3");
                    if (((GuiButton) button).id <= 6) {
                        drawTexturedModalRect(width / 2 - offset, 20, 0, 93, 256, 64);
                        drawString(fontRendererObj, s, width / 2 - offset + 4, 20 + 2, 0xffffff);
                        drawString(fontRendererObj, s1, width / 2 - offset + 4, 20 + 12, 0xffffff);
                        drawString(fontRendererObj, s2, width / 2 - offset + 4, 20 + 22, 0xffffff);
                    }
                    if (((GuiButton) button).id > 6) {
                        drawTexturedModalRect(width / 2 + offset - 256, 20, 0, 93, 256, 64);
                        drawString(fontRendererObj, s, width / 2 + offset - 256 + 4, 20 + 2, 0xffffff);
                        drawString(fontRendererObj, s1, width / 2 + offset - 256 + 4, 20 + 12, 0xffffff);
                        drawString(fontRendererObj, s2, width / 2 + offset - 256 + 4, 20 + 22, 0xffffff);
                    }
                }
            }
        }
        drawCenteredString(fontRendererObj, StatCollector.translateToLocalFormatted("gui.skill.left", skillsPrev[PlayerSkill.EXP.getId()] + skills[PlayerSkill.EXP.getId()]), width / 2, height / 6 + 185, 0xffffff);
        super.drawScreen(i, j, f);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        closedWithButton = false;
        buttonList.clear();
        updateSkillList();
        buttonList.add(new GuiButton(0, width / 2 + 96, height / 6 + 168, 96, 20, StatCollector.translateToLocal("gui.done")));
        buttonList.add(new GuiButton(100, width / 2 - 192, height / 6 + 168, 96, 20, StatCollector.translateToLocal("gui.cancel")));
        buttonList.add(new GuiButton(101, width / 2 - 24, height / 6 + 164, 48, 20, StatCollector.translateToLocal("gui.reset")));

        for (int index = 0; index < 6; index++) {
            int xPosition = width / 2 - offset - 50;
            int yPosition = 20 + 32 * index;
            Integer skillOffset = getSkillOffset(index + 1);
            buttonList.add(new GuiButton(1 + index, xPosition + 28, yPosition + 2, 20, 20, skillOffset.toString()));

        }
        for (int index = 6; index < 11; index++) {
            int xPosition = width / 2 + offset;
            int yPosition = 20 + 32 * (index - 6);
            Integer skillOffset = getSkillOffset(index + 1);
            buttonList.add(new GuiButton(1 + index, xPosition + 2, yPosition + 2, 20, 20, skillOffset.toString()));

        }
    }

    @Override
    public void onGuiClosed() {
        if (closedWithButton && skills[PlayerSkill.EXP.getId()] != 0) {
            FMLProxyPacket packet = SkillPacketHandler.getPacket(Side.SERVER, PacketChannel.LEVELUPSKILLS, (byte) -1, skills);
            LevelUp.skillChannel.sendToServer(packet);
        }
    }

    private void updateSkillList() {
        if (skillsPrev == null) {
            skillsPrev = new int[skills.length];
            for (int i = 0; i < skills.length; i++) {
                skillsPrev[i] = PlayerExtendedProperties.getSkill(mc.thePlayer, EnumUtils.getPlayerSkillFromId(i));
            }
        }
    }

    private int getSkillOffset(int i) {
        return skillsPrev[i] + skills[i];
    }
}
