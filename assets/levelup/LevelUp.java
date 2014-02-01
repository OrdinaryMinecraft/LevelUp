package assets.levelup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEventChannel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "levelup", name = "Level Up!", version = "0.3")
public class LevelUp {
	@Instance(value = "levelup")
	public static LevelUp instance;
	@SidedProxy(clientSide = "assets.levelup.SkillClientProxy", serverSide = "assets.levelup.SkillProxy")
	public static SkillProxy proxy;
	private static Item xpTalisman;
	private static Map<Item, Integer> towItems = new HashMap<Item, Integer>();
	private static Item[] ingrTier1, ingrTier2, ingrTier3, ingrTier4;
	public static boolean allowHUD, renderTopLeft, renderExpBar;
    public static FMLEventChannel initChannel, skillChannel, classChannel;

	@EventHandler
	public void load(FMLInitializationEvent event) {
        PlayerEventHandler handler = new PlayerEventHandler();
        FMLCommonHandler.instance().bus().register(handler);
		MinecraftForge.EVENT_BUS.register(handler);
		MinecraftForge.EVENT_BUS.register(new BowEventHandler());
		MinecraftForge.EVENT_BUS.register(new FightEventHandler());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        SkillPacketHandler sk = new SkillPacketHandler();
        initChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel("LEVELUPINIT");
        initChannel.register(sk);
        skillChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel("LEVELUPSKILLS");
        skillChannel.register(sk);
        classChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel("LEVELUPCLASSES");
        classChannel.register(sk);
		proxy.registerGui();
	}

