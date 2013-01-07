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

// Replacement class for Vanilla severed heads.
public class BWRBlockSkull extends BlockSkull
	{
	public BWRBlockSkull(int id)
		{
		super(id);
		}

	public void onBlockAdded(World world, int x, int y, int z)
		{
		super.onBlockAdded(world, x, y, z);

		// Check for golem recipe for blazes.
		if((y < 2)
			|| (this.getDamageValue(world, x, y, z) != 0)
			|| (world.getBlockId(x, y - 1, z) != Block.blockGold.blockID)
			|| (world.getBlockId(x + 1, y - 1, z) != Block.fenceIron.blockID)
			|| (world.getBlockId(x - 1, y - 1, z) != Block.fenceIron.blockID)
			|| (world.getBlockId(x, y - 1, z + 1) != Block.fenceIron.blockID)
			|| (world.getBlockId(x, y - 1, z - 1) != Block.fenceIron.blockID)
			|| ((world.getBlockId(x, y - 2, z) != Block.fire.blockID)
				&& (world.getBlockId(x, y - 2, z) != mod_FCBetterThanWolves.fcBlockFireStoked.blockID)))
			return;

		// Set a special metadata flag on the skull block that prevents
		// it from being dropped as an item when destroyed.
		world.setBlockMetadata(x, y, z, 8);

		// Destroy all blocks that will become the blaze.
		world.setBlockAndMetadata(x, y, z, 0, 0);
		world.setBlockAndMetadata(x, y - 1, z, 0, 0);
		world.setBlockAndMetadata(x + 1, y - 1, z, 0, 0);
		world.setBlockAndMetadata(x - 1, y - 1, z, 0, 0);
		world.setBlockAndMetadata(x, y - 1, z + 1, 0, 0);
		world.setBlockAndMetadata(x, y - 1, z - 1, 0, 0);
		world.setBlockAndMetadata(x, y - 2, z, 0, 0);

		// Send BUD events after all blocks have been removed.
		world.notifyBlockChange(x, y, z, 0);
		world.notifyBlockChange(x, y - 1, z, 0);
		world.notifyBlockChange(x + 1, y - 1, z, 0);
		world.notifyBlockChange(x - 1, y - 1, z, 0);
		world.notifyBlockChange(x, y - 1, z + 1, 0);
		world.notifyBlockChange(x, y - 1, z - 1, 0);
		world.notifyBlockChange(x, y - 2, z, 0);

		// Spawn a new blaze where the golem construct was.
		EntityBlaze blaze = new EntityBlaze(world);
		blaze.setLocationAndAngles(x + 0.5D, y, z + 0.5D, 0, 0);
		world.spawnEntityInWorld(blaze);

		// Play sound and visual effects.
		for(int i = 0; i < 3; i++)
			world.playAuxSFX(2004, x, y, z, 0);
		world.playSoundAtEntity(blaze, blaze.getDeathSound(), blaze.getSoundVolume(),
			(world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
		}
	}
