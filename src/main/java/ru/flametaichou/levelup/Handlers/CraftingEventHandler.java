package ru.flametaichou.levelup.Handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CraftingEventHandler {

    public static final CraftingEventHandler INSTANCE = new CraftingEventHandler();

    @SubscribeEvent
    public void onCrafting(cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent event) {
//        if(e.crafting.getItem().equals(Tools.RubyAxe)){
//            e.player.addStat(Achievements.achievementRubyAxe, 1);
//        }
        writeSmithInfo(event.crafting);
    }

    public static void writeSmithInfo(ItemStack craftedItem) {
        NBTTagCompound tagCompound = craftedItem.getTagCompound();
        if (tagCompound == null)
            tagCompound = new NBTTagCompound();
        craftedItem.setTagCompound(tagCompound);
        tagCompound.setString("Smith", "name");
        craftedItem.setTagCompound(tagCompound);
    }
}
