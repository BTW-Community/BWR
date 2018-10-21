// ==========================================================================
// Copyright (C)2018 by Aaron Suen <warr1024@gmail.com>
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
// ---------------------------------------------------------------------------

package net.minecraft.src;

import java.util.ArrayList;

// Singleton that manages adding custom BWR recipes to the crafting managers.
public class BWREngineRecipes {
	public static BWREngineRecipes instance_ = null;

	// Singleton access method.
	public static BWREngineRecipes getInstance()
		{
		if(instance_ == null)
			instance_ = new BWREngineRecipes();
		return instance_;
		}

	// Adds a recipe to the stoked cauldron to recover the specified number of diamonds
	// from the specified item.  Calculates the amount of potash and hellfire that
	// would be required.  Potash + hellfire are kind of like "Drano Crystals" that
	// form a strong alkaline, sufficient to destroy sticks and other bonding materials.
	public void addDiamondRecoveryRecipe(Item tool, int qty)
		{
		FCRecipes.AddStokedCauldronRecipe(
			new ItemStack(Item.diamond, qty),
			new ItemStack[]
				{
				new ItemStack(tool, 1, -1),
				new ItemStack(FCBetterThanWolves.fcPotash, qty * 8, -1),
				new ItemStack(FCBetterThanWolves.fcConcentratedHellfire, qty, -1),
				});
		}
	public void addDiamondRecoveryRecipe(Block block, int qty)
		{
		FCRecipes.AddStokedCauldronRecipe(
			new ItemStack(Item.diamond, qty),
			new ItemStack[]
				{
				new ItemStack(block, 1, -1),
				new ItemStack(FCBetterThanWolves.fcPotash, qty * 8, -1),
				new ItemStack(FCBetterThanWolves.fcConcentratedHellfire, qty, -1),
				});
		}

	// Add low-tech low-efficiency alternative to melting gold in the crucible.
	// Grinding it in a millstone recovers about one third of the original gold, and
	// can be used to obtain raw gold from tools dropped by mobs.  Only gold
	// is soft enough for this exception, and only because it's needed for
	// fabricating redstone.
	public void addGoldGrindingRecipe(Item tool, int qty)
		{
		ItemStack[] stax = new ItemStack[qty];
		for(int I = 0; I < qty; I++)
			stax[I] = new ItemStack(Item.goldNugget, 3);
		FCRecipes.AddMillStoneRecipe(stax,
			new ItemStack[] { new ItemStack(tool, 1, -1) });
		}

	// Add a recipe to the stoked cauldron to recover lapis from dyed wool.  Lapis is the
	// only mineral dye, thus can be extracted with soap, leaving the other organic dyes
	// behind.  Up to 2 different colors of wool can be left behind, to preserve the
	// other organic dyes; magenta wool needs this to leave behind pink and red.  Also adds
	// a recipe to destroy lapis if you forget to add enough clay to the pot.
	public void addLapisRecoveryRecipe(int indmg, int outdmg1, int outdmg2, int qty)
		{
		FCRecipes.AddStokedCauldronRecipe(
			new ItemStack[]
				{
				new ItemStack(Item.dyePowder, 1, 4),
				new ItemStack(Block.cloth, 4 * qty, outdmg1),
				new ItemStack(Block.cloth, 4 * qty, outdmg2)
				},
			new ItemStack[]
				{
				new ItemStack(Block.cloth, 8 * qty, indmg),
				new ItemStack(FCBetterThanWolves.fcSoap, 2 * qty, -1),
				new ItemStack(Item.clay, 1, -1)
				});
		FCRecipes.AddStokedCauldronRecipe(
			new ItemStack[]
				{
				new ItemStack(Block.cloth, 4 * qty, outdmg1),
				new ItemStack(Block.cloth, 4 * qty, outdmg2)
				},
			new ItemStack[]
				{
				new ItemStack(Block.cloth, 8 * qty, indmg),
				new ItemStack(FCBetterThanWolves.fcSoap, 2 * qty, -1)
				});
		}

