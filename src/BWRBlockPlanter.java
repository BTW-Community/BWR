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

// Replacement class for BTW planter block that adds some new behavior.
public class BWRBlockPlanter extends FCBlockPlanter
	{
	public BWRBlockPlanter(int id)
		{
		super(id);
		}

	// Called randomly by World.
	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		// Do plant/fungus cross-breeding checks if there is room above.
		if(y < 255)
			{
			int Meta = world.getBlockMetadata(x, y, z);

			// Soulsand planters can only grow fungus (netherwart), so skip plant checks.
			if(Meta == FCBlockPlanter.m_iTypeSoulSand)
				BWRPlantBreedEngine.m_instance.GrowFungus(world, x, y + 1, z);

			// Dirt planters can grow either plants or fungi.  Also attempt to grow
			// plants 2 blocks above, so that lilypads can grow onto water under which
			// a fertile planter is submerged.
			else if(Meta == FCBlockPlanter.m_iTypeSoilFertilized)
				if(!BWRPlantBreedEngine.m_instance.GrowPlant(world, x, y + 1, z)
					&& !((y < 254) && BWRPlantBreedEngine.m_instance.GrowPlant(world, x, y + 2, z)))
					BWRPlantBreedEngine.m_instance.GrowFungus(world, x, y + 1, z);
			}
		}
	}
