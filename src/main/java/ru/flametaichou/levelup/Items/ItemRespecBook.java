package ru.flametaichou.levelup.Items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.flametaichou.levelup.Handlers.FMLEventHandler;
import ru.flametaichou.levelup.PlayerExtendedProperties;

import java.util.List;

public final class ItemRespecBook extends Item {
    public ItemRespecBook() {
        super();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
        if (!world.isRemote) {
            PlayerExtendedProperties.from(entityplayer).convertPointsToXp(true);
            FMLEventHandler.INSTANCE.loadPlayer(entityplayer);
        }
        if (!entityplayer.capabilities.isCreativeMode)
            itemstack.stackSize--;
        return itemstack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean isAdvanced) {
        list.add(StatCollector.translateToLocal("item.respecBook.tooltip1"));
        list.add(StatCollector.translateToLocal("item.respecBook.tooltip2"));
        list.add(StatCollector.translateToLocal("item.respecBook.tooltip3"));
    }
}
