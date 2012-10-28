package net.minecraft.src;
import java.util.Random;

public class BWRBlockLilyPad extends BlockLilyPad
	{
	public BWRBlockLilyPad(int id, int txridx)
		{
		super(id, txridx);
		this.setTickRandomly(true);
		this.setRequiresSelfNotify();
		}

	protected boolean canThisPlantGrowOnThisBlockID(int id)
		{
		return (id == Block.waterStill.blockID)
			|| (id == Block.waterMoving.blockID);
		}

	public boolean canBlockStay(World world, int x, int y, int z)
		{
		if((z < 0) || (z > 255))
			return false;
		int id = world.getBlockId(x, y - 1, z);
		if(id == Block.waterStill.blockID)
			return true;
		if(id == Block.waterMoving.blockID)
			return world.getBlockMetadata(x, y - 1, z) < 2;
		return false;
		}

	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		int neighbors = 0;
		for(int dx = -1; dx <= 1; dx++)
			for(int dz = -1; dz <= 1; dz++)
				{
				int b = world.getBlockId(x + dx, y, z + dz);
				if(b == blockID)
					neighbors++;
				}

		if((neighbors < 2) && (r.nextInt(4) == 0))
			{
			int rx = x + r.nextInt(3) - 1;
			int rz = z + r.nextInt(3) - 1;
			if((world.getBlockId(rx, y, rz) == 0) && canBlockStay(world, rx, y, rz))
				{
				world.setBlockWithNotify(rx, y, rz, blockID);
				world.markBlocksDirty(rx, y, rz, rx, y, rz);
				}
			}
		}
	}
