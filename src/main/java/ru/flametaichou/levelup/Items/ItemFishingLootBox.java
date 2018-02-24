package ru.flametaichou.levelup.Items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.FishingHooks;
import ru.flametaichou.levelup.FMLEventHandler;
import ru.flametaichou.levelup.PlayerExtendedProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemFishingLootBox extends Item {

    private static List<String> fishingLBItemList;
    private static Random random = new Random();

    public ItemFishingLootBox() {
        super();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
        if (!entityplayer.worldObj.isRemote) {

            //Enchantment enchantment = Enchantment.enchantmentsBookList[random.nextInt(Enchantment.enchantmentsBookList.length)];
            //int l = MathHelper.getRandomIntegerInRange(random, enchantment.getMinLevel(), 2);
            //ItemStack book = new ItemStack(Items.book, 1, 0); // will become an Enchanted Book after next method
            //EnchantmentHelper.addRandomEnchantment(random, itemstack, 30); // last parameter is how powerful of an enchantment you want, roughly

            ItemStack lootItem = null;
            int itemCount = random.nextInt(3) + 1;
            for (int i = 0; i <= itemCount; i++) {
                if (random.nextInt(3) == 1) {
                    lootItem = new ItemStack(Items.book, 1);
                    EnchantmentHelper.addRandomEnchantment(random, lootItem, 6);
                } else {
                    int itemNum = random.nextInt(fishingLBItemList.size());
                    lootItem = new ItemStack (Item.getItemById(Integer.parseInt(fishingLBItemList.get(itemNum))));
                }
                EntityItem loot = new EntityItem(entityplayer.worldObj, entityplayer.posX, entityplayer.posY, entityplayer.posZ, lootItem);
                entityplayer.worldObj.spawnEntityInWorld(loot);
            }
        }

        if (!entityplayer.capabilities.isCreativeMode)
            itemstack.stackSize--;
        return itemstack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean isAdvanced) {
        if (itemStack.getItemDamage() > 0) {
            list.add(StatCollector.translateToLocal("lootbox.fishing"));
        }
    }

    public static void addItemsFishingLBList(List<String> itemlist) {
        if (fishingLBItemList == null)
            fishingLBItemList = new ArrayList<String>(itemlist.size());
        fishingLBItemList = itemlist;
    }
}