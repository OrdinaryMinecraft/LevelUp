package ru.flametaichou.levelup;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import ru.flametaichou.levelup.Handlers.SkillPacketHandler;
import ru.flametaichou.levelup.Model.PacketChannel;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.Util.EnumUtils;

public final class GuiSkills extends GuiScreen {
    private boolean closedWithButton;
    private final static int offset = 80;
    private final int[] skills = new int[PlayerSkill.values().length];
    private int[] skillsPrev = null;
    PlayerClass pClass = PlayerClass.NONE;

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        System.out.println(guibutton.id);
        if (guibutton.id == 0) {
            closedWithButton = true;
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else if (guibutton.id == 100) {
            closedWithButton = false;
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else if (guibutton.id < skills.length) {
            if (getSkillOffset(PlayerSkill.EXP.getId()) > 0 && getSkillOffset(guibutton.id) < ClassBonus.getMaxSkillPoints()) {
                skills[guibutton.id]++;
                skills[PlayerSkill.EXP.getId()]--;
            }
        } else if (guibutton.id > 20 && skills[guibutton.id - 20] > 0) {
            skills[guibutton.id - 20]--;
            skills[PlayerSkill.EXP.getId()]++;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        drawDefaultBackground();
        String s = "";
        String s1 = "";
        for (Object button : buttonList) {
            PlayerSkill skill = EnumUtils.getPlayerSkillFromId(((GuiButton) button).id);
            if (skill != null) {
                if (skill.getId() < 1 || skill.getId() > 99) {
                    continue;
                }
                if (skill.getId() > 20) {
                    skill = null;
                }
                if (((GuiButton) button).mousePressed(mc, i, j)) {
                    s = StatCollector.translateToLocal("skill." + skill + ".tooltip1");
                    s1 = StatCollector.translateToLocal("skill." + skill + ".tooltip2");
                }
            }
        }
        if (pClass == PlayerClass.NONE)
            pClass = PlayerExtendedProperties.getPlayerClass(mc.thePlayer);
        else {
            drawCenteredString(fontRendererObj, StatCollector.translateToLocalFormatted("hud.skill.text2", StatCollector.translateToLocal("class." + pClass.name() + ".name")), width / 2, 2, 0xffffff);
        }
        // x < 4 - количество строк
        for (int x = 0; x < 6; x++) {
            drawCenteredString(fontRendererObj, StatCollector.translateToLocal("skill." + EnumUtils.getPlayerSkillFromId(x + 1) + ".name") + ": " + getSkillOffset(x + 1), width / 2 - offset, 20 + 32 * x, 0xffffff);
            drawCenteredString(fontRendererObj, StatCollector.translateToLocal("skill." + EnumUtils.getPlayerSkillFromId(x + 6) + ".name") + ": " + getSkillOffset(x + 5), width / 2 + offset, 20 + 32 * x, 0xffffff);
        }
        drawCenteredString(fontRendererObj, s, width / 2, height / 6 + 168, 0xffffff);
        drawCenteredString(fontRendererObj, s1, width / 2, height / 6 + 180, 0xffffff);
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
        for (int index = 0; index < 6; index++) {
            buttonList.add(new GuiButton(1 + index, (width / 2 + 44) - offset, 15 + 32 * index, 20, 20, "+"));
            buttonList.add(new GuiButton(7 + index, width / 2 + 44 + offset, 15 + 32 * index, 20, 20, "+"));
            buttonList.add(new GuiButton(21 + index, width / 2 - 64 - offset, 15 + 32 * index, 20, 20, "-"));
            buttonList.add(new GuiButton(27 + index, (width / 2 - 64) + offset, 15 + 32 * index, 20, 20, "-"));
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
