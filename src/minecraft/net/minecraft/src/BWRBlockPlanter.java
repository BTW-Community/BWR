package net.minecraft.src;

import java.util.Random;

//Replacement class for BTW planter block that adds some new behavior.
public class BWRBlockPlanter extends FCBlockPlanter {
	public BWRBlockPlanter(int id) {
		super(id);
	}

	// Called randomly by World.
	public void updateTick(World world, int x, int y, int z, Random r) {
		super.updateTick(world, x, y, z, r);

		// If there is room above, attempt to cross-breed plant/fungus on top.
		// Also attempt 2 blocks above, so that lilypads can grow on water
		// above fertile farmland.
		if (y < 255)
			BWREngineBreedPlant.getInstance().grow(world, x, y + 1, z);
		if (y < 254)
			BWREngineBreedPlant.getInstance().grow(world, x, y + 2, z);
	}
}
