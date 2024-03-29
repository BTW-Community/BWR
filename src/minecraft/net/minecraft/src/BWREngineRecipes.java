package net.minecraft.src;

//Singleton that manages adding custom BWR recipes to the crafting managers.
public class BWREngineRecipes {
	public static BWREngineRecipes instance_ = null;

	// Singleton access method.
	public static BWREngineRecipes getInstance() {
		if (instance_ == null)
			instance_ = new BWREngineRecipes();
		return instance_;
	}

	// Add low-tech low-efficiency alternative to melting gold in the crucible.
	// Grinding it in a millstone recovers about one third of the original gold, and
	// can be used to obtain raw gold from tools dropped by mobs. Only gold
	// is soft enough for this exception, and only because it's needed for
	// fabricating redstone.
	public void addGoldGrindingRecipe(Item tool, int qty) {
		ItemStack[] stax = new ItemStack[qty];
		for (int I = 0; I < qty; I++)
			stax[I] = new ItemStack(FCBetterThanWolves.fcItemPileGoldOre, 3);
		FCRecipes.AddMillStoneRecipe(stax, new ItemStack[] { new ItemStack(tool, 1, -1) });
	}

	// Add a recipe to the stoked cauldron to recover lapis from dyed wool. Lapis is
	// the
	// only mineral dye, thus can be extracted with soap, leaving the other organic
	// dyes
	// behind. Up to 2 different colors of wool can be left behind, to preserve the
	// other organic dyes; magenta wool needs this to leave behind pink and red.
	// Also adds
	// a recipe to destroy lapis if you forget to add enough clay to the pot.
	public void addLapisRecoveryRecipe(int indmg, int outdmg1, int outdmg2, int qty) {
		FCRecipes.AddStokedCauldronRecipe(
				new ItemStack[] { new ItemStack(Item.dyePowder, 1, 4), new ItemStack(Block.cloth, 4 * qty, outdmg1),
						new ItemStack(Block.cloth, 4 * qty, outdmg2) },
				new ItemStack[] { new ItemStack(Block.cloth, 8 * qty, indmg),
						new ItemStack(FCBetterThanWolves.fcItemSoap, 2 * qty, -1), new ItemStack(Item.clay, 1, -1) });
		FCRecipes.AddStokedCauldronRecipe(
				new ItemStack[] { new ItemStack(Block.cloth, 4 * qty, outdmg1),
						new ItemStack(Block.cloth, 4 * qty, outdmg2) },
				new ItemStack[] { new ItemStack(Block.cloth, 8 * qty, indmg),
						new ItemStack(FCBetterThanWolves.fcItemSoap, 2 * qty, -1) });
	}

