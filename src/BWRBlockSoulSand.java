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

// Replacement class for Vanilla soul sand, which adds some new behavior.
public class BWRBlockSoulSand extends BlockSoulSand
	{
	public BWRBlockSoulSand(int id, int txridx)
		{
		super(id, txridx);
		this.setTickRandomly(true);
		this.setRequiresSelfNotify();
		}

	public void updateTick(World world, int x, int y, int z, Random r)
		{
		super.updateTick(world, x, y, z, r);

		// If there is room above, attempt to cross-breed fungus (netherwart)
		// into the space above.
		if(y < 255)
			BWREngineBreedPlant.getInstance().grow(world, x, y + 1, z);
		}
	}
