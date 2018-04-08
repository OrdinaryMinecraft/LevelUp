package ru.flametaichou.levelup;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import ru.flametaichou.levelup.Handlers.FMLEventHandler;
import ru.flametaichou.levelup.Handlers.PlayerEventHandler;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.Util.ConfigHelper;

import java.awt.*;
import java.util.List;

public final class LevelUpHUD extends Gui {
    public static final LevelUpHUD INSTANCE = new LevelUpHUD();

    public void addToText(List<String> left) {
        PlayerClass playerClass = PlayerExtendedProperties.getPlayerClass(LevelUp.proxy.getPlayer());
        if (playerClass != PlayerClass.NONE) {
        	int skillXP = PlayerExtendedProperties.from(LevelUp.proxy.getPlayer()).getSkillFromIndex(PlayerSkill.EXP);
            if (skillXP > 0)
                left.add(StatCollector.translateToLocalFormatted("hud.skill.text2", StatCollector.translateToLocal("class." + playerClass + ".name")) + " (очки умений: " + skillXP + ")");
            else
            	left.add(StatCollector.translateToLocalFormatted("hud.skill.text2", StatCollector.translateToLocal("class." + playerClass + ".name")));
        } else if (canSelectClass()) {
            left.add(StatCollector.translateToLocal("hud.skill.select"));
        }
    }

    @SubscribeEvent
    public void renderLvlUpHUD(RenderGameOverlayEvent.Pre event) {
        if (LevelUp.proxy.getPlayer() != null) {
            if (event.type == ElementType.TEXT)
                addToText(((RenderGameOverlayEvent.Text) event).left);
        }
    }

    @SubscribeEvent
    public void onFOV(FOVUpdateEvent event){
        if(!event.entity.isUsingItem()) {
            int skill = 0;
            if(event.entity.isSneaking()){
                skill = 2 * PlayerExtendedProperties.getSkill(event.entity, PlayerSkill.SNEAKING);
            }else if(event.entity.isSprinting()){
                skill = PlayerExtendedProperties.getSkill(event.entity, PlayerSkill.ATHLETICS);
            }
            if(skill > 0){
                event.newfov -= 0.5F;
                event.newfov *=  1/(1.0F + skill / 100F);
                event.newfov += 0.5F;
            }
        }
    }

    public static boolean canSelectClass() {
        if (LevelUp.proxy.getPlayer().experienceLevel >= PlayerEventHandler.minLevel)
            return true;
        else {
            int points = PlayerExtendedProperties.from(LevelUp.proxy.getPlayer()).getSkillPoints();
            return points > PlayerEventHandler.minLevel * PlayerEventHandler.xpPerLevel || points > ConfigHelper.maxSkillPoints;
        }
    }

    public static boolean canShowSkills() {
        return PlayerExtendedProperties.from(LevelUp.proxy.getPlayer()).hasClass();
    }
}
