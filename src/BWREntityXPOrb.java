// ==========================================================================
// Copyright (C)2012 by Aaron Suen <warr1024@gmail.com>
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
// ---------------------------------------------------------------------------

package net.minecraft.src;

import java.util.List;
import java.util.Random;

// Replacement class for BTW/Vanilla XP Orb entity, which represents both
// Vanilla XP Orbs and BTW Dragon Orbs, that adds some custom behavior.
public class BWREntityXPOrb extends EntityXPOrb
	{
	public static boolean[] soulPressBlocksAllowed = null;

	public BWREntityXPOrb(World world)
		{
		super(world);

		// On first instance, create, and cache, a list of all
		// block ID's that are allowed as materials in the press
		// mechanism.
		if(soulPressBlocksAllowed == null)
			{
			boolean[] A = new boolean[Block.blocksList.length];
			for(int I = 0; I < Block.blocksList.length; I++)
				{
				Block B = Block.blocksList[I];
				A[I] = (B != null)
					&& (B.blockHardness >= 5.0F)
					&& B.isNormalCube(I);
				}
			soulPressBlocksAllowed = A;
			}
		}

	// Called by Entity superclass on each update cycle to determine
	// if the entity is trapped inside solid blocks, and should be
	// pushed out in any direction.
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

			// Set a bunch of fire around the press, so an automated one needs
			// to be sufficiently fire-proofed to keep running.
			for(int n = 0; n < 20; n++)
				{
				int px = bx + MathHelper.floor_double(this.worldObj.rand.nextGaussian() + 0.5D);
				int py = by + MathHelper.floor_double(this.worldObj.rand.nextGaussian() + 0.5D);
				int pz = bz + MathHelper.floor_double(this.worldObj.rand.nextGaussian() + 0.5D);
				if(this.worldObj.getBlockId(px, py, pz) == 0)
					this.worldObj.setBlockAndMetadata(px, py, pz, Block.fire.blockID, 0);
				}
		
			// Play some sound/visual effects.
			for(int dx = -1; dx <= 1; dx++)
				for(int dy = -1; dy <= 1; dy++)
					for(int dz = -1; dz <= 1; dz++)
						this.worldObj.playAuxSFX(2004, bx + dx, by + dy, bz + dz, 0);
			for(int dx = -1; dx <= 1; dx += 2)
				for(int dy = -1; dy <= 1; dy += 2)
					for(int dz = -1; dz <= 1; dz += 2)
						{
						this.worldObj.playSoundEffect(x + dx, y + dy, z + dz, "mob.wither.shoot", 0.2F,
							(this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.8F);
						this.worldObj.playSoundEffect(x + dx, y + dy, z + dz, "random.fizz", 0.2F,
							(this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.8F);
						}

			// Replace sand with soulsand.
			this.worldObj.setBlockAndMetadataWithNotify(bx, by, bz, Block.slowSand.blockID, 0);
			}
	
		return true;
		}
	}
