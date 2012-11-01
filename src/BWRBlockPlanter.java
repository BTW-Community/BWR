package net.minecraft.src;
import java.util.Random;

public class BWRBlockPlanter extends FCBlockPlanter
	{
	public BWRBlockPlanter(int id)
		{
		super(id);
		}

	// Called randomly by World.
	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		// Do plant/fungus cross-breeding checks if there is room above.
		if(y < 255)
			{
			int Meta = world.getBlockMetadata(x, y, z);

			// Soulsand planters can only grow fungus (netherwart), so skip plant checks.
			if(Meta == FCBlockPlanter.m_iTypeSoulSand)
				BWRPlantBreedEngine.m_instance.GrowFungus(world, x, y + 1, z);

			// Dirt planters can grow either plants or fungi.  Also attempt to grow
			// plants 2 blocks above, so that lilypads can grow onto water under which
			// a fertile planter is submerged.
			else if(Meta == FCBlockPlanter.m_iTypeSoilFertilized)
				if(!BWRPlantBreedEngine.m_instance.GrowPlant(world, x, y + 1, z)
					&& !((y < 254) && BWRPlantBreedEngine.m_instance.GrowPlant(world, x, y + 2, z)))
					BWRPlantBreedEngine.m_instance.GrowFungus(world, x, y + 1, z);
			}
		}
	}
