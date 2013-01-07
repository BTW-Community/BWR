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

// Replacement animal class that supports cross-breeding.
public class BWREntityWolf extends EntityWolf
	{
	public boolean isOnDrugs;

	public BWREntityWolf(World world)
		{
		super(world);
		this.isOnDrugs = false;
		}

	public void onLivingUpdate()
		{
		super.onLivingUpdate();

		// Do cross-breeding check.
		BWREngineBreedAnimal.getInstance().tryBreed(this);

		if(!this.isInLove())
			this.isOnDrugs = false;
		else if(this.isOnDrugs && (this.entityToAttack != null))
			{
			double dx = this.posX - this.entityToAttack.posX;
			double dy = this.posY - this.entityToAttack.posY;
			double dz = this.posZ - this.entityToAttack.posZ;
			this.attackEntity(this.entityToAttack,
				(float)Math.sqrt(dx * dx + dy * dy + dz * dz));
			}
		}

	public void CheckForLooseFood()
		{
		if(this.isFed())
			{
			super.CheckForLooseFood();
			return;
			}
		super.CheckForLooseFood();
		if(!this.isFed())
			return;

		List list = this.worldObj.getEntitiesWithinAABB(EntityItem.class,
			AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(this.posX - 2.5D,
			this.posY - 1.0D, this.posZ - 2.5D, this.posX + 2.5D, this.posY + 1.0D, this.posZ + 2.5D));
         	if(!list.isEmpty())
			for(int idx = 0; idx < list.size(); ++idx)
				{
				EntityItem ent = (EntityItem)list.get(idx);
				ItemStack item = ent.func_92014_d();
				if(ent.delayBeforeCanPickup > 0 || ent.isDead)
					continue;
				int id = item.itemID;
				if(id != mod_FCBetterThanWolves.fcItemBlastingOil.shiftedIndex)
					continue;

				this.isOnDrugs = true;
				this.setInLove(600);
				this.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 600, 0));

				item.stackSize--;
				if(item.stackSize < 1)
					ent.setDead();
				break;
				}

		if(this.isOnDrugs && (this.entityToAttack == null))
			{
			list = this.worldObj.getEntitiesWithinAABB(EntityWolf.class,
				this.boundingBox.expand(3.5F, 3.5F, 3.5F));
         		if(!list.isEmpty())
				for(int idx = 0; idx < list.size(); ++idx)
					{
					EntityWolf wolf = (EntityWolf)list.get(idx);
					if(wolf.isInLove())
						{
						this.entityToAttack = wolf;
						break;
						}
					}
			}
		}
	}
