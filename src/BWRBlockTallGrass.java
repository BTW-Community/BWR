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
import java.util.List;

// Replacement class for grass blocks.
public class BWRBlockTallGrass extends BlockTallGrass
	{
	public BWRBlockTallGrass(int id, int txridx)
		{
		super(id, txridx);
		this.setTickRandomly(true);
		this.setRequiresSelfNotify();
		}

	// Called randomly by World.
	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		// Animals spawn spontaneously only extremely rarely.
		if(world.rand.nextInt(2400) > 0)
			return;

		// Animals can only spawn in direct natural sunlight.
                if(world.provider.hasNoSky
                        || !world.isDaytime()
                        || world.isRaining()
                        || !world.canBlockSeeTheSky(x, y + 1, z))
			return;

		// Look for nearby animals.  If there are any, none can spawn
		// spontaneously here.
		Double R = 32.0D;
		List Near = world.getEntitiesWithinAABB(EntityAnimal.class,
			AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(
			x - R, y - R, z - R, x + R, y + R, z + R));
		if(Near.size() > 0)
			return;

		// Choose a random animal species.
		EntityAnimal A = null;
		switch(world.rand.nextInt(4))
			{
			case 0:
				A = new EntityCow(world);
				break;
			case 1:
				A = new EntityPig(world);
				break;
			case 2:
				A = new EntitySheep(world);
				break;
			default:
				A = new EntityChicken(world);
				break;
			}

		// Create new animal and spawn in world.
		A.setLocationAndAngles(x + 0.5D, y + 0.1D, z + 0.5D, world.rand.nextFloat() * 360.0F, 0F);
		world.spawnEntityInWorld(A);
		}
	}
