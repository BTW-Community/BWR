package net.minecraft.src;

import java.util.List;
import java.util.Random;

public class BWREntityXPOrb extends EntityXPOrb
	{
	public static boolean[] soulPressBlocksAllowed = null;

	public BWREntityXPOrb(World world)
		{
		super(world);

		if(soulPressBlocksAllowed == null)
			{
			boolean[] A = new boolean[4096];
			for(int I = 0; I < 4096; I++)
				{
				Block B = Block.blocksList[I];
				A[I] = (B != null)
					&& (B.blockHardness >= 5.0F)
					&& B.isNormalCube(I);
				}
			soulPressBlocksAllowed = A;
			}
		}

	protected boolean pushOutOfBlocks(double x, double y, double z)
		{
		// Run normal check; if not in a solid block, do nothing.
		if(!super.pushOutOfBlocks(x, y, z))
			return false;

		// Detect if this XP orb is not destroyed, and trapped
		// inside a sand block that's surrounded by suitable blocks
		// to form a "press."
		int bx = MathHelper.floor_double(x);
		int by = MathHelper.floor_double(y);
		int bz = MathHelper.floor_double(z);
		if(isDead || !m_bNotPlayerOwned
			|| (this.worldObj.getBlockId(bx, by, bz) != Block.sand.blockID)
			|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx - 1, by, bz)]
			|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx + 1, by, bz)]
			|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx, by - 1, bz)]
			|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx, by + 1, bz)]
			|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx, by, bz - 1)]
			|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx, by, bz + 1)])
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
			// Burn all entities that are too close to the press.
			List Hurt = this.worldObj.getEntitiesWithinAABB(Entity.class,
				AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(bx - 2, by - 2, bz - 2, bx + 3, by + 4, bz + 3));
			if((Hurt != null) && (Hurt.size() > 0))
				for(int I = 0; I < Hurt.size(); I++)
					{
					Entity Ent = (Entity)Hurt.get(I);
					Ent.attackEntityFrom(DamageSource.onFire, 1);
					Ent.setFire(9);
					}

			// Set fire to nearby flammable materials.
			for(int dx = -2; dx <= 2; dx++)
				for(int dy = -2; dy <= 3; dy++)
					for(int dz = -2; dz <= 2; dz++)
						Block.fire.tryToCatchBlockOnFire(this.worldObj,
							bx + dx, by + dy, bz + dz,
							100, this.worldObj.rand, 0);
			
			// Play some sound/visual effects.
			for(int dx = -1; dx <= 1; dx++)
				for(int dy = -1; dy <= 1; dy++)
					for(int dz = -1; dz <= 1; dz++)
						this.worldObj.playAuxSFX(2004, bx + dx, by + dy, bz + dz, 0);
			for(int dx = -1; dx <= 1; dx += 2)
				for(int dy = -1; dy <= 1; dy += 2)
					for(int dz = -1; dz <= 1; dz += 2)
						this.worldObj.playSoundEffect(x + dx, y + dy, z + dz, "fire.ignite", 1.0F,
				(this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.8F);
			this.worldObj.setBlockWithNotify(bx, by, bz, Block.slowSand.blockID);
			}
		
		return true;
		}
	}
