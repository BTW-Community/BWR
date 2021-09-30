package net.minecraft.src;

// Replacement animal class that supports cross-breeding.
public class BWREntityCow extends FCEntityCow {
	public BWREntityCow(World world) {
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
