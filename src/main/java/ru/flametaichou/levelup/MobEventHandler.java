package ru.flametaichou.levelup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

public class MobEventHandler {
	
    private static List<String> itemListLoot;
    private static List<String> itemRareListLoot;
    final Random random = new Random();
    public static final MobEventHandler INSTANCE = new MobEventHandler();

	@SubscribeEvent
    public void onMobDrops(LivingDropsEvent event)
    {
		int count = 0;
		if (event.source.getSourceOfDamage() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.source.getSourceOfDamage();
	        int bonus = getSkill(player, 4);
	        //add new loot to drop
	        if (event.entity instanceof EntityMob && bonus > 0)
	        {
                double d = Math.random() * 100;
                if (d < bonus) {
    	        	count = random.nextInt(bonus / 5) + 1;
    	            int index = random.nextInt(itemListLoot.size());
    	            Item item = Item.getItemById(Integer.parseInt(itemListLoot.get(index)));
    	            ItemStack stack = new ItemStack(item, count);
    	            EntityItem drop = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, stack);
    	            event.drops.add(drop);
    	        	//System.out.println("added");
                }
                else if (d < (bonus + bonus / 5)) {
    	            int index = random.nextInt(itemRareListLoot.size());
    	            Item item = Item.getItemById(Integer.parseInt(itemRareListLoot.get(index)));
    	            ItemStack stack = new ItemStack(item);
    	            EntityItem drop = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, stack);
    	            event.drops.add(drop);
                    player.addChatComponentMessage(new ChatComponentTranslation("rare.drop"));
                }
	        }
			//double drop
	        if (event.entity instanceof EntityMob && !event.entity.getClass().getName().contains("custom") && bonus / 5 > 0)
	        {
                double d = Math.random() * 100; 
                if (d < bonus / 5 * 5) {
    	            event.drops.clone();
    	        	//System.out.println("cloned");
                }
	            event.drops.clone();
	        }
		}
    }	

    /**
     * Helper to retrieve skill points from the index
     */
    public static int getSkill(EntityPlayer player, int id) {
        return PlayerExtendedProperties.getSkillFromIndex(player, id);
    }

	/**
     * Add items to loot itemlist
     */
    public static void addItemsToList(List<String> itemlist, List<String> itemrarelist) {
        if (itemListLoot == null)
        	itemListLoot = new ArrayList<String>(itemlist.size());
        itemListLoot = itemlist;
        if (itemRareListLoot == null)
        	itemRareListLoot = new ArrayList<String>(itemrarelist.size());
        itemRareListLoot = itemrarelist;
    }
	
}