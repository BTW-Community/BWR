package net.minecraft.src;
import net.minecraft.server.MinecraftServer;

public class BWRRecipes {
	public static BWRRecipes m_instance = new BWRRecipes();

	public void AddDiamondRecoveryRecipe(Item tool, int qty)
		{
		FCRecipes.AddStokedCauldronRecipe(
			new ItemStack(Item.diamond, qty),
			new ItemStack[]
				{
				new ItemStack(tool, 1, -1),
				new ItemStack(mod_FCBetterThanWolves.fcPotash, qty * 8, -1),
				new ItemStack(mod_FCBetterThanWolves.fcConcentratedHellfire, qty, -1),
				});
		}

	public void AddLapisRecoveryRecipe(int indmg, int outdmg1, int outdmg2, int qty)
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
				new ItemStack(mod_FCBetterThanWolves.fcSoap, 2 * qty, -1),
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
				new ItemStack(mod_FCBetterThanWolves.fcSoap, 2 * qty, -1)
				});
		}

	public void AddRecipes()
		{
		// Add recipes to the stoked pot for recovering diamond from equipment
		// that can be purchased from villagers.
		AddDiamondRecoveryRecipe(Item.plateDiamond, 8);
		AddDiamondRecoveryRecipe(Item.legsDiamond, 7);
		AddDiamondRecoveryRecipe(Item.helmetDiamond, 5);
		AddDiamondRecoveryRecipe(Item.bootsDiamond, 4);
		AddDiamondRecoveryRecipe(Item.axeDiamond, 3);
		AddDiamondRecoveryRecipe(Item.pickaxeDiamond, 3);
		AddDiamondRecoveryRecipe(Item.swordDiamond, 2);
		AddDiamondRecoveryRecipe(Item.hoeDiamond, 2);
		AddDiamondRecoveryRecipe(Item.shovelDiamond, 1);

		// Add recipes for recovering lapis from various colored wools that
		// can be made via sheep breeding.
		AddLapisRecoveryRecipe(11, 0, 0, 1);
		AddLapisRecoveryRecipe(3, 0, 0, 2);
		AddLapisRecoveryRecipe(9, 13, 13, 2);
		AddLapisRecoveryRecipe(10, 14, 14, 2);
		AddLapisRecoveryRecipe(2, 6, 14, 4);

		// Netherrack can be created by mixing netherwart, cobblestone,
		// and a source of souls (of which soul dust is renewable).
		FCRecipes.AddCauldronRecipe(
			new ItemStack[]
				{
				new ItemStack(Block.netherrack, 1),
				new ItemStack(mod_FCBetterThanWolves.fcSawDust, 1),
				},
			new ItemStack[]
				{
				new ItemStack(Block.cobblestone, 1, -1),
				new ItemStack(Item.netherStalkSeeds, 1, -1),
				new ItemStack(mod_FCBetterThanWolves.fcSoulDust, 1, -1)
				});
		FCRecipes.AddCauldronRecipe(
			new ItemStack(Block.netherrack, 8),
			new ItemStack[]
				{
				new ItemStack(Block.cobblestone, 8, -1),
				new ItemStack(Item.netherStalkSeeds, 8, -1),
				new ItemStack(mod_FCBetterThanWolves.fcSoulUrn, 1, -1)
				});

		// Dead bushes can be trivially created from oak saplings.
		FCRecipes.AddStokedCauldronRecipe(
			new ItemStack(Block.deadBush, 1),
			new ItemStack[]
				{
				new ItemStack(Block.sapling, 1, 0)
				});
		}
	}
