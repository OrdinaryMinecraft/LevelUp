package ru.flametaichou.levelup;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.ShapedOreRecipe;
import ru.flametaichou.levelup.Handlers.*;
import ru.flametaichou.levelup.Items.ItemFishingLootBox;
import ru.flametaichou.levelup.Items.ItemRespecBook;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Mod(modid = LevelUp.ID, name = "Level Up! (Ordinary Edition)", version = "${version}", guiFactory = "ru.flametaichou.levelup.ConfigLevelUp")
public final class LevelUp {
    public final static String ID = "levelup";
    @Instance(value = ID)
    public static LevelUp instance;
    @SidedProxy(clientSide = "ru.flametaichou.levelup.SkillClientProxy", serverSide = "ru.flametaichou.levelup.SkillProxy")
    public static SkillProxy proxy;
    private Property[] clientProperties;
    private Property[] serverProperties;
    private static Item xpTalisman;
    private static Item respecBook;
    public static Item fishingLootBox;
    private static Configuration config;
    public static boolean allowHUD = true, renderTopLeft = true, renderExpBar = false, changeFOV = true;
    private static boolean bonusMiningXP = true, bonusSmeltingXP = true, bonusRandomXP = true, bonusFightingXP = true, oreMiningXP = true;
    public static FMLEventChannel initChannel, skillChannel, classChannel, configChannel;
	public static boolean debugMode;

