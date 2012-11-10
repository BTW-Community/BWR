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
import java.util.Map;
import java.util.HashMap;

// Singleton that provides some utility functions for animal cross-breeding,
// allowing otherwise unavailable animal species to be obtained in
// worlds where only a limited set are available, e.g. obtaining wolves
// on a Classic SuperFlat world.
public class BWRAnimalBreedEngine {
	public static BWRAnimalBreedEngine m_instance = new BWRAnimalBreedEngine();

	// Entity ID constants.
	private static final int eidSlime = 55;
	private static final int eidCaveSpider = 59;
	private static final int eidSilverfish = 60;
	private static final int eidBlaze = 61;
	private static final int eidPig = 90;
	private static final int eidSheep = 91;
	private static final int eidCow = 92;
	private static final int eidChicken = 93;
	private static final int eidSquid = 94;
	private static final int eidWolf = 95;
	private static final int eidOcelot = 98;
	private static final int eidVillager = 120;

	// One-time initialization of the engine, called by the mod startup.
	public void Initialize()
		{
		// Stub.  Normally I'd initialize some fast lookup tables or
		// something here, but the engine doesn't currently use any of that.
		}

	// Routines for recursively scanning the prospective habitat of a new creature,
	// using a 3D flood-fill algorithm like block lighting.  It adds up the quantities
	// of all blocks found by ID/metadata.
	private static final int MaxScanDepth = 10;
	private void ScanHabitat(World world, int x, int y, int z, int[] results, Map<String, Integer> seen, int depth)
		{
		// Check to see if the block has been visited before.
		String Key = x + "," + y + "," + z;
		Integer Prev = seen.get(Key);

		// If the block has not been visited, analyze it.
		if(Prev == null)
			{
			// Get the type of block in this space.
			int ID = world.getBlockId(x, y, z);

			// For wood and leaves, pay attention to the metadata, for others
			// just treat it as 0 and lump together all blocks with the same ID.
			int Meta = 0;
			if((ID == Block.wood.blockID) || (ID == Block.leaves.blockID))
				Meta = world.getBlockMetadata(x, y, z) & 3;

			// Increment block count.
			results[(ID * 16) + Meta]++;

			// If the block is passable, record the depth at which we scanned it.
			// If not, mark it so future passes will know not to continue.
			if((ID == 0) || !Block.blocksList[ID].blockMaterial.isSolid())
				seen.put(Key, depth);
			else
				seen.put(Key, Prev = MaxScanDepth);
			}

		// Do not scan further if we've reached the depth limit.
		if(depth <= 0)
			return;

		// If this block has not been scanned, or if a previous scan was done
		// at a lower depth, then recurse to neighboring blocks.
		if((Prev == null) || (Prev < depth))
			{
			ScanHabitat(world, x - 1, y, z, results, seen, depth - 1);
			ScanHabitat(world, x + 1, y, z, results, seen, depth - 1);
			ScanHabitat(world, x, y - 1, z, results, seen, depth - 1);
			ScanHabitat(world, x, y + 1, z, results, seen, depth - 1);
			ScanHabitat(world, x, y, z - 1, results, seen, depth - 1);
			ScanHabitat(world, x, y, z + 1, results, seen, depth - 1);
			}
		}
	private int[] ScanHabitat(World world, int x, int y, int z)
		{
		// Create the results array, seen hashset, and default depth,
		// and start the scan with default settings.
		int[] Results = new int[Block.blocksList.length * 16];
		ScanHabitat(world, x, y, z, Results, new HashMap<String, Integer>(), MaxScanDepth);
		return Results;
		}

	// Helper function to add probability to a map of weighted probabilties.
	private void AddProb(Map<Integer, Integer> map, int key, int val)
		{
		Integer I = map.get(new Integer(key));
		map.put(key, (I != null) ? (I + val) : new Integer(val));
		}

