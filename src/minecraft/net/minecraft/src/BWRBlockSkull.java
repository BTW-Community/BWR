package net.minecraft.src;

// Replacement class for Vanilla severed heads.
public class BWRBlockSkull extends FCBlockSkull {
	public BWRBlockSkull(int id) {
		super(id);
	}

	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);

		// Check for golem recipe for blazes.
		if ((y < 2) || (this.getDamageValue(world, x, y, z) != 0)
				|| (world.getBlockId(x, y - 1, z) != Block.blockGold.blockID)
				|| (world.getBlockId(x + 1, y - 1, z) != Block.fenceIron.blockID)
				|| (world.getBlockId(x - 1, y - 1, z) != Block.fenceIron.blockID)
				|| (world.getBlockId(x, y - 1, z + 1) != Block.fenceIron.blockID)
				|| (world.getBlockId(x, y - 1, z - 1) != Block.fenceIron.blockID)
				|| ((world.getBlockId(x, y - 2, z) != Block.fire.blockID)
						&& (world.getBlockId(x, y - 2, z) != FCBetterThanWolves.fcBlockFireStoked.blockID)))
			return;

		// Set a special metadata flag on the skull block that prevents
		// it from being dropped as an item when destroyed.
		world.setBlockMetadata(x, y, z, 8);

		// Destroy all blocks that will become the blaze.
		world.setBlockAndMetadata(x, y, z, 0, 0);
		world.setBlockAndMetadata(x, y - 1, z, 0, 0);
		world.setBlockAndMetadata(x + 1, y - 1, z, 0, 0);
		world.setBlockAndMetadata(x - 1, y - 1, z, 0, 0);
		world.setBlockAndMetadata(x, y - 1, z + 1, 0, 0);
		world.setBlockAndMetadata(x, y - 1, z - 1, 0, 0);
		world.setBlockAndMetadata(x, y - 2, z, 0, 0);

		// Send BUD events after all blocks have been removed.
		world.notifyBlockChange(x, y, z, 0);
		world.notifyBlockChange(x, y - 1, z, 0);
		world.notifyBlockChange(x + 1, y - 1, z, 0);
		world.notifyBlockChange(x - 1, y - 1, z, 0);
		world.notifyBlockChange(x, y - 1, z + 1, 0);
		world.notifyBlockChange(x, y - 1, z - 1, 0);
		world.notifyBlockChange(x, y - 2, z, 0);

		// Spawn a new blaze where the golem construct was.
		BWREntityBlaze blaze = new BWREntityBlaze(world);
		blaze.isArtificial = true;
		blaze.setLocationAndAngles(x + 0.5D, y, z + 0.5D, 0, 0);
		world.spawnEntityInWorld(blaze);

		// Play sound and visual effects.
		for (int i = 0; i < 3; i++)
			world.playAuxSFX(2004, x, y, z, 0);
		world.playSoundAtEntity(blaze, blaze.getDeathSound(), blaze.getSoundVolume(),
				(world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
	}
}
