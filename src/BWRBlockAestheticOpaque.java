package net.minecraft.src;
import java.util.Random;

public class BWRBlockAestheticOpaque extends FCBlockAestheticOpaque
	{
	public BWRBlockAestheticOpaque(int id)
		{
		super(id);
		this.setTickRandomly(true);
		this.setRequiresSelfNotify();
		}

	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);
		int type = world.getBlockMetadata(x, y, z);
		if(type == FCBlockAestheticOpaque.m_iSubtypeDung)
			{
			int above = world.getBlockId(x, y + 1, z);
			if((above == Block.waterStill.blockID) || (above == Block.waterMoving.blockID))
				{
				int heat = 10;
				for(int dy = 0; dy <= 3; dy++)
					{
					int b = world.getBlockId(x, y - dy, z);
					if(b == mod_FCBetterThanWolves.fcStokedFire.blockID)
						heat += 27;
					else if(b == Block.fire.blockID)
						heat += 9;
					}

				if((heat > 0) && (r.nextInt(3200) < heat))
					{
					world.setBlockWithNotify(x, y, z, Block.dirt.blockID);

					int b = world.getBlockId(x, y - 1, z);
					if(b == Block.sand.blockID)
						{
						world.playAuxSFX(1004, x, y - 1, z, 0);
						world.setBlockWithNotify(x, y - 1, z, Block.blockClay.blockID);
						}

					world.markBlocksDirty(x, y, z, x, y - 1, z);
					}
				}
			}
		}
	}