	// Do all cross-breeding checks.  Called by EntityUpdate on all animal
	// entities that are capable of cross-breeding.
	public boolean TryBreed(EntityAnimal self)
		{
		// ########## STEP 1: DETERMINE IF CROSS-BREEDING HAPPENS

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
		// a probability that it won't happen at all this attempt.
		if(self.rand.nextInt(20) != 0)
			return false;

		// ########## STEP 2: DETERMINE CHILD SPECIES

		// Setup a table of probabilities for each creature type.
		Map<Integer, Integer> Weights = new HashMap<Integer, Integer>();

		// Standard weights: animals that bear live offspring have
		// a default base probability, while chickens, which reproduce
		// via egg-laying, are a less likely mutation.
		AddProb(Weights, eidCow, 100);
		AddProb(Weights, eidPig, 100);
		AddProb(Weights, eidSheep, 100);
		AddProb(Weights, eidChicken, 50);

		// Add high probability that parents' genetic contribution
		// is completely dominant and no mutation occurs.
		AddProb(Weights, EntityList.getEntityID(self), 500);
		AddProb(Weights, EntityList.getEntityID(Found), 500);

		// Add base probabilties of certain abominations.
		AddProb(Weights, eidSilverfish, 100);
		AddProb(Weights, eidCaveSpider, 50);
		AddProb(Weights, eidSlime, 10);

		// Calculate child's location, and measure environment there.
		double CX = (self.posX + Found.posX) / 2.0D;
		double CY = (self.posY + Found.posY) / 2.0D;
		double CZ = (self.posZ + Found.posZ) / 2.0D;
		int CBX = MathHelper.floor_double(CX);
		int CBY = MathHelper.floor_double(CY);
		int CBZ = MathHelper.floor_double(CZ);
		int[] Habitat = ScanHabitat(world, CBX, CBY, CBZ);

		// If in development, log the habitat profile information.
		if(mod_BetterWithRenewables.bwrDevVersion)
			{
			String HDebug = "Animal Cross-Breed Habitat Data:";
			for(int I = 0; I < Habitat.length; I++)
				if(Habitat[I] > 0)
					HDebug += " " + (((I / 16) == 0) ? "Air"
						: Block.blocksList[I / 16].getBlockName())
						+ "[" + (I % 16) + "]:" + Habitat[I];
			mod_BetterWithRenewables.m_instance.Log(HDebug);
			}

		// Choose a random child species based on weighted probabilities
		// If the tile into which the child would spawn is water, then
		// a squid is possible, and encouraged by nearby water.
		if(world.getBlockMaterial(CBX, CBY, CBZ) == Material.water)
			AddProb(Weights, eidSquid, Habitat[Block.waterMoving.blockID * 16]
				+ Habitat[Block.waterStill.blockID * 16]);

		// Add probabilities for wolves based on ratios of pine
		// logs, leaves, and snow to simuate a tundra biome.
		int PineLogs = Habitat[Block.wood.blockID * 16 + 1] * 20;
		int PineLeaves = Habitat[Block.leaves.blockID * 16 + 1];
		int Pine = (PineLogs > PineLeaves) ? PineLeaves : PineLogs;
		AddProb(Weights, eidWolf, Pine / 10);
		int Snow = Habitat[Block.snow.blockID] + Habitat[Block.blockSnow.blockID] * 2;
		Snow = (Snow > Pine) ? Pine : Snow;
		AddProb(Weights, eidWolf, Snow / 10);

		// Ocelots are encouraged by jungle wood and leaves, vines,
		// and cocoa pods.
		int JungLogs = Habitat[Block.wood.blockID * 16 + 3] * 5;
		int JungLeaves = Habitat[Block.leaves.blockID * 16 + 3];
		int Jung = (JungLogs > JungLeaves) ? JungLeaves : JungLogs;
		AddProb(Weights, eidOcelot, Jung / 10);
		int Vines = Habitat[Block.vine.blockID] * 2;
		Vines = (Vines > Jung) ? Jung : Vines;
		AddProb(Weights, eidOcelot, Vines / 20);
		int Cocoa = Habitat[Block.cocoaPlant.blockID] * 3;
		Cocoa = (Cocoa > Jung) ? Jung : Cocoa;
		AddProb(Weights, eidOcelot, Cocoa / 20);

		// Blazes require nether brick to spawn, plus fire and
		// lava encourage them.
		int Brick = Habitat[Block.netherBrick.blockID]
			+ (Habitat[Block.stairsNetherBrick.blockID] * 3 / 4)
			+ (Habitat[Block.netherFence.blockID] / 2);
		AddProb(Weights, eidBlaze, Brick / 50);
		int Fire = Habitat[Block.fire.blockID]
			+ (Habitat[mod_FCBetterThanWolves.fcStokedFire.blockID] * 3)
			+ ((Habitat[Block.lavaMoving.blockID]
				+ Habitat[Block.lavaStill.blockID]) / 2);
		Fire = (Fire > Brick) ? Brick : Fire;
		AddProb(Weights, eidBlaze, Fire / 40);

		// Villagers will rarely spawn if surrounded by blocks they desire.
		AddProb(Weights, eidVillager, (Habitat[Block.blockEmerald.blockID] / 100)
			+ (Habitat[Block.blockDiamond.blockID] / 30));

		// And create a new instance.
		EntityLiving Child = null;
		int Max = 0;
		for(Map.Entry<Integer, Integer> P : Weights.entrySet())
			Max += P.getValue();
		int Pick = self.rand.nextInt(Max);
		for(Map.Entry<Integer, Integer> P : Weights.entrySet())
			{
			Pick -= P.getValue();
			if(Pick <= 0)
				{
				Child = (EntityLiving)EntityList.createEntityByID(
					P.getKey().intValue(), world);
				break;
				}
			}

		// If in development, log probability profile of animal cross-breeds.
		if(mod_BetterWithRenewables.bwrDevVersion)
			{
			String PDebug = "Animal Cross-Breeding Weights:";
			for(Map.Entry<Integer, Integer> P : Weights.entrySet())
				if(P.getValue() > 0)
					PDebug += " " + EntityList.func_75617_a(P.getKey().intValue())
						+ ":" + P.getValue();
			PDebug += " TOTAL:" + Max;
			mod_BetterWithRenewables.m_instance.Log(PDebug);
			}

		// Choose a random child species based on weighted probabilities
		// ########## STEP 3: COMPLETE BREEDING PROCESs

		// Final setup of child parameters, and place child in the world.
		if(Child instanceof EntitySlime)
			((EntitySlime)Child).setSlimeSize(1);
		if(Child instanceof EntityAgeable)
			((EntityAgeable)Child).setGrowingAge(-24000);
		Child.setAttackTarget(self.rand.nextInt(2) == 0 ? self : Found);
		Child.setLocationAndAngles((self.posX + Found.posX) / 2.0D, (self.posY + Found.posY) / 2.0D,
			(self.posZ + Found.posZ) / 2.0D, self.rotationYaw, self.rotationPitch);
		world.spawnEntityInWorld(Child);

		// Special case: blazes set parents on fire upon birth.
		if(Child instanceof EntityBlaze)
			{
			self.setFire(10);
			Found.setFire(10);
			}

		// Use up the parents' "in love" status, and make them not breed
		// again for the normal refractory period.
		self.resetInLove();
		self.setGrowingAge(6000);
		Found.resetInLove();
		Found.setGrowingAge(6000);

		// Play breeding sounds for both parents.  Normally we only play one, but since the parents
		// are different species, they'll have different sounds which both need representation.
		world.playSoundAtEntity(self, self.getDeathSound(), self.getSoundVolume(),
			(self.rand.nextFloat() - self.rand.nextFloat()) * 0.2F + 1.0F);
		world.playSoundAtEntity(Found, Found.getDeathSound(), Found.getSoundVolume(),
			(Found.rand.nextFloat() - Found.rand.nextFloat()) * 0.2F + 1.0F);

		// Add confusion effect to parents.  They'd have to be confused...
		self.addPotionEffect(new PotionEffect(Potion.confusion.id, 6000, 0));
		Found.addPotionEffect(new PotionEffect(Potion.confusion.id, 6000, 0));

		// Create placenta.
		world.playAuxSFX(2222, MathHelper.floor_double(Child.posX), MathHelper.floor_double(Child.posY),
			MathHelper.floor_double(Child.posZ), 0);

		return true;
		}
	}
