package net.minecraft.src;

import java.util.Random;

public class BWRBlockAestheticOpaqueEarth extends FCBlockAestheticOpaqueEarth {

	public BWRBlockAestheticOpaqueEarth(int id) {
		super(id);

		this.setTickRandomly(true);
	}

	// Called randomly by World.
	public void RandomUpdateTick(World world, int x, int y, int z, Random r) {
		super.updateTick(world, x, y, z, r);

		int type = world.getBlockMetadata(x, y, z);
		if (type == FCBlockAestheticOpaqueEarth.m_iSubtypeDung) {
			// For dung blocks to create dirt/clay, they must have water above.
			int above = world.getBlockId(x, y + 1, z);
			if ((above == Block.waterStill.blockID) || (above == Block.waterMoving.blockID)) {
				// Calculate the amount of heat available to speed up the reaction.
				// Ambient temperature is 10, plus 1 for each fire, 3 for every stoked
				// fire. Search downwards for the first non-solid-cube block, then
				// search a 3x3 for fire. This means the max heat available is 37.
				// Note that a non-solid non-cube block above the fire will act as an
				// insulator.
				int heat = 10;
				for (int dy = 1; dy <= 3; dy++) {
					if (!world.isBlockNormalCube(x, y - dy, z)) {
						for (int dx = -1; dx <= 1; dx++)
							for (int dz = -1; dz <= 1; dz++) {
								int b = world.getBlockId(x + dx, y - dy, z + dz);
								if (b == FCBetterThanWolves.fcBlockFireStoked.blockID)
									heat += 3;
								else if (b == Block.fire.blockID)
									heat += 1;
							}
						break;
					}
				}

				// Reaction proceeds stochastically, with probability proportional to heat,
				// so adding 3x3 stoked flame shortens the half-life by almost 75%. 4800
				if (r.nextInt(4800) < heat) {
					// Acid is washed from dung block, leaving behind dirt suitable for
					// farming applications.
					world.setBlockAndMetadataWithNotify(x, y, z, FCBetterThanWolves.fcBlockDirtLoose.blockID, 0);

					// Check for sand below the dung.
					int b = world.getBlockId(x, y - 1, z);
					if (b == Block.sand.blockID) {
						// If there is sand, the acid being washed down from the dung
						// block will etch it into finer clay particles, and produce
						// a hissing sound.
						world.playSoundEffect(x, y, z, "random.fizz", 1.0F, 1.0F + world.rand.nextFloat() * 0.5F);
						world.setBlockAndMetadataWithNotify(x, y - 1, z, Block.blockClay.blockID, 0);
					}
				}
			}
		}
	}
}
