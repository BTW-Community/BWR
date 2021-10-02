package net.minecraft.src;

// Replacement animal class that supports cross-breeding.
public class BWREntityOcelot extends FCEntityOcelot {
	public BWREntityOcelot(World world) {
		super(world);
		if (world.isRemote)
			return;
	}

	public void onLivingUpdate() {
		super.onLivingUpdate();

		// Do cross-breeding check.
		BWREngineBreedAnimal.getInstance().tryBreed(this);
	}
}
