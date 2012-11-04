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

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

public class BWRAnimalBreedEngine {
	public static BWRAnimalBreedEngine m_instance = new BWRAnimalBreedEngine();

	public void Initialize()
		{
		}

	public boolean TryBreed(EntityAnimal self)
		{
		// Animal trying to cross-breed must be in love.
		if(!self.isInLove())
			return false;

		// Search all possible candidates for mating or cross-breeding.
		List Mates = self.worldObj.getEntitiesWithinAABB(EntityAnimal.class, self.boundingBox.expand(8F, 8F, 8F));
		EntityAnimal Found = null;
		if((Mates != null) && (Mates.size() > 0))
			for(int I = 0; I < Mates.size(); I++)
				{
				EntityAnimal Ent = (EntityAnimal)Mates.get(I);

				// To cross-breed, partner must be in love, and unrelated.
				if(Ent.getClass().isAssignableFrom(self.getClass()))
					{
					mod_BetterWithRenewables.m_instance.Log("reject 1");
					continue;
					}
				if(self.getClass().isAssignableFrom(Ent.getClass()))
					{
					mod_BetterWithRenewables.m_instance.Log("reject 2");
					continue;
					}
				if(!Ent.isInLove())
					{
					mod_BetterWithRenewables.m_instance.Log("reject 3");
					continue;
					}

				// To cross-breed, animals must be in close proximity.  AI is
				// not modified, so they will not move together voluntarily,
				// i.e. moving them together is left as an exercise for the
				// player.
				double DX = Ent.posX - self.posX;
				double DY = Ent.posY - self.posY;
				double DZ = Ent.posZ - self.posZ;
				double DS = (DX * DX) + (DY * DY) + (DZ * DZ);
				if(DS > (3.5D * 3.5D))
					{
					mod_BetterWithRenewables.m_instance.Log("reject 4");
					continue;
					}

				Found = Ent;
				break;
				}
		if(Found == null)
			return false;

		return true;
		}
	}
