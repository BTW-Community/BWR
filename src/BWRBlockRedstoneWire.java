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

		// The probability of glowstone transformation is proportional to the amount
		// of signal strength change.
		if((newmeta - oldmeta) < world.rand.nextInt(6400))
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

		// Pop off glowstone dust as an item.
		FCUtilsItem.EjectSingleItemWithRandomOffset(world, x, y, z,
			Item.lightStoneDust.shiftedIndex, 0);
		world.setBlockAndMetadataWithNotify(x, y, z, 0, 0);
		}
	}
