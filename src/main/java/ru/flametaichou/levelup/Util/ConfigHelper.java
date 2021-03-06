package ru.flametaichou.levelup.Util;

import net.minecraftforge.common.config.Configuration;
import ru.flametaichou.levelup.Handlers.FMLEventHandler;
import ru.flametaichou.levelup.Handlers.MobEventHandler;
import ru.flametaichou.levelup.Handlers.PlayerEventHandler;
import ru.flametaichou.levelup.Items.ItemFishingLootBox;

import java.util.Arrays;
import java.util.List;

public class ConfigHelper {

    public ConfigHelper() {
    }

    public static List<String> blackList;
    public static List<String> itemList;
    public static List<String> itemRareList;
    public static List<String> itemFishingList;
    public static List<String> itemFishingLB;
    public static List<String> stealBlackList;

    public static int maxSkillPoints;
    public static boolean respecBookRecipe;
    public static boolean xpTalismanRecipe;
    public static boolean resetClassOnDeath;
    public static boolean resetSkillsOnDeath;
    public static int percentSkillOnDeath;
    public static int activeSkillCooldown;

    public static long thiefKillTime = 200;


    private static void setupConfig(Configuration config) {
        try {
            config.load();
            config.addCustomCategoryComment("Settings", "Mod settings.");
            config.addCustomCategoryComment("Items", "Mod items settings.");
            config.addCustomCategoryComment("Lists", "Lists of items, that uses in mod.");

            blackList = Arrays.asList(config.getStringList("Crops blacklist for farming", "Lists", new String[]{"31", "37", "38"}, "That won't be affected by farming growth skill, uses internal block name. No sync to client needed."));
            itemList = Arrays.asList(config.getStringList("Item for looting", "Lists", new String[]{"265","266","339","263","371","337","318","318","357"}, "Additional items dropping from mobs and looted from chests."));
            itemRareList = Arrays.asList(config.getStringList("Item for looting (rare)", "Lists", new String[]{"264","388","372","384"}, "Rare items dropping from mobs and looted from chests."));
            itemFishingList = Arrays.asList(config.getStringList("Item for fishing (rare)", "Lists", new String[]{"264","388","372","384","368","378"}, "Rare items for fishing loot."));
            itemFishingLB = Arrays.asList(config.getStringList("Item for fishing Loot Box", "Lists", new String[]{"2256","2257","2258","2259","2260","2261","2262","2263","2264","2265","2266","2267","264","388","368","418","419","322"}, "Items for fishing lootbox."));
            stealBlackList = Arrays.asList(config.getStringList("Containers blacklist for stealing", "Lists", new String[]{"999"}, "List of containers protected from theft (block IDs)."));

            maxSkillPoints = config.getInt("maxSkillPoints", "Settings", 30, 10,50,"Max skill points for each skill.");
            percentSkillOnDeath = config.getInt("percentSkillOnDeath", "Settings", 0, 0,100,"How much skill points will gone on death (percent).");
            activeSkillCooldown = config.getInt("activeSkillCooldown", "Settings", 120, 0,9999,"Class active skill cooldown (seconds).");
            respecBookRecipe = config.getBoolean("respecBookRecipe", "Items", false, "Add recipe to craft Respec Book.");
            xpTalismanRecipe = config.getBoolean("xpTalismanRecipe", "Items", false,"Add recipe to craft XP Talisman.");
            resetClassOnDeath = config.getBoolean("resetClassOnDeath", "Settings", false,"Reset player class on death?");
            resetSkillsOnDeath = config.getBoolean("resetSkillsOnDeath", "Settings", false,"Reset player skills on death?");
        } catch(Exception e) {
            System.out.println("A severe error has occured when attempting to load the config file for this mod!");
        } finally {
            if(config.hasChanged()) {
                config.save();
            }
        }
    }

    public static int[] getServerProperties() {
        int[] result = new int[5];
        result[0] = maxSkillPoints;
        result[1] = percentSkillOnDeath;
        if (resetClassOnDeath)
            result[2] = 1;
        else
            result[2] = 0;
        if (resetSkillsOnDeath)
            result[3] = 1;
        else
            result[3] = 0;
        result[4] = activeSkillCooldown;
        return result;
    }

    public static void useServerProperties(int[] properties) {
        maxSkillPoints = properties[0];
        percentSkillOnDeath = properties[1];
        if (properties[2] == 1)
            resetClassOnDeath = true;
        else if (properties[2] == 0)
            resetClassOnDeath = false;
        if (properties[3] == 1)
            resetSkillsOnDeath = true;
        else if (properties[3] == 0)
            resetSkillsOnDeath = false;
        activeSkillCooldown = properties[4];
    }

    public static void loadConfig(Configuration config) {
        setupConfig(config);
        FMLEventHandler.INSTANCE.addCropsToBlackList(blackList);
        MobEventHandler.addItemsToList(itemList, itemRareList);
        PlayerEventHandler.addItemsToFishingList(itemFishingList);
        ItemFishingLootBox.addItemsFishingLBList(itemFishingLB);
    }
}
