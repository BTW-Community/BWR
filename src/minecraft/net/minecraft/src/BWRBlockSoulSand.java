package net.minecraft.src;

import java.util.Random;

//Replacement class for Vanilla soul sand, which adds some new behavior.
public class BWRBlockSoulSand extends FCBlockSoulSand {
	public BWRBlockSoulSand(int id) {
		super(id);
		this.setTickRandomly(true);
	}

	public void updateTick(World world, int x, int y, int z, Random r) {
		super.updateTick(world, x, y, z, r);

		// If there is room above, attempt to cross-breed fungus (netherwart)
		// into the space above.
		if (y < 255)
			BWREngineBreedPlant.getInstance().grow(world, x, y + 1, z);
	}
}
