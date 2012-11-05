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

// Singleton that provides some utility functions for animal cross-breeding,
// allowing otherwise unavailable animal species to be obtained in
// worlds where only a limited set are available, e.g. obtaining wolves
// on a Classic SuperFlat world.
public class BWRAnimalBreedEngine {
	public static BWRAnimalBreedEngine m_instance = new BWRAnimalBreedEngine();

	// One-time initialization of the engine, called by the mod startup.
	public void Initialize()
		{
		// Stub.  Normally I'd initialize some fast lookup tables or
		// something here, but the engine doesn't currently use any of that.
		}

	// Do all cross-breeding checks.  Called by EntityUpdate on all animal
	// entities that are capable of cross-breeding.
	public boolean TryBreed(EntityAnimal self)
		{
		// Animal trying to cross-breed must be in love, and reaching the
		// end of the "in love" period (i.e. "desperate").
		if(!self.isInLove() || (self.getInLove() > 20))
			return false;

		// Cross-breeding only happens in low-light conditions.  Maybe the animals
		// are embarrassed about it, or maybe they wouldn't do it if they could see...
		int BX = MathHelper.floor_double(self.posX);
		int BY = MathHelper.floor_double(self.posY);
		int BZ = MathHelper.floor_double(self.posZ);
		World world = self.worldObj;
		if(world.getBlockLightValue(BX, BY, BZ) > 4)
			return false;

		// Search all possible candidates for mating or cross-breeding.
		List Mates = world.getEntitiesWithinAABB(EntityAnimal.class, self.boundingBox.expand(8F, 8F, 8F));
		EntityAnimal Found = null;
		if((Mates != null) && (Mates.size() > 0))
			for(int I = 0; I < Mates.size(); I++)
				{
				EntityAnimal Ent = (EntityAnimal)Mates.get(I);
				if(Ent == self)
					continue;

				// If there are any nearby animals of same/related species,
				// this animal will not cross-breed.  Note that if you have
				// many of species A and only one of B, A will not mate with
				// B, but B will still mate with A (it just takes longer).
				if(Ent.getClass().isAssignableFrom(self.getClass())
					|| self.getClass().isAssignableFrom(Ent.getClass()))
					return false;

				// To cross-breed, partner must be in love.
				if(!Ent.isInLove())
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
				if(world.getBlockLightValue(MathHelper.floor_double(Ent.posX),
					MathHelper.floor_double(Ent.posY), MathHelper.floor_double(Ent.posZ)) > 4)
					continue;

				Found = Ent;
				break;
				}
		if(Found == null)
			return false;

		// Cross-breeding happens randomly within the time window, so there's still
		// a probability that it won't happen at all this attempt.  This works out
		// to be either a 66% or 45% chance the breed will fail, depending on whether
		// one or both parents is without intra-species prospects.
		if(self.rand.nextInt(50) != 0)
			return false;

		// Use up the parents' "in love" status, and make them not breed
		// again for the normal refractory period.
		self.resetInLove();
		self.setGrowingAge(6000);
		Found.resetInLove();
		Found.setGrowingAge(6000);

		// Setup to create new child creature.  Figure
		// out where the child creature would be created.
		EntityLiving Child = null;
		EntityLiving Target = null;
		double CX = (self.posX + Found.posX) / 2.0D;
		double CY = (self.posY + Found.posY) / 2.0D;
		double CZ = (self.posZ + Found.posZ) / 2.0D;

		// There's an 80% chance that one or the other of the parent species
		// is highly dominant, and no mutation occurs.
		int P = self.rand.nextInt(100);
		if(P < 40)
			Child = (EntityLiving)EntityList.createEntityByName(self.getEntityName(), world);
		else if(P < 80)
			Child = (EntityLiving)EntityList.createEntityByName(Found.getEntityName(), world);

		// Most mutations will be other farm animals that would normally be
		// found by trivial exploration.  Chickens have a reduced probability
		// due to their evolutionary distance from other animals that breed
		// live offspring.
		else if(P < 84)
			Child = new EntityCow(world);
		else if(P < 88)
			Child = new EntityPig(world);
		else if(P < 92)
			Child = new EntitySheep(world);
		else if(P < 95)
			Child = new EntityChicken(world);

		// If a mutation occurs, first check the environment.  Animals will "evolve"
		// based on their environment, to add a block design/building aspect to
		// cross-breeding, and to avoid completely bypassing the biome requirements
		// (players must at least "simulate" the necessary biome).
		if(Child == null)
			{
			// Count pine and jungle leaves, and water tiles, to affect the
			// probability of wolves, ocelots, and squid, respectively.
			int PineQty = 0;
			int JungleQty = 0;
			int WaterQty = 0;

			// Search a 9x9 area around where the child would appear.
			int CBX = MathHelper.floor_double(CX);
			int CBY = MathHelper.floor_double(CY);
			int CBZ = MathHelper.floor_double(CZ);
			for(int dx = -4; dx <= 4; dx++)
				for(int dz = -1; dz <= 4; dz++)
					{
					int X = CBX + dx;
					int Z = CBZ + dz;

					// Scan downwards for 9 blocks within each area space,
					// sweeping out a 9x9x9 area.  This optimizes the sky visibility
					// check, which can be skipped after our first block that cannot
					// see the sky.
					boolean Sky = world.canBlockSeeTheSky(X, CBY + 10, Z);
					for(int dy = 4; dy >= -4; dy--)
						{
						int Y = CBY + dy;
						int ID = world.getBlockId(X, Y, Z);

						// Count all water tiles in the volume.
						if((ID > 0) && (Block.blocksList[ID].blockMaterial == Material.water))
							{
							WaterQty++;
							Sky = false;
							continue;
							}

						if(Sky)
							{
							// Count leaf blocks that can see the sky.  Only
							// pine and jungle are relevant.
							if(ID == Block.leaves.blockID)
								{
								int Meta = world.getBlockMetadata(X, Y, Z) & 3;
								switch(Meta)
									{
									case 1:
										PineQty++;
										break;
									case 3:
										JungleQty++;
										break;
									}
								}

							// If any block is blocking our view of the sky,
							// we no longer have to do leaf checks in this
							// column.
							if(!world.canBlockSeeTheSky(X, Y, Z))
								Sky = false;
							}
						}
					}

			// Squid can only spawn inside actual water, so it may be necessary
			// to flow water between the parents.  Probability of a squid spawning
			// is about 7/8 if the entire 729 spaces are filled with water, which
			// is unlikely because parents will need air and something to stand on.
			if((world.getBlockMaterial(CBX, CBY, CBZ) == Material.water)
				&& (self.rand.nextInt(800) < WaterQty))
				Child = new EntitySquid(world);

			// The absolute maximum number of pine leaves that can see the sky that
			// can be in the 9x9x9 volume is 81, which would give about 80% chance
			// of spawning a wolf.  A wolf must have at least one sheep parent, and
			// will attack that parent upon birth.
			else if(self.rand.nextInt(100) < PineQty)
				{
				EntityLiving Sheep = (self instanceof EntitySheep) ? self
					: (Found instanceof EntitySheep) ? Found
					: null;
				if(Sheep != null)
					{
					Child = new EntityWolf(world);
					Target = Sheep;
					}
				}

			// The absolute maximum number of jungle leaves that can see the sky that
			// can be in the 9x9x9 volume is 81, which would give about 80% chance
			// of spawning an ocelot.
			else if(self.rand.nextInt(100) < JungleQty)
				Child = new EntityOcelot(world);
			}

		// For all other mutants, produce an abomination of nature.
		if(Child == null)
			{
			P = world.rand.nextInt(8);
			if(P == 0)
				Child = new EntitySlime(world);
			else if(P < 3)
				Child = new EntityCaveSpider(world);
			else
				Child = new EntitySilverfish(world);

			// Abominations attack a random parent initially.
			Target = self.rand.nextInt(2) == 0 ? self : Found;
			}

		// Setup some parameters of the child.  Children always spawn
		// between parents.  Slimes are always smallest size, and ageable
		// mobs are always children.  Set child AI to attack the selected
		// target if one defined above.
		if(Child instanceof EntitySlime)
			((EntitySlime)Child).setSlimeSize(1);
		if(Child instanceof EntityAgeable)
			((EntityAgeable)Child).setGrowingAge(-24000);
		if(Target != null)
			Child.setAttackTarget(Target);
		Child.setLocationAndAngles((self.posX + Found.posX) / 2.0D, (self.posY + Found.posY) / 2.0D,
			(self.posZ + Found.posZ) / 2.0D, self.rotationYaw, self.rotationPitch);
		world.spawnEntityInWorld(Child);

		// Play breeding sounds for both parents.  Normally we only play one, but since the parents
		// are different species, they'll have different sounds which both need representation.
		world.playSoundAtEntity(self, self.getDeathSound(), self.getSoundVolume(),
			(self.rand.nextFloat() - self.rand.nextFloat()) * 0.2F + 1.0F);
		world.playSoundAtEntity(Found, Found.getDeathSound(), Found.getSoundVolume(),
			(Found.rand.nextFloat() - Found.rand.nextFloat()) * 0.2F + 1.0F);

		// Add confusion effect to parents.  They'd have to be confused...
		self.addPotionEffect(new PotionEffect(Potion.confusion.id, 300, 0));
		Found.addPotionEffect(new PotionEffect(Potion.confusion.id, 300, 0));

		// Create placenta.
		world.playAuxSFX(2222, MathHelper.floor_double(Child.posX), MathHelper.floor_double(Child.posY),
			MathHelper.floor_double(Child.posZ), 0);

		return true;
		}
	}
