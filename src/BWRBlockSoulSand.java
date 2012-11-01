package net.minecraft.src;
import java.util.Random;

public class BWRBlockSoulSand extends BlockSoulSand
	{
	public BWRBlockSoulSand(int id, int txridx)
		{
		super(id, txridx);
		this.setTickRandomly(true);
		this.setRequiresSelfNotify();
		}

	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		// If there is room above, attempt to cross-breed fungus (netherwart)
		// into the space above.
		if(y < 255)
			BWRPlantBreedEngine.m_instance.GrowFungus(world, x, y + 1, z);
		}
	}
