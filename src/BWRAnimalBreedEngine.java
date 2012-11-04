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

		// Cross-breeding only happens in low-light conditions.  Maybe the animals
		// are embarrassed about it, or maybe they wouldn't do it if they could see...
		if(self.worldObj.getBlockLightValue(MathHelper.floor_double(self.posX),
			MathHelper.floor_double(self.posY), MathHelper.floor_double(self.posZ)) > 4)
			return false;

		// Search all possible candidates for mating or cross-breeding.
		List Mates = self.worldObj.getEntitiesWithinAABB(EntityAnimal.class, self.boundingBox.expand(8F, 8F, 8F));
		EntityAnimal Found = null;
		if((Mates != null) && (Mates.size() > 0))
			for(int I = 0; I < Mates.size(); I++)
				{
				EntityAnimal Ent = (EntityAnimal)Mates.get(I);

				// To cross-breed, partner must be in love, and unrelated.
				if(Ent.getClass().isAssignableFrom(self.getClass())
					|| self.getClass().isAssignableFrom(Ent.getClass())
					|| !Ent.isInLove())
					continue;

				// To cross-breed, animals must be in close proximity.  AI is
				// not modified, so they will not move together voluntarily,
				// i.e. moving them together is left as an exercise for the
				// player.
				double DX = Ent.posX - self.posX;
				double DY = Ent.posY - self.posY;
				double DZ = Ent.posZ - self.posZ;
				double DS = (DX * DX) + (DY * DY) + (DZ * DZ);
				if(DS > (3.5D * 3.5D))
					continue;

				// Tamable animals must be tamed and not sitting to cross-breed.
				if(Ent instanceof EntityTameable)
					{
					EntityTameable Tame = (EntityTameable)Ent;
					if(!Tame.isTamed() || Tame.isSitting())
						continue;
					}

				// Cross-breeding partner must also be in low light conditions.
				if(Ent.worldObj.getBlockLightValue(MathHelper.floor_double(Ent.posX),
					MathHelper.floor_double(Ent.posY), MathHelper.floor_double(Ent.posZ)) > 4)
					continue;

				Found = Ent;
				break;
				}
		if(Found == null)
			return false;

		// Cross-breeding happens randomly, but takes longer on average
		// than normal breeding.
		if(self.rand.nextInt(600) != 0)
			return false;

		// Use up the parents' "in love" status, and make them not breed
		// again for the normal refractory period.
		self.resetInLove();
		self.setGrowingAge(6000);
		Found.resetInLove();
		Found.setGrowingAge(6000);

		// Create a new child creature.
		// TODO: DEFINE LOGIC FOR DETERMINING CHILD SPECIES.
		Entity Child = new EntitySlime(self.worldObj);
		Child.setLocationAndAngles((self.posX + Found.posX) / 2.0D, (self.posY + Found.posY) / 2.0D,
			(self.posZ + Found.posZ) / 2.0D, self.rotationYaw, self.rotationPitch);
		self.worldObj.spawnEntityInWorld(Child);

		// Create heart particles.
		for(int X = 0; X < 7; X++)
			self.worldObj.spawnParticle("heart", self.posX + (double)(self.rand.nextFloat() * self.width * 2.0F),
				self.posY + 0.5D + (double)(self.rand.nextFloat() * self.height),
				self.posZ + (double)(self.rand.nextFloat() * self.width * 2.0F) - (double)self.width,
				self.rand.nextGaussian() * 0.02D, self.rand.nextGaussian() * 0.02D, self.rand.nextGaussian() * 0.02D);

		// Play breeding sounds for both parents.  Normally we only play one, but since the parents
		// are different species, they'll have different sounds which both need representation.
		self.worldObj.playSoundAtEntity(self, self.getDeathSound(), self.getSoundVolume(),
			(self.rand.nextFloat() - self.rand.nextFloat()) * 0.2F + 1.0F);
		Found.worldObj.playSoundAtEntity(Found, Found.getDeathSound(), Found.getSoundVolume(),
			(Found.rand.nextFloat() - Found.rand.nextFloat()) * 0.2F + 1.0F);

		// Create placenta.
		self.worldObj.playAuxSFX(2222, MathHelper.floor_double(Child.posX), MathHelper.floor_double(Child.posY),
			MathHelper.floor_double(Child.posZ), 0);

		return true;
		}
	}