	// Add all BWR recipes to the BTW/Vanilla crafting managers.  Called on
	// add-on initialization.
	public void addRecipes()
		{
		// Add recipes to the stoked pot for recovering diamond from equipment.
		addDiamondRecoveryRecipe(Item.plateDiamond, 8);
		addDiamondRecoveryRecipe(Item.legsDiamond, 7);
		addDiamondRecoveryRecipe(Item.helmetDiamond, 5);
		addDiamondRecoveryRecipe(Item.bootsDiamond, 4);
		addDiamondRecoveryRecipe(Item.axeDiamond, 3);
		addDiamondRecoveryRecipe(Item.pickaxeDiamond, 3);
		addDiamondRecoveryRecipe(Item.swordDiamond, 2);
		addDiamondRecoveryRecipe(Item.hoeDiamond, 2);
		addDiamondRecoveryRecipe(Item.shovelDiamond, 1);
		addDiamondRecoveryRecipe(Block.jukebox, 1);

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
		FCRecipes.AddCauldronRecipe(
			new ItemStack(Block.netherrack, 8),
			new ItemStack[]
				{
				new ItemStack(Block.cobblestone, 8, -1),
				new ItemStack(Item.netherStalkSeeds, 8, -1),
				new ItemStack(FCBetterThanWolves.fcSoulUrn, 1, -1)
				});
		FCRecipes.AddCauldronRecipe(
			new ItemStack[]
				{
				new ItemStack(Block.netherrack, 1),
				new ItemStack(FCBetterThanWolves.fcItemSawDust, 1),
				},
			new ItemStack[]
				{
				new ItemStack(Block.cobblestone, 1, -1),
				new ItemStack(Item.netherStalkSeeds, 1, -1),
				new ItemStack(FCBetterThanWolves.fcSoulDust, 1, -1)
				});

		// Low-efficiency alternative way to obtain redstone from gold and hellfire.
		// Hibachis cannot be made without redstone, but are necessary to make
		// redstone in a stoked crucible without it being available from worldgen.
		// Instead, redstone dust can be made in a millstone, but it requires 3x as
		// much gold for the same amount of redstone.
		FCRecipes.AddMillStoneRecipe(
			new ItemStack[] { new ItemStack(Item.redstone, 63, 0) },
			new ItemStack[]
				{
				new ItemStack(FCBetterThanWolves.fcConcentratedHellfire, 9, -1),
				new ItemStack(Item.ingotGold, 3, -1)
				});
		FCRecipes.AddMillStoneRecipe(
			new ItemStack[] { new ItemStack(Item.redstone, 7, 0) },
			new ItemStack[]
				{
				new ItemStack(FCBetterThanWolves.fcConcentratedHellfire, 1, -1),
				new ItemStack(Item.goldNugget, 3, -1)
				});

		// Method for extracting hellfire from netherrack earlier in the tech tree
		// than necessary to bottle souls.  Grind the netherrack in the millstone a
		// second time to extract hellfire.  If sawdust is present, souls are absorbed
		// by it, producing souldust.  If not, souls dissipate harmlessly (since
		// they are not trapped by soulsand).
		FCRecipes.AddMillStoneRecipe(
			new ItemStack[]
				{
				new ItemStack(FCBetterThanWolves.fcHellfireDust, 1, 0),
				new ItemStack(FCBetterThanWolves.fcSoulDust, 1, 0)
				},
			new ItemStack[]
				{
				new ItemStack(FCBetterThanWolves.fcGroundNetherrack, 1, 0),
				new ItemStack(FCBetterThanWolves.fcItemSawDust, 1, 0)
				});
		FCRecipes.AddMillStoneRecipe(
			new ItemStack[] { new ItemStack(FCBetterThanWolves.fcHellfireDust, 1, 0) },
			new ItemStack[] { new ItemStack(FCBetterThanWolves.fcGroundNetherrack, 1, 0) });

		// Dead bushes can be trivially created from oak saplings.
		// Birch is the wrong color (and horizontally reversed), and pine
		// and jungle are clearly the wrong shape.
		FCRecipes.AddStokedCauldronRecipe(
			new ItemStack(Block.deadBush, 1),
			new ItemStack[] { new ItemStack(Block.sapling, 1, 0) });

		// Combine dung into blocks automatically to allow clay farms to be automated.
		// If scoured leather is present, then it acts as a catalyst to break the dung
		// back up again so the leather can be tanned, preventing automatic tanning
		// machines from jamming on a dung backlog.
		FCRecipes.AddCauldronRecipe(
			new ItemStack(FCBetterThanWolves.fcAestheticOpaque, 1, FCBlockAestheticOpaque.m_iSubtypeDung),
			new ItemStack[] { new ItemStack(FCBetterThanWolves.fcDung, 9) });
		FCRecipes.AddCauldronRecipe(
			new ItemStack[]
				{
				new ItemStack(FCBetterThanWolves.fcTannedLeather, 1, 0),
				new ItemStack(FCBetterThanWolves.fcDung, 8, 0)
				},
			new ItemStack[]
				{
				new ItemStack(FCBetterThanWolves.fcScouredLeather, 1, -1),
				new ItemStack(FCBetterThanWolves.fcAestheticOpaque, 1, FCBlockAestheticOpaque.m_iSubtypeDung)
				});

		// Allow the creation of webs for builds and aesthetic purposes.
		FCRecipes.AddCauldronRecipe(
			new ItemStack(Block.web, 1),
			new ItemStack[]
				{
				new ItemStack(Item.silk, 3, -1),
				new ItemStack(Item.slimeBall, 1, -1)
				});

		// Diamond synthesis from ghast tears as the principal ingredient, with
		// hellfire (stabilized by a soul) as a catalyst, to add risk.
		FCRecipes.AddStokedCrucibleRecipe(
			new ItemStack[]
				{
				new ItemStack(Item.diamond, 1),
				new ItemStack(FCBetterThanWolves.fcHellfireDust, 1)
				},
			new ItemStack[]
				{
				new ItemStack(Item.ghastTear, 1, -1),
				new ItemStack(Item.dyePowder, 1, 6),
				new ItemStack(FCBetterThanWolves.fcGroundNetherrack, 1, -1)
				});
		}
	}
