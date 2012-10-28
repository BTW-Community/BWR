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
				for(int dy = 1; dy <= 3; dy++)
					{
					if(!world.isBlockNormalCube(x, y - dy, z))
						{
						for(int dx = -1; dx < 1; dx++)
							for(int dz = -1; dz < 1; dz++)
								{
								int b = world.getBlockId(x + dy, y - dy, z);
								if(b == mod_FCBetterThanWolves.fcStokedFire.blockID)
									heat += 3;
								else if(b == Block.fire.blockID)
									heat += 1;
								}
						break;
						}
					}

				if((heat > 0) && (r.nextInt(4800) < heat))
					{
					world.setBlockAndMetadataWithNotify(x, y, z, Block.dirt.blockID, 0);

					int b = world.getBlockId(x, y - 1, z);
					if(b == Block.sand.blockID)
						{
						world.playSoundEffect(x, y, z, "random.fizz", 1.0F,
							1.0F + world.rand.nextFloat() * 0.5F);
						world.setBlockAndMetadataWithNotify(x, y - 1, z, Block.blockClay.blockID, 0);
						}
					}
				}
			}
		else if(type == m_iSubtypeHellfire)
			{
			int nearby = 0;
			int sources = 0;
			for(int dx = -1; dx < 1; dx++)
				for(int dy = -1; dy < 1; dy++)
					for(int dz = -1; dz < 1; dz++)
						{
						int id = world.getBlockId(x + dx, y + dy, z + dz);
						if((id == Block.lavaStill.blockID) || (id == Block.lavaMoving.blockID))
							{
							if(id == Block.lavaStill.blockID)
								sources++;
							nearby++;
							}
						}
			if((sources > 0) && (r.nextInt(1200) < nearby))
				{
				for(int i = 0; i < 3; i++)
					world.playAuxSFX(2004, x, y, z, 0);
				world.playSoundEffect(x, y, z, "fire.ignite", 1.0F, world.rand.nextFloat() * 0.5F);
				world.setBlockAndMetadataWithNotify(x, y, z, Block.lavaStill.blockID, 0);
				}
			}
		}
	}
