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

public class BWRBlockRedstoneWire extends BlockRedstoneWire
	{
	public BWRBlockRedstoneWire(int id, int txridx)
		{
		super(id, txridx);
		}

	public void onNeighborBlockChange(World world, int x, int y, int z, int i)
		{
		if((y >= 255)
			|| world.provider.hasNoSky
			|| !world.isDaytime()
			|| world.isRaining()
			|| !world.canBlockSeeTheSky(x, y + 1, z)
			|| (world.getBlockId(x, y + 1, z) != mod_FCBetterThanWolves.fcLens.blockID))
			{
			super.onNeighborBlockChange(world, x, y, z, i);
			return;
			}
			
		int OldMeta = world.getBlockMetadata(x, y, z);
		super.onNeighborBlockChange(world, x, y, z, i);
		int NewMeta = world.getBlockMetadata(x, y, z);

		if((NewMeta - OldMeta) > world.rand.nextInt(160))
			{
			FCUtilsItem.EjectSingleItemWithRandomOffset(world, x, y, z,
				Item.lightStoneDust.shiftedIndex, 0);
			world.setBlockAndMetadata(x, y, z, 0, 0);
			}
		}
	}
