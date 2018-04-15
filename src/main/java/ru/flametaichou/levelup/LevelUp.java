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
import ru.flametaichou.levelup.Items.ItemExpOrb;
import ru.flametaichou.levelup.Items.ItemFishingLootBox;
import ru.flametaichou.levelup.Items.ItemRespecBook;
import ru.flametaichou.levelup.Model.PacketChannel;
import ru.flametaichou.levelup.Model.PlayerClass;
import ru.flametaichou.levelup.Util.ConfigHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Mod(modid = LevelUp.ID, name = "Level Up! (Ordinary Edition)", version = "${version}")
public final class LevelUp {
    public final static String ID = "levelup";
    @Instance(value = ID)
    public static LevelUp instance;
    @SidedProxy(clientSide = "ru.flametaichou.levelup.SkillClientProxy", serverSide = "ru.flametaichou.levelup.SkillProxy")
    public static SkillProxy proxy;
    private static Item xpTalisman;
    private static Item expOrb;
    private static Item respecBook;
    public static Item iconHunter, iconThief, iconPeasant, iconSmith, iconSentinel, iconTraveller, iconAthletics, iconHearth, iconSneaking, iconSwimming;
    public static Item fishingLootBox;
    public static FMLEventChannel initChannel, skillChannel, classChannel, configChannel, extPropertiesChannel, otherChannel;

    @EventHandler
    public void load(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(BowEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FightEventHandler.INSTANCE);
        SkillPacketHandler sk = new SkillPacketHandler();
        initChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketChannel.LEVELUPINIT.name());
        initChannel.register(sk);
        classChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketChannel.LEVELUPCLASSES.name());
        classChannel.register(sk);
        skillChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketChannel.LEVELUPSKILLS.name());
        skillChannel.register(sk);
        configChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketChannel.LEVELUPCFG.name());
        configChannel.register(sk);
        extPropertiesChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketChannel.LEVELUPEXTPROP.name());
        extPropertiesChannel.register(sk);
        otherChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketChannel.LEVELUPOTHER.name());
        otherChannel.register(sk);
        proxy.registerGui();
    }

    @EventHandler
    public void load(FMLPreInitializationEvent event) {
        ConfigHelper.loadConfig(new Configuration(event.getSuggestedConfigurationFile()));
        registerItems();

        if (event.getSourceFile().getName().endsWith(".jar")) {
            proxy.tryUseMUD();
        }

        FMLCommonHandler.instance().bus().register(FMLEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
        MinecraftForge.EVENT_BUS.register(new MobEventHandler());
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

    // Smith class bonus
    public static void giveBonusSmeltingXP(EntityPlayer player) {
        PlayerClass pClass = PlayerExtendedProperties.getPlayerClass(player);
        if (pClass == PlayerClass.SMITH) {
            player.addExperience(1);
        }
    }

    // Hunter class bonus
    public static void giveBonusFightingXP(EntityPlayer player) {
        PlayerClass pClass = PlayerExtendedProperties.getPlayerClass(player);
        if (pClass == PlayerClass.HUNTER) {
            player.addExperience(2);
        }
    }

    // Peasant class bonus
    public static void giveBonusFishingXP(EntityPlayer player) {
        PlayerClass pClass = PlayerExtendedProperties.getPlayerClass(player);
        if (pClass == PlayerClass.PEASANT) {
            player.addExperience(2);
        }
    }

    // Miner class bonus
    public static void giveBonusMiningXP(EntityPlayer player, int exp) {
        PlayerClass pClass = PlayerExtendedProperties.getPlayerClass(player);
        if (pClass == PlayerClass.MINER) {
            player.addExperience(exp);
        }
    }
    
    public static void giveBonusRandomXP(EntityPlayer player) {
        PlayerClass pClass = PlayerExtendedProperties.getPlayerClass(player);
        // Traveller class bonus
        if (pClass == PlayerClass.TRAVELLER) {
            player.addExperience(1);
        }

    	if (player.inventory.hasItem(xpTalisman)) {
    		player.addExperience(2);
   		}
    }
    
	public static void takenFromSmelting(EntityPlayer player, ItemStack smelting) {
        for (int i = 0; i < smelting.stackSize; i++) {
            giveBonusSmeltingXP(player);
        }
	}

    private void registerItems() {
        fishingLootBox = new ItemFishingLootBox().setUnlocalizedName("fishingLootBox").setTextureName(ID + ":FishingLootBox").setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerItem(fishingLootBox, "fishingLootBox");

        xpTalisman = new Item().setUnlocalizedName("xpTalisman").setTextureName(ID + ":XPTalisman").setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerItem(xpTalisman, "xpTalisman");
        if (ConfigHelper.xpTalismanRecipe) {
            GameRegistry.addRecipe(new ShapedOreRecipe(xpTalisman, "GG ", " R ", " GG", 'G', Items.gold_ingot, 'R', Items.ender_pearl));
        }

        respecBook = new ItemRespecBook().setUnlocalizedName("respecBook").setTextureName(ID + ":RespecBook").setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerItem(respecBook, "respecBook");
        if (ConfigHelper.respecBookRecipe) {
            GameRegistry.addRecipe(new ItemStack(respecBook), "OEO", "DBD", "ODO", 'O', Blocks.obsidian, 'D', new ItemStack(Items.dye),
                'E', Items.ender_pearl, 'B', Items.book);
        }

        expOrb = new ItemExpOrb().setUnlocalizedName("expOrb").setTextureName(ID + ":expOrb").setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerItem(expOrb, "expOrb");

        iconHunter = new Item().setUnlocalizedName("iconHunter").setTextureName(ID + ":bow_sword");
        GameRegistry.registerItem(iconHunter, "iconHunter");
        iconThief = new Item().setUnlocalizedName("iconThief").setTextureName(ID + ":dagger_coins");
        GameRegistry.registerItem(iconThief, "iconThief");
        iconPeasant = new Item().setUnlocalizedName("iconPeasant").setTextureName(ID + ":fishing_rod_hoe");
        GameRegistry.registerItem(iconPeasant, "iconPeasant");
        iconSmith = new Item().setUnlocalizedName("iconSmith").setTextureName(ID + ":hammer");
        GameRegistry.registerItem(iconSmith, "iconSmith");
        iconSentinel = new Item().setUnlocalizedName("iconSentinel").setTextureName(ID + ":shield");
        GameRegistry.registerItem(iconSentinel, "iconSentinel");
        iconTraveller = new Item().setUnlocalizedName("iconTraveller").setTextureName(ID + ":boots");
        GameRegistry.registerItem(iconTraveller, "iconTraveller");

        iconAthletics = new Item().setUnlocalizedName("iconAthletics").setTextureName(ID + ":athletics");
        GameRegistry.registerItem(iconAthletics, "iconAthletics");
        iconHearth = new Item().setUnlocalizedName("iconHearth").setTextureName(ID + ":hearth");
        GameRegistry.registerItem(iconHearth, "iconHearth");
        iconSneaking = new Item().setUnlocalizedName("iconSneaking").setTextureName(ID + ":sneaking");
        GameRegistry.registerItem(iconSneaking, "iconSneaking");
        iconSwimming = new Item().setUnlocalizedName("iconSwimming").setTextureName(ID + ":swimming");
        GameRegistry.registerItem(iconSwimming, "iconSwimming");
    }
}