    @EventHandler
    public void load(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(BowEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FightEventHandler.INSTANCE);
        SkillPacketHandler sk = new SkillPacketHandler();
        initChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[0]);
        initChannel.register(sk);
        classChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[1]);
        classChannel.register(sk);
        skillChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[2]);
        skillChannel.register(sk);
        configChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[3]);
        configChannel.register(sk);
        proxy.registerGui();
    }

    @EventHandler
    public void load(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.addCustomCategoryComment("HUD", "Entirely client side. No need to sync.");
        initClientProperties();
        config.addCustomCategoryComment("Items", "Need to be manually synced to the client on a dedicated server");
        config.addCustomCategoryComment("Cheats", "Will be automatically synced to the client on a dedicated server");
        initServerProperties();
        boolean talismanEnabled = config.getBoolean("Enable Talisman", "Items", true, "Enable item and recipe");
        boolean bookEnabled = config.getBoolean("Enable Unlearning Book", "Items", true, "Enable item and related recipe");
        useServerProperties();
        List<String> blackList = Arrays.asList(config.getStringList("Crops for farming", "BlackList", new String[]{"31"}, "That won't be affected by farming growth skill, uses internal block name. No sync to client needed."));
        FMLEventHandler.INSTANCE.addCropsToBlackList(blackList);
        List<String> itemList = Arrays.asList(config.getStringList("Item for loot", "ItemList", new String[]{"265","266","339","263","371","337","318","318","357"}, "Additional items dropping from mobs and looted from chests."));
        List<String> itemRareList = Arrays.asList(config.getStringList("Item for loot (rare)", "itemRareList", new String[]{"264","388","372","384"}, "Rare items dropping from mobs and looted from chests."));
        List<String> itemFishingList = Arrays.asList(config.getStringList("Item for fishing (rare)", "itemFishingList", new String[]{"264","388","372","384","368","378"}, "Rare items for fishing loot."));
        List<String> itemFishingLB = Arrays.asList(config.getStringList("Item for fishing Loot Box", "itemFishingLB", new String[]{"2256","2257","2258","2259","2260","2261","2262","2263","2264","2265","2266","2267","264","388","368","418","419","322"}, "Items for fishing lootbox."));
        debugMode = config.getBoolean("Enable Debug Mode", "Cheats", false, "Enable debug messages in chat");
        MobEventHandler.addItemsToList(itemList, itemRareList);
        PlayerEventHandler.addItemsToFishingList(itemFishingList);
        ItemFishingLootBox.addItemsFishingLBList(itemFishingLB);
        if (config.hasChanged())
            config.save();

        fishingLootBox = new ItemFishingLootBox().setUnlocalizedName("fishingLootBox").setTextureName(ID + ":FishingLootBox").setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerItem(fishingLootBox, "fishingLootBox");

        if (talismanEnabled) {
            xpTalisman = new Item().setUnlocalizedName("xpTalisman").setTextureName(ID + ":XPTalisman").setCreativeTab(CreativeTabs.tabMisc);
            GameRegistry.registerItem(xpTalisman, "xpTalisman");
            GameRegistry.addRecipe(new ShapedOreRecipe(xpTalisman, "GG ", " R ", " GG", 'G', Items.gold_ingot, 'R', Items.ender_pearl));
        }

        if (bookEnabled) {
            respecBook = new ItemRespecBook().setUnlocalizedName("respecBook").setTextureName(ID + ":RespecBook").setCreativeTab(CreativeTabs.tabMisc);
            GameRegistry.registerItem(respecBook, "respecBook");
            ItemStack output = new ItemStack(respecBook);
            if (config.getBoolean("unlearning Book Reset Class", "Cheats", true, "Should unlearning book also remove class")) {
                output.setItemDamage(1);
            }
            GameRegistry.addRecipe(output, "OEO", "DBD", "ODO", 'O', Blocks.obsidian, 'D', new ItemStack(Items.dye),
                    'E', Items.ender_pearl, 'B', Items.book);
        }

        if (event.getSourceFile().getName().endsWith(".jar")) {
            proxy.tryUseMUD();
        }

        FMLCommonHandler.instance().bus().register(FMLEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
        MinecraftForge.EVENT_BUS.register(new MobEventHandler());
    }

    private void initClientProperties() {
        clientProperties = new Property[]{
                config.get("HUD", "allow HUD", allowHUD, "If anything should be rendered on screen at all.").setRequiresMcRestart(true),
                config.get("HUD", "render HUD on Top Left", renderTopLeft),
                config.get("HUD", "render HUD on Exp Bar", renderExpBar),
                config.get("FOV", "speed based", changeFOV, "Should FOV change based on player speed from athletics / sneak skills." )};
        allowHUD = clientProperties[0].getBoolean();
        renderTopLeft = clientProperties[1].getBoolean();
        renderExpBar = clientProperties[2].getBoolean();
        changeFOV = clientProperties[3].getBoolean();
    }

    private void initServerProperties() {
        String cat = "Cheats";
        String limitedBonus = "This is a bonus related to a few classes";
        serverProperties = new Property[]{
                config.get(cat, "Max points per skill", ClassBonus.getMaxSkillPoints(), "Minimum is 1"),
                config.get(cat, "Bonus points for classes", ClassBonus.getBonusPoints(), "Points given when choosing a class, allocated automatically.\n Minimum is 0, Maximum is max points per skill times 2"),
                config.get(cat, "Xp gain per level", PlayerEventHandler.xpPerLevel, "Minimum is 0"),
                config.get(cat, "Skill points lost on death", (int) PlayerEventHandler.resetSkillOnDeath * 100, "How much skill points are lost on death, in percent.").setMinValue(0).setMaxValue(100),
                config.get(cat, "Use old speed for dirt and gravel digging", PlayerEventHandler.oldSpeedDigging),
                config.get(cat, "Use old speed for redstone breaking", PlayerEventHandler.oldSpeedRedstone, "Makes the redstone ore mining efficient"),
                config.get(cat, "Reset player class on death", PlayerEventHandler.resetClassOnDeath, "Do the player lose the class he choose on death ?"),
                config.get(cat, "Prevent duplicated ores placing", PlayerEventHandler.noPlaceDuplicate, "Some skill duplicate ores, this prevent infinite duplication by replacing"),
                config.get(cat, "Add Bonus XP on Smelt", bonusSmeltingXP, limitedBonus),
                config.get(cat, "Add Bonus XP on Mining", bonusMiningXP, limitedBonus),
                config.get(cat, "Add XP on Mining some ore", oreMiningXP, "This is a global bonus, limited to a few ores"),
                config.get(cat, "Add Bonus XP on Fighting", bonusFightingXP, limitedBonus)};
    }

    public void useServerProperties() {
        ClassBonus.setSkillMax(serverProperties[0].getInt());
        ClassBonus.setBonusPoints(serverProperties[1].getInt());
        double opt = serverProperties[2].getDouble();
        if (opt >= 0.0D)
            PlayerEventHandler.xpPerLevel = opt <= ClassBonus.getMaxSkillPoints() ? opt : ClassBonus.getMaxSkillPoints();
        PlayerEventHandler.resetSkillOnDeath = (float) serverProperties[3].getInt() / 100.00F;
        PlayerEventHandler.oldSpeedDigging = serverProperties[4].getBoolean();
        PlayerEventHandler.oldSpeedRedstone = serverProperties[5].getBoolean();
        PlayerEventHandler.resetClassOnDeath = serverProperties[6].getBoolean();
        PlayerEventHandler.noPlaceDuplicate = serverProperties[7].getBoolean();
        bonusSmeltingXP = serverProperties[8].getBoolean();
        bonusMiningXP = serverProperties[9].getBoolean();
        oreMiningXP = serverProperties[10].getBoolean();
        bonusFightingXP = serverProperties[11].getBoolean();
    }

    public Property[] getServerProperties() {
        return serverProperties;
    }

    public boolean[] getClientProperties() {
        boolean[] result = new boolean[clientProperties.length];
        for (int i = 0; i < clientProperties.length; i++) {
            result[i] = clientProperties[i].getBoolean();
        }
        return result;
    }

    public void refreshValues(boolean[] values) {
        if (values.length == clientProperties.length) {
            LevelUp.allowHUD = values[0];
            LevelUp.renderTopLeft = values[1];
            LevelUp.renderExpBar = values[2];
            LevelUp.changeFOV = values[3];
            for (int i = 0; i < values.length; i++) {
                clientProperties[i].set(values[i]);
            }
            config.save();
        }
    }

    @EventHandler
    public void remap(FMLMissingMappingsEvent event) {
        for (FMLMissingMappingsEvent.MissingMapping missingMapping : event.get()) {
            if (missingMapping.name.equals(ID + ":Talisman of Wonder")) {
                missingMapping.remap(xpTalisman);
            } else if (missingMapping.name.equals(ID + ":Book of Unlearning")) {
                missingMapping.remap(respecBook);
            }
        }
    }

    public static void giveBonusSmeltingXP(EntityPlayer player) {
	    if (bonusSmeltingXP) {
	        byte pClass = PlayerExtendedProperties.getPlayerClass(player);
	        if (pClass == 2) {
	            runBonusCounting(player, 1);
	        }
	    }
    }

    public static void giveBonusFightingXP(EntityPlayer player) {
        if (bonusFightingXP) {
            byte pClass = PlayerExtendedProperties.getPlayerClass(player);
            if (pClass == 8) {
                player.addExperience(2);
            }
        }
    }

    public static void giveBonusFishingXP(EntityPlayer player) {
        byte pClass = PlayerExtendedProperties.getPlayerClass(player);
        if (pClass == 9) {
            player.addExperience(2);
        }
    }

    public static void giveBonusMiningXP(EntityPlayer player) {
        if (bonusMiningXP) {
            byte pClass = PlayerExtendedProperties.getPlayerClass(player);
            if (pClass == 1) {
                runBonusCounting(player, 0);
            }
        }
    }
    
    public static void giveBonusRandomXP(EntityPlayer player) {
    if (bonusRandomXP) {
        byte pClass = PlayerExtendedProperties.getPlayerClass(player);
        if (pClass == 13 || pClass == 10) {
            player.addExperience(1);
       		}
    	}
    	if (player.inventory.hasItem(xpTalisman)) {
    		player.addExperience(2);
   		}
    }

    private static void runBonusCounting(EntityPlayer player, int type) {
        Map<String, int[]> counters = PlayerExtendedProperties.getCounterMap(player);
        int[] bonus = counters.get(PlayerExtendedProperties.counters[2]);
        if (bonus == null || bonus.length == 0) {
            bonus = new int[]{0, 0, 0};
        }
        if (bonus[type] < 4) {
            bonus[type]++;
        } else {
            bonus[type] = 0;
            player.addExperience(2);
        }
        counters.put(PlayerExtendedProperties.counters[2], bonus);
    }

    public static void incrementOreCounter(EntityPlayer player, int i) {
        if (oreMiningXP) {
            Map<String, int[]> counters = PlayerExtendedProperties.getCounterMap(player);
            int[] ore = counters.get(PlayerExtendedProperties.counters[0]);
            if (ore.length <= i) {
                int[] orenew = new int[i + 1];
                System.arraycopy(ore, 0, orenew, 0, ore.length);
                counters.put(PlayerExtendedProperties.counters[0], orenew);
                ore = orenew;
            }
            ore[i]++;
            float f = (float) Math.pow(2D, 3 - i) / 2.0F;
            boolean flag;
            for (flag = false; f <= ore[i]; f += 0.5F) {
                player.addExperience(1);
                flag = true;
            }
            if (flag) {
                ore[i] = 0;
            }
            counters.put(PlayerExtendedProperties.counters[0], ore);
        }
        giveBonusMiningXP(player);
    }
    
	public static void takenFromSmelting(EntityPlayer player, ItemStack smelting) {
        giveBonusSmeltingXP(player);
	}
}
