package net.minecraft.src;

import java.util.List;
import java.util.Random;

public class BWREntityXPOrb extends EntityXPOrb
	{
	public BWREntityXPOrb(World world)
		{
		super(world);
		}

	protected boolean pushOutOfBlocks(double x, double y, double z)
		{
		// Run normal check; if not in a solid block, do nothing.
		if(!super.pushOutOfBlocks(x, y, z))
			return false;

		// Detect if this XP orb is not destroyed, and trapped
		// inside sand with solid blocks on all sides. If not,
		// skip special logic.
		int bx = MathHelper.floor_double(x);
		int by = MathHelper.floor_double(y);
		int bz = MathHelper.floor_double(z);

		if(isDead
			|| (this.worldObj.getBlockId(bx, by, bz) != Block.sand.blockID)
			|| !this.worldObj.isBlockNormalCube(bx - 1, by, bz)
			|| !this.worldObj.isBlockNormalCube(bx + 1, by, bz)
			|| !this.worldObj.isBlockNormalCube(bx, by - 1, bz)
			|| !this.worldObj.isBlockNormalCube(bx, by + 1, bz)
			|| !this.worldObj.isBlockNormalCube(bx, by, bz - 1)
			|| !this.worldObj.isBlockNormalCube(bx, by, bz + 1))
			return true;


		// If trapped in sand, destroy this XP orb and any others trapped
		// in the same block, and add up total XP value.
		int XP = this.xpValue;
		this.setDead();
		List Found = this.worldObj.getEntitiesWithinAABB(BWREntityXPOrb.class,
			AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(bx, by, bz, bx + 1, by + 1, bz + 1));
		if((Found != null) && (Found.size() > 0))
			for(int I = 0; I < Found.size(); I++)
				{
				BWREntityXPOrb Orb = (BWREntityXPOrb)Found.get(I);
				if(!Orb.isDead && Orb.m_bNotPlayerOwned)
					{
					XP += Orb.xpValue;
					Orb.setDead();
					}
				}

		// 2% probability per trapped XP point of converting sand to soulsand.
		// Large flame particle effect from soulsand conversion.
		if(this.worldObj.rand.nextInt(50) < XP)
			{
			for(int dx = -1; dx <= 1; dx++)
				for(int dy = -1; dy <= 1; dy++)
					for(int dz = -1; dz <= 1; dz++)
						this.worldObj.playAuxSFX(2004, bx + dx, by + dy, bz + dz, 0);
			this.worldObj.playAuxSFX(2001, bx, by, bz, Block.sand.blockID);
			this.worldObj.setBlockWithNotify(bx, by, bz, Block.slowSand.blockID);
			}
		
		return true;
		}
	}
