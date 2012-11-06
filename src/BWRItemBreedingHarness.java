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

import java.util.Random;

// Replacement class for the Breeding Harness that works around an issue with
// cow subclassing (used to support animal cross-breeding).
public class BWRItemBreedingHarness extends FCItemBreedingHarness
	{
	public BWRItemBreedingHarness(int id)
		{
		super(id);
		}

	// Called when the item is used on a living entity, i.e. by a player
	// right-clicking an animal with it equipped.
	public boolean useItemOnEntity(ItemStack stack, EntityLiving orig)
		{
		if((orig instanceof EntityCow) && !((EntityCow)orig).isChild()
			&& !((EntityCow)orig).getWearingBreedingHarness())
			{
			// Handle cows specially, external to upstream code.  The effect
			// is the same as originally intended, except that the correct
			// cow subclass is used, and the breeding harness is applied before
			// spawning the animal in the world, i.e. before BWR's code for
			// transforming (replacing) entities on spawn is called.
			BWREntityCow Cow = new BWREntityCow(orig.worldObj);
			Cow.setLocationAndAngles(orig.posX, orig.posY, orig.posZ, orig.rotationYaw, orig.rotationPitch);
			Cow.setEntityHealth(orig.getHealth());
			Cow.renderYawOffset = orig.renderYawOffset;
			Cow.setWearingBreedingHarness(true);
			orig.worldObj.spawnEntityInWorld(Cow);
			return true;
			}
		else
			return super.useItemOnEntity(stack, orig);
		}
	}
