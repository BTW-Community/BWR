package net.minecraft.src;
import java.util.Random;

public class BWRBlockFarmlandFertilized extends FCBlockFarmlandFertilized
	{
	public BWRBlockFarmlandFertilized(int id)
		{
		super(id);
		}

	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		if(y < 250)
			if(!BWRPlantBreedEngine.m_instance.GrowPlant(world, x, y + 1, z)
				&& !BWRPlantBreedEngine.m_instance.GrowPlant(world, x, y + 2, z))
				BWRPlantBreedEngine.m_instance.GrowFungus(world, x, y + 1, z);
		}
	}
