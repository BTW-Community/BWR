// ==========================================================================
// Copyright (C)2013 by Aaron Suen <warr1024@gmail.com>
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

// Replacement wolf class that supports cross-breeding, and breeding
// automation.
public class BWREntityWolf extends EntityWolf
	{
	// True if the wolf is "on drugs," which allows it to mate
	// while seated.
	public int onDrugsTime;

	public BWREntityWolf(World world)
		{
		super(world);
		this.onDrugsTime = 0;
		}

	// Called once per tick by the world.
	public void onLivingUpdate()
		{
		super.onLivingUpdate();

		// Do cross-breeding check.
		BWREngineBreedAnimal.getInstance().tryBreed(this);

		// If love timer is empty, clear drugs out of system; if
		// on drugs, and a mate has been chosen, trigger mating manually
		// (since sitting disables the breeding AI).
		if(this.onDrugsTime > 0)
			{
			if(this.isInLove() && (this.entityToAttack != null))
				{
				double dx = this.posX - this.entityToAttack.posX;
				double dy = this.posY - this.entityToAttack.posY;
				double dz = this.posZ - this.entityToAttack.posZ;
				this.attackEntity(this.entityToAttack,
					(float)Math.sqrt(dx * dx + dy * dy + dz * dz));
				}

			// Count down drug effects.  When they run out, wolf
			// will be left hungry again.
			this.onDrugsTime--;
			if(this.onDrugsTime <= 0)
				{
				this.onDrugsTime = 0;
				this.setFed(false);
				}
			}
		}

	// When the wolf is on drugs, treat it as if it's wearing a breeding
	// harness so that babies bred will spawn halfway between parents.
	public boolean getWearingBreedingHarness()
		{
		return ((this.health > 0) && (this.onDrugsTime > 0))
			|| super.getWearingBreedingHarness();
		}

	// Called when unloading chunk and/or serializing entity.
	public void writeToNBT(NBTTagCompound tag)
		{
		super.writeToNBT(tag);
		tag.setInteger("onDrugsTime", this.onDrugsTime);
		}

	// Called when loading chunk and/or deserializing entity.
	public void readFromNBT(NBTTagCompound tag)
		{
		super.readFromNBT(tag);
		this.onDrugsTime = tag.getInteger("onDrugsTime");
		}

	// Called whenever the wolf takes damage.
	public boolean attackEntityFrom(DamageSource source, int amount)
		{
		boolean r = super.attackEntityFrom(source, amount);
		if(!this.isDead && (this.onDrugsTime > 0))
			{
			// Wolves high on blasting oil will explode when hurt.
			this.onDrugsTime = 0;
			this.health = 0;
			this.onDeath(DamageSource.explosion);
			this.worldObj.createExplosion((Entity)null, this.posX, this.posY, this.posZ, 2, true);
			}
		return r;
		}

	// Called by BTW mod code to make wolves eat loose food off the ground
	// when hungry.
	public void CheckForLooseFood()
		{
		// Call base to eat food off the ground.  If the wolf was
		// already fed, or found no food, do nothing further here.
		if(this.isFed())
			{
			super.CheckForLooseFood();
			return;
			}
		super.CheckForLooseFood();
		if(!this.isFed() || (this.onDrugsTime > 0))
			return;

		// Puppies do not consume drugs.
		if(this.getGrowingAge() < 0)
			return;

		// Do another search for loose items on the ground to find additional
		// substances the wolves can consume as a recreational aphrodesiac.
		List list = this.worldObj.getEntitiesWithinAABB(EntityItem.class,
			AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(this.posX - 2.5D,
			this.posY - 1.0D, this.posZ - 2.5D, this.posX + 2.5D, this.posY + 1.0D, this.posZ + 2.5D));
         	if(!list.isEmpty())
			for(int idx = 0; idx < list.size(); ++idx)
				{
				// Find out if the entity represents a stack of the correct item.
				EntityItem ent = (EntityItem)list.get(idx);
				ItemStack item = ent.func_92059_d();
				if(ent.delayBeforeCanPickup > 0 || ent.isDead)
					continue;
				int id = item.itemID;
				if(id != mod_FCBetterThanWolves.fcItemBlastingOil.itemID)
					continue;

				// Set the dog as "on drugs."  In this state, it can breed while
				// seated.  Potion effect is for visual purposes.
				this.onDrugsTime = 600;
				this.setInLove(600);
				this.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 600, 0));

				// Consume one item from the stack.
				item.stackSize--;
				if(item.stackSize < 1)
					ent.setDead();
				break;
				}

		// If the wolf has succesfully consumed the aphrodesiac, we need to try to select
		// a mate manually, because while seated, the AI is turned off and won't do it.
		if((this.onDrugsTime > 0) && (this.entityToAttack == null))
			{
			list = this.worldObj.getEntitiesWithinAABB(EntityWolf.class,
				this.boundingBox.expand(3.5F, 3.5F, 3.5F));
         		if(!list.isEmpty())
				for(int idx = 0; idx < list.size(); ++idx)
					{
					EntityWolf wolf = (EntityWolf)list.get(idx);
					if((wolf != this) && wolf.isInLove())
						{
						this.entityToAttack = wolf;
						break;
						}
					}
			}
		}
	}
