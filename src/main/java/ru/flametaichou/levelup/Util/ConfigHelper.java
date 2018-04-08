package ru.flametaichou.levelup.Util;

import net.minecraftforge.common.config.Configuration;
import ru.flametaichou.levelup.Handlers.FMLEventHandler;
import ru.flametaichou.levelup.Handlers.MobEventHandler;
import ru.flametaichou.levelup.Handlers.PlayerEventHandler;
import ru.flametaichou.levelup.Items.ItemFishingLootBox;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigHelper {

    public ConfigHelper() {
    }

    public static List<String> blackList;
    public static List<String> itemList;
    public static List<String> itemRareList;
    public static List<String> itemFishingList;
    public static List<String> itemFishingLB;

    public static int maxSkillPoints;
    public static int resetSkillOnDeath;
    public static boolean respecBookRecipe;
    public static boolean xpTalismanRecipe;


    private static void setupConfig(Configuration config) {
        try {
            config.load();
            config.addCustomCategoryComment("Settings", "Mod settings.");
            config.addCustomCategoryComment("Items", "Mod items settings.");
            config.addCustomCategoryComment("Lists", "Lists of items, that uses in mod.");

            blackList = Arrays.asList(config.getStringList("Crops for farming", "Lists", new String[]{"31"}, "That won't be affected by farming growth skill, uses internal block name. No sync to client needed."));
            itemList = Arrays.asList(config.getStringList("Item for loot", "Lists", new String[]{"265","266","339","263","371","337","318","318","357"}, "Additional items dropping from mobs and looted from chests."));
            itemRareList = Arrays.asList(config.getStringList("Item for loot (rare)", "Lists", new String[]{"264","388","372","384"}, "Rare items dropping from mobs and looted from chests."));
            itemFishingList = Arrays.asList(config.getStringList("Item for fishing (rare)", "Lists", new String[]{"264","388","372","384","368","378"}, "Rare items for fishing loot."));
            itemFishingLB = Arrays.asList(config.getStringList("Item for fishing Loot Box", "Lists", new String[]{"2256","2257","2258","2259","2260","2261","2262","2263","2264","2265","2266","2267","264","388","368","418","419","322"}, "Items for fishing lootbox."));

            maxSkillPoints = config.getInt("maxSkillPoints", "Settings", 30, 10,50,"Max skill points for each skill.");
            resetSkillOnDeath = config.getInt("resetSkillOnDeath", "Settings", 0, 0,100,"How much skill points will gone on death (percent).");
            respecBookRecipe = config.getBoolean("respecBookRecipe", "Items", false, "Add recipe to craft Respec Book.");
            xpTalismanRecipe = config.getBoolean("xpTalismanRecipe", "Items", false,"Add recipe to craft XP Talisman.");
        } catch(Exception e) {
            System.out.println("A severe error has occured when attempting to load the config file for this mod!");
        } finally {
            if(config.hasChanged()) {
                config.save();
            }
        }
    }

    public static int[] getServerProperties() {
        int[] result = new int[2];
        result[0] = maxSkillPoints;
        result[1] = resetSkillOnDeath;
        return result;
    }

    public static void useServerProperties(int[] properties) {
        maxSkillPoints = properties[0];
        resetSkillOnDeath = properties[1];
    }

    public static void loadConfig(Configuration config) {
        setupConfig(config);
        FMLEventHandler.INSTANCE.addCropsToBlackList(blackList);
        MobEventHandler.addItemsToList(itemList, itemRareList);
        PlayerEventHandler.addItemsToFishingList(itemFishingList);
        ItemFishingLootBox.addItemsFishingLBList(itemFishingLB);
    }
}