	@EventHandler
	public void load(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		allowHUD = config.get("HUD", "allow HUD", true).getBoolean(true);
		renderTopLeft = config.get("HUD", "render HUD on Top Left", true).getBoolean(true);
		renderExpBar = config.get("HUD", "render HUD on Exp Bar", true).getBoolean(true);
		ItemRespecBook.resClass = config.get("Cheats", "unlearning Book Reset Class", false).getBoolean(false);
		PlayerEventHandler.xpPerLevel = config.get("Cheats", "Xp gain per level", 3).getInt(3);
		if (config.hasChanged())
			config.save();
		ingrTier1 = (new Item[] { Items.stick, Items.leather, Item.func_150898_a(Blocks.stone) });
		ingrTier2 = (new Item[] { Items.iron_ingot, Items.gold_ingot, Items.paper, Items.slime_ball });
		ingrTier3 = (new Item[] { Items.redstone, Items.glowstone_dust, Items.ender_pearl });
		ingrTier4 = (new Item[] { Items.diamond });
		towItems.put(Item.func_150898_a(Blocks.log), Integer.valueOf(2));
		towItems.put(Items.coal, Integer.valueOf(4));
		towItems.put(Items.brick, Integer.valueOf(4));
		towItems.put(Items.book, Integer.valueOf(4));
		towItems.put(Item.func_150898_a(Blocks.iron_ore), Integer.valueOf(8));
		towItems.put(Items.dye, Integer.valueOf(8));
		towItems.put(Items.redstone, Integer.valueOf(8));
		towItems.put(Items.bread, Integer.valueOf(10));
		towItems.put(Items.melon, Integer.valueOf(10));
		towItems.put(Item.func_150898_a(Blocks.pumpkin), Integer.valueOf(10));
		towItems.put(Items.cooked_porkchop, Integer.valueOf(12));
		towItems.put(Items.cooked_beef, Integer.valueOf(12));
		towItems.put(Items.cooked_chicken, Integer.valueOf(12));
		towItems.put(Items.cooked_fished, Integer.valueOf(12));
		towItems.put(Items.iron_ingot, Integer.valueOf(16));
		towItems.put(Item.func_150898_a(Blocks.gold_ore), Integer.valueOf(20));
		towItems.put(Items.gold_ingot, Integer.valueOf(24));
		towItems.put(Items.diamond, Integer.valueOf(40));
        Item respecBook = new ItemRespecBook().setUnlocalizedName("respecBook").setTextureName("levelup:RespecBook").setCreativeTab(CreativeTabs.tabTools);
        xpTalisman = new Item().setUnlocalizedName("xpTalisman").setTextureName("levelup:XPTalisman").setCreativeTab(CreativeTabs.tabTools);
        GameRegistry.registerItem(respecBook, "Book of Unlearning");
        GameRegistry.registerItem(xpTalisman, "Talisman of Wonder");
        GameRegistry.addRecipe(new ItemStack(respecBook, 1), "OEO", "DBD", "ODO", Character.valueOf('O'), Blocks.obsidian, Character.valueOf('D'), new ItemStack(Items.dye, 1, 0),
                Character.valueOf('E'), Items.ender_pearl, Character.valueOf('B'), Items.book );
        ItemStack talisman = new ItemStack(xpTalisman, 1);
        GameRegistry.addRecipe(talisman, "GG ", " R ", " GG", Character.valueOf('G'), Items.gold_ingot, Character.valueOf('R'), Items.redstone);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.coal);
        GameRegistry.addRecipe(new ShapelessOreRecipe(talisman, xpTalisman, "oreGold"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(talisman, xpTalisman, "oreIron"));
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.diamond);
        GameRegistry.addRecipe(new ShapelessOreRecipe(talisman, xpTalisman, "logWood"));
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.brick);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.book);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, new ItemStack(Items.dye, 1, 4));
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.redstone);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.bread);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.melon);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.cooked_porkchop);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.cooked_beef);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.cooked_chicken);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.cooked_fished);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.iron_ingot);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Items.gold_ingot);
        GameRegistry.addShapelessRecipe(talisman, xpTalisman, Blocks.pumpkin);
        GameRegistry.addRecipe(new ItemStack(Items.pumpkin_seeds, 4), "#", Character.valueOf('#'), Blocks.pumpkin);
        GameRegistry.addRecipe(new ItemStack(Blocks.gravel, 4), "##", "##", Character.valueOf('#'), Items.flint);
	}

	public static void giveBonusCraftingXP(EntityPlayer player) {
		byte pClass = PlayerExtendedProperties.getPlayerClass(player);
		if (pClass == 3 || pClass == 6 || pClass == 9 || pClass == 12) {
			Map<String, int[]> counters = PlayerExtendedProperties.getCounterMap(player);
			int[] bonus = counters.get(PlayerExtendedProperties.counters[2]);
			if (bonus == null || bonus.length == 0) {
				bonus = new int[] { 0, 0, 0, 0 };
			}
			if (bonus[1] < 4) {
				bonus[1]++;
			} else {
				bonus[1] = 0;
				player.addExperience(2);
			}
			counters.put(PlayerExtendedProperties.counters[2], bonus);
		}
	}

	public static void giveBonusMiningXP(EntityPlayer player) {
		byte pClass = PlayerExtendedProperties.getPlayerClass(player);
		if (pClass == 1 || pClass == 4 || pClass == 7 || pClass == 10) {
			Map<String, int[]> counters = PlayerExtendedProperties.getCounterMap(player);
			int[] bonus = counters.get(PlayerExtendedProperties.counters[2]);
			if (bonus == null || bonus.length == 0) {
				bonus = new int[] { 0, 0, 0 };
			}
			if (bonus[0] < 4) {
				bonus[0]++;
			} else {
				bonus[0] = 0;
				player.addExperience(2);
			}
			counters.put(PlayerExtendedProperties.counters[2], bonus);
		}
	}

	public static void giveCraftingXP(EntityPlayer player, ItemStack itemstack) {
		Item[][] ai = { ingrTier1, ingrTier2, ingrTier3, ingrTier4 };
		for (int i = 0; i < 4; i++) {
			if (Arrays.asList(ai[i]).contains(itemstack.getItem())) {
				incrementCraftCounter(player, i);
			}
		}
	}

	public static void incrementCraftCounter(EntityPlayer player, int i) {
		Map<String, int[]> counters = PlayerExtendedProperties.getCounterMap(player);
		int[] craft = counters.get(PlayerExtendedProperties.counters[1]);
		if (craft.length <= i) {
			int[] craftnew = new int[i + 1];
			System.arraycopy(craft, 0, craftnew, 0, craft.length);
			counters.put(PlayerExtendedProperties.counters[0], craftnew);
			craft = craftnew;
		}
		craft[i]++;
		float f = (float) Math.pow(2D, 3 - i);
		boolean flag;
		for (flag = false; f <= craft[i]; flag = true) {
			player.addExperience(1);
			f += 0.5F;
		}
		if (flag) {
			craft[i] = 0;
		}
		counters.put(PlayerExtendedProperties.counters[1], craft);
	}

	public static void incrementOreCounter(EntityPlayer player, int i) {
		Map<String, int[]> counters = PlayerExtendedProperties.getCounterMap(player);
		int[] ore = counters.get(PlayerExtendedProperties.counters[0]);
		if (ore.length <= i) {
			int[] orenew = new int[i + 1];
			System.arraycopy(ore, 0, orenew, 0, ore.length);
			counters.put(PlayerExtendedProperties.counters[0], orenew);
			ore = orenew;
		}
		ore[i]++;
		giveBonusMiningXP(player);
		float f = (float) Math.pow(2D, 3 - i) / 2.0F;
		boolean flag;
		for (flag = false; f <= ore[i]; flag = true) {
			player.addExperience(1);
			f += 0.5F;
		}
		if (flag) {
			ore[i] = 0;
		}
		counters.put(PlayerExtendedProperties.counters[0], ore);
	}

	public static boolean isTalismanRecipe(IInventory iinventory) {
		for (int i = 0; i < iinventory.getSizeInventory(); i++) {
			if (iinventory.getStackInSlot(i) != null && iinventory.getStackInSlot(i).getItem() == xpTalisman) {
				return true;
			}
		}
		return false;
	}

	public static void takenFromCrafting(EntityPlayer player, ItemStack itemstack, IInventory iinventory) {
		if (isTalismanRecipe(iinventory)) {
			for (int i = 0; i < iinventory.getSizeInventory(); i++) {
				ItemStack itemstack1 = iinventory.getStackInSlot(i);
				if (itemstack1 != null) {
					if (towItems.containsKey(itemstack.getItem())) {
						player.addExperience((int) Math.floor(itemstack1.stackSize * towItems.get(itemstack.getItem()) / 4D));
						iinventory.getStackInSlot(i).stackSize = 0;
					}
				}
			}
		} else {
			for (int j = 0; j < iinventory.getSizeInventory(); j++) {
				ItemStack itemstack2 = iinventory.getStackInSlot(j);
				if (itemstack2 != null && itemstack.getItem() != Item.func_150898_a(Blocks.gold_block) && itemstack.getItem() != Item.func_150898_a(Blocks.iron_block) && itemstack.getItem() != Item.func_150898_a(Blocks.diamond_block)) {
					giveCraftingXP(player, itemstack2);
					giveBonusCraftingXP(player);
				}
			}
		}
	}
}