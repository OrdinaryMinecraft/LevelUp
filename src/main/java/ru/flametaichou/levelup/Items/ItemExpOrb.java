package ru.flametaichou.levelup.Items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.PlayerExtendedProperties;

import java.util.List;

public class ItemExpOrb extends Item {

    public ItemExpOrb() {
        super();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
        if (itemstack.getTagCompound() != null) {
            if (itemstack.getTagCompound().getString("Owner") != "") {
                if (itemstack.getTagCompound().getString("Owner").equals(entityplayer.getDisplayName())) {
                    PlayerExtendedProperties.from(entityplayer).addToSkill(PlayerSkill.EXP, 1);
                    entityplayer.worldObj.playSoundEffect(entityplayer.posX, entityplayer.posY, entityplayer.posZ, "random.levelup", 1.0F, 1.0F);
                    if (!entityplayer.capabilities.isCreativeMode)
                        itemstack.stackSize--;
                } else {
                    entityplayer.addChatComponentMessage(new ChatComponentTranslation("item.expOrb.notOwner"));
                }
            }
        }
        return itemstack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean isAdvanced) {
        list.add(StatCollector.translateToLocal("item.expOrb.tooltip1"));
        list.add(StatCollector.translateToLocal("item.expOrb.tooltip2"));
    }
}

