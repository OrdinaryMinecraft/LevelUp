package ru.flametaichou.levelup.Handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Model.PlayerSkill;
import ru.flametaichou.levelup.PlayerExtendedProperties;

public class MobEventHandler {
	
    private static List<String> itemListLoot;
    private static List<String> itemRareListLoot;
    public static final MobEventHandler INSTANCE = new MobEventHandler();

	@SubscribeEvent
    public void onMobDrops(LivingDropsEvent event)
    {
		// Фикс дублирования дропа с мобов из-за костыльного нанесения урона @FightEventHandler.onHurting
		if (event.entityLiving.getHealth() == -1) {
			event.entityLiving.setHealth(0);
			event.drops.set(0, null);
			event.setCanceled(true);
		} else if (!(event.entity instanceof EntityPlayer)) {
			event.entityLiving.setHealth(-1);

			int count = 0;
			if (event.source.getSourceOfDamage() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) event.source.getSourceOfDamage();
				int bonus = PlayerExtendedProperties.getSkill(player, PlayerSkill.LOOTING);

				// Looting skill bonus
				// Добавляю бонусный лут в дроп
				if (event.entity instanceof EntityMob && bonus > 0) {
					double d = Math.random() * 100;
					if (d <= bonus) {
						Random random = new Random();
						count = 1;
						if (bonus >= 5) {
							count = random.nextInt(bonus / 5) + 1;
						}
						int index = random.nextInt(itemListLoot.size());
						Item item = Item.getItemById(Integer.parseInt(itemListLoot.get(index)));
						ItemStack stack = new ItemStack(item, count);
						EntityItem drop = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, stack);
						event.drops.add(drop);
					} else if (d < (bonus + bonus / 5)) {
						Random random = new Random();
						int index = random.nextInt(itemRareListLoot.size());
						Item item = Item.getItemById(Integer.parseInt(itemRareListLoot.get(index)));
						ItemStack stack = new ItemStack(item);
						EntityItem drop = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, stack);
						event.drops.add(drop);
						player.addChatComponentMessage(new ChatComponentTranslation("rare.drop"));
					}
				}

				// Удваиваем дроп. Только если моб - не из мода CustomNPCs
				if (event.entity instanceof EntityMob && !event.entity.getClass().getName().contains("custom") && bonus / 5 > 0) {
					double d = Math.random() * 100;
					if (d < bonus / 5) {
						event.drops.clone();
						player.addChatComponentMessage(new ChatComponentTranslation("drop.double"));
					}
				}

				// Hunter class bonus
                PlayerClass pClass = PlayerExtendedProperties.getPlayerClass(player);
                if (event.entity instanceof EntityAnimal && pClass == PlayerClass.HUNTER) {
                    if (Math.random() < 0.15) {
                        event.drops.clone();
						player.addChatComponentMessage(new ChatComponentTranslation("drop.double"));
                    }
                }
			}
		}
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
