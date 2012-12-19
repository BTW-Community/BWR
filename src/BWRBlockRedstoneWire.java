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

// Replacement class for redstone wire that adds glowstone creation recipe.
public class BWRBlockRedstoneWire extends BlockRedstoneWire
	{
	public BWRBlockRedstoneWire(int id, int txridx)
		{
		super(id, txridx);
		this.setTickRandomly(false);
		}

	// Called when a neighbor block changes, including redstone signal updates.
	public void onNeighborBlockChange(World world, int x, int y, int z, int i)
		{
		// Perform normal update, and keep before/after metadata (signal strength)
		// for comparison.
		int oldmeta = world.getBlockMetadata(x, y, z);
		super.onNeighborBlockChange(world, x, y, z, i);
		int newmeta = world.getBlockMetadata(x, y, z);

		// Only continue on a "rising" clock edge.
		if(newmeta <= oldmeta)
			return;

		// Probability of continuing the effect is proportional to the change
		// in signal strength.
		if((newmeta - oldmeta) < world.rand.nextInt(128))
			return;

		// Make sure that we have a downward-facing lens immediately above the redstone
		// wire to concentrate sunlight into it, and that we have the requisite sunlight.
		if((y >= 255)
			|| world.provider.hasNoSky
			|| (world.getBlockId(x, y + 1, z) != mod_FCBetterThanWolves.fcLens.blockID)
			|| (world.getBlockMetadata(x, y + 1, z) != 0)
			|| !world.isDaytime()
			|| world.isRaining()
			|| !world.canBlockSeeTheSky(x, y + 2, z))
			return;

		// Delay about a quarter of a second after the clock edge before actually
		// applying the transformation effect.  This discourages the use of very fast
		// clocks that eat CPU and network chunk update bandwidth, and fixes an
		// exploit of sorts (discovered by Noir) that makes a glowstone-making machine
		// easy to build using only a clock and block dispenser.
		if(!world.IsUpdateScheduledForBlock(x, y, z, this.blockID))
			world.scheduleBlockUpdate(x, y, z, this.blockID, 9);
		}

	// Delayed tick event, called by World, and schduled by onNeighborBlockChange.
	public void updateTick(World world, int x, int y, int z, Random r)
		{
		// Play the effect of redstone wire being broken.
		world.playAuxSFX(2001, x, y, z, this.blockID);

		// Remove the existing redstone wire.  Save the old metadata
		// value in case we have to replace it.
		int meta = world.getBlockMetadata(x, y, z);
		world.setBlockAndMetadataWithNotify(x, y, z, 0, 0);

		// 10% of the time, pop glowstone dust off as an item.
		if(world.rand.nextInt(10) == 0)
			{
			FCUtilsItem.EjectSingleItemWithRandomOffset(world, x, y, z,
				Item.lightStoneDust.shiftedIndex, 0);
			return;
			}

		// The other 90% of the time, we get a "spurious block update" effect,
		// in which the redstone wire is placed back as it was; this makes
		// detecting the reaction more challenging, as a buddy block won't work.
		world.setBlockAndMetadata(x, y, z, this.blockID, meta);
		}
	}
