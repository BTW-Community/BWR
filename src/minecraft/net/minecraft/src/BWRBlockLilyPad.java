package net.minecraft.src;

import java.util.Random;

// Replacement class for BTW lilypad block that adds some new behavior.
public class BWRBlockLilyPad extends FCBlockLilyPad {
	public BWRBlockLilyPad(int id) {
		super(id);
		this.setTickRandomly(true);
	}

	// Used by BlockFlower superclass code as a quick check if the
	// plant should pop off.
	protected boolean canThisPlantGrowOnThisBlockID(int id) {
		// Lilypads can survive on either flowing or still water.
		return (id == Block.waterStill.blockID) || (id == Block.waterMoving.blockID);
	}

	// A more thorough check, called on block update, to determine
	// if the block should pop off.
	public boolean canBlockStay(World world, int x, int y, int z) {
		if ((y < 0) || (y > 255))
			return false;

		// Lilypads can survive on either flowing or still water.
		int id = world.getBlockId(x, y - 1, z);
		if ((id != Block.waterStill.blockID) && (id != Block.waterMoving.blockID))
			return false;

		// Lilypads require standard (>= 8) light level.
		if ((world.getBlockLightValue(x, y, z) < 8) && !world.canBlockSeeTheSky(x, y, z))
			return false;

		// Flowing water must be the deepest type, i.e the immediate
		// output of a stable-running screw pump, to work.
		if (world.getBlockMetadata(x, y - 1, z) > 1)
			return false;

		return true;
	}

	/*
	 * public void onNeighborBlockChange(World world, int i, int j, int k, int
	 * iBlockID) { updateTick(world, i, j, k, new Random()); }
	 */

	// Called randomly by World.
	public void updateTick(World world, int x, int y, int z, Random r) {
		super.updateTick(world, x, y, z, r);

		// Count the number of lilypads surrounding.
		int neighbors = 0;
		for (int dx = -1; dx <= 1; dx++)
			for (int dz = -1; dz <= 1; dz++) {
				int b = world.getBlockId(x + dx, y, z + dz);
				if (b == blockID)
					neighbors++;
			}

		// Only spread if there are no other neighboring pads, to prevent
		// lilypads from filling up long-lived swamp chunks (even though this
		// would be closer to IRL behavior, it would be out of balance).
		// Growth rate is dependent on light level, so naturally-lit lilypads
		// will not grow at night.
		int ll = world.getBlockLightValue(x, y, z);
		if ((neighbors < 2) && (r.nextInt(32) < ll)) {
			// Try to place a lilypad block in a randomly-chosen adjacent spot.
			int rx = x + r.nextInt(3) - 1;
			int rz = z + r.nextInt(3) - 1;
			if ((world.getBlockId(rx, y, rz) == 0) && canBlockStay(world, rx, y, rz))
				world.setBlockWithNotify(rx, y, rz, blockID);
		}
	}
}
