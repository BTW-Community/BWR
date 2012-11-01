package net.minecraft.src;
import java.util.Random;

public class BWRBlockFarmlandFertilized extends FCBlockFarmlandFertilized
	{
	public BWRBlockFarmlandFertilized(int id)
		{
		super(id);
		}

	// Called randomly by World.
	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		// If there is room above, attempt to cross-breed plant/fungus on top.
		// Also attempt 2 blocks above, so that lilypads can grow on water
		// above fertile farmland.
		if(y < 255)
			if(!BWRPlantBreedEngine.m_instance.GrowPlant(world, x, y + 1, z)
				&& !((y < 254) && BWRPlantBreedEngine.m_instance.GrowPlant(world, x, y + 2, z)))
				BWRPlantBreedEngine.m_instance.GrowFungus(world, x, y + 1, z);
		}
	}
