package net.minecraft.src;

// Replacement animal class that supports cross-breeding.
public class BWREntitySheep extends FCEntitySheep {
	public BWREntitySheep(World world) {
		super(world);
	}

	public void onLivingUpdate() {
		super.onLivingUpdate();

		// Do cross-breeding check.
		BWREngineBreedAnimal.getInstance().tryBreed(this);
	}
}
