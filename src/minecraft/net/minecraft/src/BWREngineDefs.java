package net.minecraft.src;

public class BWREngineDefs {
	public static BWREngineDefs instance_ = null;

	// Singleton access method.
	public static BWREngineDefs getInstance() {
		if (instance_ == null)
			instance_ = new BWREngineDefs();
		return instance_;
	}

	public void addDefinitions() {
	}

	public void addReplacements() {
		// block replacement

		// ASK DAWN ABOUT THE DRAGON ORB CLASS, HOW TO CREATE ITEMS AND BLOCKS, AND IF
		// IM REPLACING THESE PROPERLY
		// ALSO MAKE SURE TO ASK HOW IF SHE KNOWS A GOOD DEBUG COURSE

		Block.redstoneWire = (BlockRedstoneWire) Block.replaceBlock(Block.redstoneWire.blockID,
				BWRBlockRedstoneWire.class);

		Block.skull = (BlockSkull) Block.replaceBlock(Block.skull.blockID, BWRBlockSkull.class);

		Block.waterlily = (BlockLilyPad) Block.replaceBlock(Block.waterlily.blockID, BWRBlockLilyPad.class);

		Block.slowSand = (BlockSoulSand) Block.replaceBlock(Block.slowSand.blockID, BWRBlockSoulSand.class);

		Block.tallGrass = (BlockTallGrass) Block.replaceBlock(Block.tallGrass.blockID, BWRBlockTallGrass.class);

		FCBetterThanWolves.fcBlockFarmlandFertilized = (FCBlockFarmlandFertilized) Block
				.replaceBlock(FCBetterThanWolves.fcBlockFarmlandFertilized.blockID, BWRBlockFarmlandFertilized.class);

		FCBetterThanWolves.fcBlockAestheticOpaqueEarth = (FCBlockAestheticOpaqueEarth) Block.replaceBlock(
				FCBetterThanWolves.fcBlockAestheticOpaqueEarth.blockID, BWRBlockAestheticOpaqueEarth.class);

		FCBetterThanWolves.fcPlanter = (FCBlockPlanter) Block.replaceBlock(FCBetterThanWolves.fcPlanter.blockID,
				BWRBlockPlanter.class);

		// item replacement
		Item.replaceItem(282, BWRItemBreedingHarness.class);
		Item.itemsList[Block.tallGrass.blockID] = (new ItemColored(Block.tallGrass.blockID - 256, true))
				.setBlockNames(new String[] { "shrub", "grass", "fern" });

		// entity replacement
		EntityList.replaceExistingMappingSafe(BWREntityXPOrb.class, "XPOrb");
		EntityList.replaceExistingMappingSafe(BWREntityBlaze.class, "Blaze");
		EntityList.replaceExistingMappingSafe(BWREntityVillager.class, "addonVillagerPriest");
		EntityList.replaceExistingMappingSafe(BWREntitySheep.class, "Sheep");
		EntityList.replaceExistingMappingSafe(BWREntityPig.class, "Pig");
		EntityList.replaceExistingMappingSafe(BWREntityCow.class, "Cow");
		EntityList.replaceExistingMappingSafe(BWREntityWolf.class, "Wolf");
		EntityList.replaceExistingMappingSafe(BWREntityMooshroom.class, "MushroomCow");
		EntityList.replaceExistingMappingSafe(BWREntityOcelot.class, "Ozelot");

	}

}
