package net.minecraft.src;
import java.util.Random;

public class BWRBlockPlanter extends FCBlockPlanter
	{
	public BWRBlockPlanter(int id)
		{
		super(id);
		}

	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		if(y < 250)
			{
			int Meta = world.getBlockMetadata(x, y, z);

			if(Meta == FCBlockPlanter.m_iTypeSoulSand)
				BWRPlantBreedEngine.m_instance.GrowFungus(world, x, y + 1, z);
			else if(Meta == FCBlockPlanter.m_iTypeSoilFertilized)
				if(!BWRPlantBreedEngine.m_instance.GrowPlant(world, x, y + 1, z)
					&& !BWRPlantBreedEngine.m_instance.GrowPlant(world, x, y + 2, z))
					BWRPlantBreedEngine.m_instance.GrowFungus(world, x, y + 1, z);
			}
		}
	}
