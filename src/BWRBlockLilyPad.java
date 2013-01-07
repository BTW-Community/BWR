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

import java.util.Random;

// Replacement class for Vanilla lilypad block that adds some new behavior.
public class BWRBlockLilyPad extends BlockLilyPad
	{
	public BWRBlockLilyPad(int id, int txridx)
		{
		super(id, txridx);
		this.setTickRandomly(true);
		this.setRequiresSelfNotify();
		}

	// Used by BlockFlower superclass code as a quick check if the
	// plant should pop off.
	protected boolean canThisPlantGrowOnThisBlockID(int id)
		{
		// Lilypads can survive on either flowing or still water.
		return (id == Block.waterStill.blockID)
			|| (id == Block.waterMoving.blockID);
		}

	// A more thorough check, called on block update, to determine
	// if the block should pop off.
	public boolean canBlockStay(World world, int x, int y, int z)
		{
		if((y < 0) || (y > 255))
			return false;

		// Lilypads can survive on either flowing or still water.
		int id = world.getBlockId(x, y - 1, z);
		if((id != Block.waterStill.blockID)
			&& (id != Block.waterMoving.blockID))
			return false;

		// Lilypads require standard (>= 8) light level.
		if((world.getBlockLightValue(x, y, z) < 8)
			&& !world.canBlockSeeTheSky(x, y, z))
			return false;

		// Flowing water must be the deepest type, i.e the immediate
		// output of a stable-running screw pump, to work.
		if(world.getBlockMetadata(x, y - 1, z) > 1)
			return false;

		return true;
		}

	// Called randomly by World.
	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		// Count the number of lilypads surrounding.
		int neighbors = 0;
		for(int dx = -1; dx <= 1; dx++)
			for(int dz = -1; dz <= 1; dz++)
				{
				int b = world.getBlockId(x + dx, y, z + dz);
				if(b == blockID)
					neighbors++;
				}

		// Only spread if there are no other neighboring pads, to prevent
		// lilypads from filling up long-lived swamp chunks (even though this
		// would be closer to IRL behavior, it would be out of balance).
		// Growth rate is dependent on light level, so naturally-lit lilypads
		// will not grow at night.
		int ll = world.getBlockLightValue(x, y, z);
		if((neighbors < 2) && (r.nextInt(32) < ll))
			{
			// Try to place a lilypad block in a randomly-chosen adjacent spot.
			int rx = x + r.nextInt(3) - 1;
			int rz = z + r.nextInt(3) - 1;
			if((world.getBlockId(rx, y, rz) == 0) && canBlockStay(world, rx, y, rz))
				world.setBlockWithNotify(rx, y, rz, blockID);
			}
		}
	}