	// Add all BWR recipes to the BTW/Vanilla crafting managers. Called on
	// add-on initialization.
	public void addRecipes() {

		// Add low-efficiency gold recycling recipes.
		addGoldGrindingRecipe(Item.plateGold, 8);
		addGoldGrindingRecipe(Item.legsGold, 7);
		addGoldGrindingRecipe(Item.helmetGold, 5);
		addGoldGrindingRecipe(Item.bootsGold, 4);
		addGoldGrindingRecipe(Item.axeGold, 3);
		addGoldGrindingRecipe(Item.pickaxeGold, 3);
		addGoldGrindingRecipe(Item.swordGold, 2);
		addGoldGrindingRecipe(Item.hoeGold, 2);
		addGoldGrindingRecipe(Item.shovelGold, 1);

		// Add recipes for recovering lapis from various colored wools that
		// can be made via sheep breeding.
		addLapisRecoveryRecipe(11, 0, 0, 1);
		addLapisRecoveryRecipe(3, 0, 0, 2);
		addLapisRecoveryRecipe(9, 13, 13, 2);
		addLapisRecoveryRecipe(10, 14, 14, 2);
		addLapisRecoveryRecipe(2, 6, 14, 4);

		// Netherrack can be created by mixing netherwart, cobblestone,
		// and a source of souls (of which soul dust is renewable).
		FCRecipes.AddCauldronRecipe(new ItemStack(Block.netherrack, 8),
				new ItemStack[] { new ItemStack(Block.cobblestone, 8, -1), new ItemStack(Item.netherStalkSeeds, 8, -1),
						new ItemStack(FCBetterThanWolves.fcItemSoulUrn, 1, -1) });

		FCRecipes.AddCauldronRecipe(
				new ItemStack[] { new ItemStack(Block.netherrack, 1),
						new ItemStack(FCBetterThanWolves.fcItemSawDust, 1), },
				new ItemStack[] { new ItemStack(Block.cobblestone, 1, -1), new ItemStack(Item.netherStalkSeeds, 1, -1),
						new ItemStack(FCBetterThanWolves.fcItemSoulDust, 1, -1) });

		/*
		 * Commented Out as there seems to be no reason for any of this, but we shall
		 * see eventually
		 * 
		 * 
		 * // Method for extracting hellfire from netherrack earlier in the tech tree //
		 * than necessary to bottle souls. Grind the netherrack in the millstone a //
		 * second time to extract hellfire. If sawdust is present, souls are absorbed //
		 * by it, producing souldust. If not, souls dissipate harmlessly (since // they
		 * are not trapped by soulsand).
		 * 
		 * FCRecipes.AddMillStoneRecipe( new ItemStack[] { new
		 * ItemStack(FCBetterThanWolves.fcItemHellfireDust, 1, 0), new
		 * ItemStack(FCBetterThanWolves.fcItemSoulDust, 1, 0) }, new ItemStack[] { new
		 * ItemStack(FCBetterThanWolves.fcItemGroundNetherrack, 1, 0), new
		 * ItemStack(FCBetterThanWolves.fcItemSawDust, 1, 0) });
		 * 
		 * FCRecipes.AddMillStoneRecipe(new ItemStack[] { new
		 * ItemStack(FCBetterThanWolves.fcItemHellfireDust, 1, 0) }, new ItemStack[] {
		 * new ItemStack(FCBetterThanWolves.fcItemGroundNetherrack, 1, 0) });
		 */

		// Dead bushes can be trivially created from oak saplings.
		// Birch is the wrong color (and horizontally reversed), and pine
		// and jungle are clearly the wrong shape.
		FCRecipes.AddStokedCauldronRecipe(new ItemStack(Block.deadBush, 1),
				new ItemStack[] { new ItemStack(Block.sapling, 1, 0) });

		// Combine dung into blocks automatically to allow clay farms to be automated.
		// If scoured leather is present, then it acts as a catalyst to break the dung
		// back up again so the leather can be tanned, preventing automatic tanning
		// machines from jamming on a dung backlog.
		FCRecipes.AddCauldronRecipe(
				new ItemStack(FCBetterThanWolves.fcBlockAestheticOpaqueEarth.blockID, 1,
						FCBlockAestheticOpaqueEarth.m_iSubtypeDung),
				new ItemStack[] { new ItemStack(FCBetterThanWolves.fcItemDung, 9) });

		// Allow the creation of webs for builds and aesthetic purposes.
		FCRecipes.AddCauldronRecipe(new ItemStack(FCBetterThanWolves.fcBlockWeb, 1),
				new ItemStack[] { new ItemStack(Item.silk, 3, -1), new ItemStack(Item.slimeBall, 1, -1) });

		// Diamond synthesis from ghast tears as the principal ingredient, with
		// hellfire (stabilized by a soul) as a catalyst, to add risk.
		FCRecipes.AddStokedCrucibleRecipe(
				new ItemStack[] { new ItemStack(Item.diamond, 1),
						new ItemStack(FCBetterThanWolves.fcItemHellfireDust, 1) },
				new ItemStack[] { new ItemStack(Item.ghastTear, 1, -1), new ItemStack(Item.dyePowder, 1, 6),
						new ItemStack(FCBetterThanWolves.fcItemGroundNetherrack, 1, -1) });

		FCRecipes.AddCauldronRecipe(new ItemStack(Item.redstone, 1),
				new ItemStack[] { new ItemStack(FCBetterThanWolves.fcItemPileGoldOre, 1),
						new ItemStack(FCBetterThanWolves.fcItemHellfireDust, 1),
						new ItemStack(FCBetterThanWolves.fcItemPileIronOre, 7) });
	}
}
