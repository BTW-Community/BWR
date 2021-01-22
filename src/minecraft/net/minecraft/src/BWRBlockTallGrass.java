package net.minecraft.src;

import java.util.List;
import java.util.Random;

// Replacement class for grass blocks.
public class BWRBlockTallGrass extends FCBlockTallGrass {
	public BWRBlockTallGrass(int id) {
		super(id);
		this.setTickRandomly(true);
	}

	// Called randomly by World.
	public void updateTick(World world, int x, int y, int z, Random rand) {
		super.updateTick(world, x, y, z, rand);

		// Animals spawn spontaneously only very rarely.
		if (rand.nextInt(480) > 0)
			return;

		// Animals can only spawn in direct natural sunlight.
		if (world.provider.hasNoSky || !world.isDaytime() || world.isRaining() || !world.canBlockSeeTheSky(x, y + 1, z))
			return;

		// Look for nearby animals. If there are any, none can spawn
		// spontaneously here.
		Double r = 32.0D;
		List near = world.getEntitiesWithinAABB(EntityAnimal.class,
				AxisAlignedBB.getAABBPool().getAABB(x - r, y - r, z - r, x + r, y + r, z + r));
		if (near.size() > 0)
			return;

		// Look for nearby wheat or carrots. Choose the species to
		// spawn based on the first food item found. Only pigs and sheep
		// are available this way; all others must be cross-bred.
		EntityAnimal animal = null;
		List found = world.getEntitiesWithinAABB(EntityItem.class,
				AxisAlignedBB.getAABBPool().getAABB(x - 2, y, z - 2, x + 3, y + 1, z + 3));
		if ((found != null) && (found.size() > 0))
			for (int i = 0; i < found.size(); i++) {
				EntityItem item = (EntityItem) found.get(i);
				if (item.isDead)
					continue;
				ItemStack stack = item.getEntityItem();
				if (stack.stackSize < 1)
					continue;
				if (stack.itemID == Item.wheat.itemID)
					animal = new FCEntitySheep(world);
				else if (stack.itemID == Item.carrot.itemID)
					animal = new FCEntityPig(world);
				if (animal != null) {
					stack.stackSize--;
					if (stack.stackSize < 1)
						item.setDead();
					break;
				}
			}
		if (animal == null)
			return;

		// Create new animal and spawn in world.
		animal.setLocationAndAngles(x + 0.5D, y + 0.1D, z + 0.5D, world.rand.nextFloat() * 360.0F, 0F);
		world.spawnEntityInWorld(animal);

		// Show some special effects.
		world.playAuxSFX(2004, x, y, z, 0);
	}
}