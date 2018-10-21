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

// Replacement class for BTW Aesthetic Opaque block, which includes "storage"
// blocks such as dung, hellfire, rope, and wicker.  This class adds some
// custom behavior when these blocks are placed in the world, so they aren't
// just for storage anymore.
public class BWRBlockAestheticOpaque extends FCBlockAestheticOpaque
	{
	public BWRBlockAestheticOpaque(int id)
		{
		super(id);

		// Make sure this block is set to tick randomly, as certain
		// subtypes (e.g. dung, hellfire) have custom BWR code.
		this.setTickRandomly(true);
		this.setRequiresSelfNotify();
		}

	// Called randomly by World.
	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		int type = world.getBlockMetadata(x, y, z);
		if(type == FCBlockAestheticOpaque.m_iSubtypeDung)
			{
			// For dung blocks to create dirt/clay, they must have water above.
			int above = world.getBlockId(x, y + 1, z);
			if((above == Block.waterStill.blockID) || (above == Block.waterMoving.blockID))
				{
				// Calculate the amount of heat available to speed up the reaction.
				// Ambient temperature is 10, plus 1 for each fire, 3 for every stoked
				// fire.  Search downwards for the first non-solid-cube block, then
				// search a 3x3 for fire.  This means the max heat available is 37.
				// Note that a non-solid non-cube block above the fire will act as an
				// insulator.
				int heat = 10;
				for(int dy = 1; dy <= 3; dy++)
					{
					if(!world.isBlockNormalCube(x, y - dy, z))
						{
						for(int dx = -1; dx <= 1; dx++)
							for(int dz = -1; dz <= 1; dz++)
								{
								int b = world.getBlockId(x + dy, y - dy, z);
								if(b == FCBetterThanWolves.fcBlockFireStoked.blockID)
									heat += 3;
								else if(b == Block.fire.blockID)
									heat += 1;
								}
						break;
						}
					}

				// Reaction proceeds stochastically, with probability proportional to heat,
				// so adding 3x3 stoked flame shortens the half-life by almost 75%.
				if(r.nextInt(4800) < heat)
					{
					// Acid is washed from dung block, leaving behind dirt suitable for
					// farming applications.
					world.setBlockAndMetadataWithNotify(x, y, z, Block.dirt.blockID, 0);

					// Check for sand below the dung.
					int b = world.getBlockId(x, y - 1, z);
					if(b == Block.sand.blockID)
						{
						// If there is sand, the acid being washed down from the dung
						// block will etch it into finer clay particles, and produce
						// a hissing sound.
						world.playSoundEffect(x, y, z, "random.fizz", 1.0F,
							1.0F + world.rand.nextFloat() * 0.5F);
						world.setBlockAndMetadataWithNotify(x, y - 1, z, Block.blockClay.blockID, 0);
						}
					}
				}
			}
		else if(type == m_iSubtypeHellfire)
			{
			// Concentrated hellfire blocks can turn into lava source blocks when
			// placed near existing lava.  Count the number of lava and source
			// blocks nearby.
			int nearby = 0;
			int sources = 0;
			for(int dx = -1; dx <= 1; dx++)
				for(int dy = -1; dy <= 1; dy++)
					for(int dz = -1; dz <= 1; dz++)
						{
						int id = world.getBlockId(x + dx, y + dy, z + dz);
						if(id == Block.lavaStill.blockID)
							{
							sources++;
							nearby++;
							}
						else if(id == Block.lavaMoving.blockID)
							nearby++;
						}

			// There must be at least one lava source block adjacent to the hellfire,
			// but flowing lava also counts towards reaction heat, so reflowing lava
			// to surround the hellfire produces the fastest reaction speed.
			if((sources > 0) && (r.nextInt(1200) < nearby))
				{
				// Create some fire visual and sound effects, and replace
				// the hellfire block with a lava source block.
				for(int i = 0; i < 3; i++)
					world.playAuxSFX(2004, x, y, z, 0);
				world.playSoundEffect(x, y, z, "lava.pop", 1.0F, world.rand.nextFloat() * 0.5F);
				world.setBlockAndMetadataWithNotify(x, y, z, Block.lavaStill.blockID, 0);
				}
			}
		}
	}
