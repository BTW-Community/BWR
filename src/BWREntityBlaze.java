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

// Replacement monster class for blazes that supports asexual reproduction.
public class BWREntityBlaze extends EntityBlaze
	{
	// Keep track of whether this is a "domestic" blaze or not, for determining
	// whether it will despawn or not.  Artificial blazes cost a whole block of
	// gold, so they should probably not despawn on their own.
	public boolean isArtificial;

	// After breeding, delay this time before breeding again.
	public int breedDelay;

	public BWREntityBlaze(World world)
		{
		super(world);
		this.isArtificial = false;
		}

	// Only blazes that were artificially-created, and their descendants, are
	// safe from automatically despawning.
	public boolean canDespawn()
		{
		return !this.isArtificial && super.canDespawn();
		}

	// Persist the "Artificial" flag when reading/writing entity to NBT.
	public void readEntityFromNBT(NBTTagCompound tag)
		{
		super.readEntityFromNBT(tag);
		this.isArtificial = tag.getBoolean("isArtificial");
		this.breedDelay = tag.getInteger("breedDelay");
		}
	public void writeEntityToNBT(NBTTagCompound tag)
		{
		super.writeEntityToNBT(tag);
		tag.setBoolean("isArtificial", this.isArtificial);
		tag.setInteger("breedDelay", this.breedDelay);
		}

	// Called once per tick by world.
	public void onLivingUpdate()
		{
		super.onLivingUpdate();

		// Add fire resistance to artificia blazes so they produce a special
		// particle effect to identify them (they're already immune to fire damage).
		if(this.isArtificial)
			this.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 60000, 0));

		// Delay before breeding again.
		if(this.breedDelay > 0)
			{
			this.breedDelay--;
			return;
			}

		// Select a random nearby block and check to see if it's fire.
		int x = MathHelper.floor_double(this.posX) + this.rand.nextInt(5) - 2;
		int y = MathHelper.floor_double(this.posY) + this.rand.nextInt(5) - 2;
		int z = MathHelper.floor_double(this.posZ) + this.rand.nextInt(5) - 2;
		int id = this.worldObj.getBlockId(x, y, z);
		if((id == Block.fire.blockID) || (id == mod_FCBetterThanWolves.fcBlockFireStoked.blockID))
			{
			// Limit population density by reducing, and eventually stopping, the
			// reproduction rate based on nearby population.
			List list = this.worldObj.getEntitiesWithinAABB(EntityBlaze.class,
				this.boundingBox.expand(8F, 8F, 8F));
			if((list.size() + 56) < this.rand.nextInt(64))
				{
				// Consume the fire block.
				this.worldObj.setBlockAndMetadataWithNotify(x, y, z, 0, 0);

				// Create a new blaze where the fire block was.
				BWREntityBlaze blaze = new BWREntityBlaze(this.worldObj);
				blaze.setLocationAndAngles(x + 0.5D, y, z + 0.5D, this.rotationYaw, this.rotationPitch);
				this.worldObj.spawnEntityInWorld(blaze);

				// Play sound and visual effects.
				for(int i = 0; i < 3; i++)
					this.worldObj.playAuxSFX(2004, x, y, z, 0);
				this.worldObj.playAuxSFX(2222, x, y, z, 0);
				this.worldObj.playSoundAtEntity(blaze, blaze.getDeathSound(), blaze.getSoundVolume(),
					(this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
				}

			// Delay before breeding again.
			this.breedDelay = 200 + this.rand.nextInt(600);
			}
		}
	}
