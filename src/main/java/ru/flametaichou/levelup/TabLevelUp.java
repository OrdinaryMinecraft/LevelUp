package ru.flametaichou.levelup;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class TabLevelUp extends CreativeTabs {

    public TabLevelUp(String string) {
        super(string);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem()
    {
        return LevelUp.xpTalisman;
    }

}
