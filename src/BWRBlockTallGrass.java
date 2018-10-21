// ==========================================================================
// Copyright (C)2018 by Aaron Suen <warr1024@gmail.com>
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
import java.util.List;

// Replacement class for grass blocks.
public class BWRBlockTallGrass extends FCBlockTallGrass
	{
	public BWRBlockTallGrass(int id, int txridx)
		{
		super(id, txridx);
		this.setTickRandomly(true);
		this.setRequiresSelfNotify();
		}

	// Called randomly by World.
	public void updateTick(World world, int x, int y, int z, Random rand)
		{
		super.updateTick(world, x, y, z, rand);

		// Animals spawn spontaneously only very rarely.
		if(rand.nextInt(480) > 0)
			return;

		// Animals can only spawn in direct natural sunlight.
		if(world.provider.hasNoSky
			|| !world.isDaytime()
			|| world.isRaining()
			|| !world.canBlockSeeTheSky(x, y + 1, z))
			return;

		// Look for nearby animals.  If there are any, none can spawn
		// spontaneously here.
		Double r = 32.0D;
		List near = world.getEntitiesWithinAABB(EntityAnimal.class,
			AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(
			x - r, y - r, z - r, x + r, y + r, z + r));
		if(near.size() > 0)
			return;

		// Look for nearby wheat or carrots.  Choose the species to
		// spawn based on the first food item found.  Only pigs and sheep
		// are available this way; all others must be cross-bred.
		EntityAnimal animal = null;
		List found = world.getEntitiesWithinAABB(EntityItem.class,
			AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(x - 2, y, z - 2, x + 3, y + 1, z + 3));
		if((found != null) && (found.size() > 0))
			for(int i = 0; i < found.size(); i++)
			{
			EntityItem item = (EntityItem)found.get(i);
			if(item.isDead)
				continue;
			ItemStack stack = item.func_92059_d();
			if(stack.stackSize < 1)
				continue;
			if(stack.itemID == Item.wheat.itemID)
				animal = new EntitySheep(world);
			else if(stack.itemID == Item.carrot.itemID)
				animal = new EntityPig(world);
			if(animal != null)
				{
				stack.stackSize--;
				if(stack.stackSize < 1)
					item.setDead();
				break;
				}
			}
		if(animal == null)
			return;

		// Create new animal and spawn in world.
		animal.setLocationAndAngles(x + 0.5D, y + 0.1D, z + 0.5D,
				world.rand.nextFloat() * 360.0F, 0F);
		world.spawnEntityInWorld(animal);

		// Show some special effects.
		world.playAuxSFX(2004, x, y, z, 0);
		}
	}
