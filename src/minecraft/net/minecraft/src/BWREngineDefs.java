package net.minecraft.src;

public class BWREngineDefs {
	public static BWREngineDefs instance_ = null;
	public static BWREngineCore bwrEngineCore;

	// Singleton access method.
	public static BWREngineDefs getInstance() {
		if (instance_ == null)
			instance_ = new BWREngineDefs();
		return instance_;
	}

	public void addDefinitions() {
		// TODO document why this method is empty
	}

	public void addReplacements() {
		// Block Replacement
		Block.redstoneWire = (BlockRedstoneWire) Block.replaceBlock(Block.redstoneWire.blockID,
				BWRBlockRedstoneWire.class, bwrEngineCore);

		FCBetterThanWolves.fcBlockScrewPump = Block.replaceBlock(FCBetterThanWolves.fcBlockScrewPump.blockID,
				BWRBlockScrewPump.class, bwrEngineCore);

		Block.skull = Block.replaceBlock(Block.skull.blockID, BWRBlockSkull.class, bwrEngineCore);

		Block.waterlily = Block.replaceBlock(Block.waterlily.blockID, BWRBlockLilyPad.class, bwrEngineCore);

		Block.slowSand = Block.replaceBlock(Block.slowSand.blockID, BWRBlockSoulSand.class, bwrEngineCore);

		Block.tallGrass = (BlockTallGrass) Block.replaceBlock(Block.tallGrass.blockID, BWRBlockTallGrass.class,
				bwrEngineCore);

		FCBetterThanWolves.fcBlockFarmlandFertilized = (FCBlockFarmlandFertilized) Block.replaceBlock(
				FCBetterThanWolves.fcBlockFarmlandFertilized.blockID, BWRBlockFarmlandFertilized.class, bwrEngineCore);

		FCBetterThanWolves.fcBlockAestheticOpaqueEarth = Block.replaceBlock(
				FCBetterThanWolves.fcBlockAestheticOpaqueEarth.blockID, BWRBlockAestheticOpaqueEarth.class,
				bwrEngineCore);

		FCBetterThanWolves.fcPlanter = Block.replaceBlock(FCBetterThanWolves.fcPlanter.blockID, BWRBlockPlanter.class,
				bwrEngineCore);

		FCBetterThanWolves.fcAestheticOpaque = Block.replaceBlock(FCBetterThanWolves.fcAestheticOpaque.blockID,
				BWRBlockAestheticOpaque.class, bwrEngineCore);

		// Item Replacement

		Item.itemsList[Block.tallGrass.blockID] = (new ItemColored(Block.tallGrass.blockID - 256, true))
				.setBlockNames(new String[] { "shrub", "grass", "fern" });

		// Entity Replacement

		EntityList.replaceExistingMappingSafe(BWREntityXPOrb.class, "XPOrb");

		EntityList.replaceExistingMappingSafe(BWREntityBlaze.class, "Blaze");

		EntityList.replaceExistingMappingSafe(BWREntitySheep.class, "Sheep");

		EntityList.replaceExistingMappingSafe(BWREntityPig.class, "Pig");

		EntityList.replaceExistingMappingSafe(BWREntityCow.class, "Cow");

		EntityList.replaceExistingMappingSafe(BWREntityWolf.class, "Wolf");

		EntityList.replaceExistingMappingSafe(BWREntityMooshroom.class, "MushroomCow");

		EntityList.replaceExistingMappingSafe(BWREntityOcelot.class, "Ozelot");

	}

}
