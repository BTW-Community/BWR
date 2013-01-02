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
public class BWREngineBreedAnimal {
	public static BWREngineBreedAnimal instance_ = null;

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

	// Get singleton instance.
	public static BWREngineBreedAnimal getInstance()
		{
		if(instance_ == null)
			instance_ = new BWREngineBreedAnimal();
		return instance_;
		}

	// One-time initialization of the engine, called by the mod startup.
	public void initialize()
		{
		// Stub.  Normally I'd initialize some fast lookup tables or
		// something here, but the engine doesn't currently use any of that.
		}

	// Routines for recursively scanning the prospective habitat of a new creature,
	// using a 3D flood-fill algorithm like block lighting.  It adds up the quantities
	// of all blocks found by ID/metadata.
	private static final int MAX_SCAN_DEPTH = 10;
	private void scanHabitat(World world, int x, int y, int z, int[] results,
		Map<String, Integer> seen, int depth)
		{
		// Check to see if the block has been visited before.
		String key = x + "," + y + "," + z;
		Integer prev = seen.get(key);

		// If the block has not been visited, analyze it.
		if(prev == null)
			{
			// Get the type of block in this space.
			int id = world.getBlockId(x, y, z);

			// For wood and leaves, pay attention to the metadata, for others
			// just treat it as 0 and lump together all blocks with the same ID.
			int meta = 0;
			if((id == Block.wood.blockID) || (id == Block.leaves.blockID))
				meta = world.getBlockMetadata(x, y, z) & 3;

			// Increment block count.
			results[(id * 16) + meta]++;

			// If the block is passable, record the depth at which we scanned it.
			// If not, mark it so future passes will know not to continue.
			if((id == 0) || !Block.blocksList[id].blockMaterial.isSolid())
				seen.put(key, depth);
			else
				seen.put(key, prev = MAX_SCAN_DEPTH);
			}

		// Do not scan further if we've reached the depth limit.
		if(depth <= 0)
			return;

		// If this block has not been scanned, or if a previous scan was done
		// at a lower depth, then recurse to neighboring blocks.
		if((prev == null) || (prev < depth))
			{
			scanHabitat(world, x - 1, y, z, results, seen, depth - 1);
			scanHabitat(world, x + 1, y, z, results, seen, depth - 1);
			scanHabitat(world, x, y - 1, z, results, seen, depth - 1);
			scanHabitat(world, x, y + 1, z, results, seen, depth - 1);
			scanHabitat(world, x, y, z - 1, results, seen, depth - 1);
			scanHabitat(world, x, y, z + 1, results, seen, depth - 1);
			}
		}
	private int[] scanHabitat(World world, int x, int y, int z)
		{
		// Create the results array, seen hashset, and default depth,
		// and start the scan with default settings.
		int[] results = new int[Block.blocksList.length * 16];
		scanHabitat(world, x, y, z, results, new HashMap<String, Integer>(), MAX_SCAN_DEPTH);
		return results;
		}

	// Helper function to add probability to a map of weighted probabilties.
	private void addProb(Map<Integer, Integer> map, int key, int val)
		{
		Integer i = map.get(new Integer(key));
		map.put(key, (i != null) ? (i + val) : new Integer(val));
		}

	// Do all cross-breeding checks.  Called by EntityUpdate on all animal
	// entities that are capable of cross-breeding.
	public boolean tryBreed(EntityAnimal self)
		{
		// ########## STEP 1: DETERMINE IF CROSS-BREEDING HAPPENS

		// Animal trying to cross-breed must be in love, and reaching the
		// end of the "in love" period (i.e. "desperate").
		if(!self.isInLove() || (self.getInLove() > 20))
			return false;

		// Cross-breeding only happens in low-light conditions.  Maybe the animals
		// are embarrassed about it, or maybe they wouldn't do it if they could see...
		int bx = MathHelper.floor_double(self.posX);
		int by = MathHelper.floor_double(self.posY);
		int bz = MathHelper.floor_double(self.posZ);
		World world = self.worldObj;
		if(world.getBlockLightValue(bx, by, bz) >= 8)
			return false;

		// Search all possible candidates for mating or cross-breeding.
		List mates = world.getEntitiesWithinAABB(EntityAnimal.class, self.boundingBox.expand(8F, 8F, 8F));
		EntityAnimal found = null;
		if((mates != null) && (mates.size() > 0))
			for(int i = 0; i < mates.size(); i++)
				{
				EntityAnimal ent = (EntityAnimal)mates.get(i);
				if(ent == self)
					continue;

				// If there are any nearby animals of same/related species,
				// this animal will not cross-breed.  Note that if you have
				// many of species A and only one of B, A will not mate with
				// B, but B will still mate with A (it just takes longer).
				if(ent.getClass().isAssignableFrom(self.getClass())
					|| self.getClass().isAssignableFrom(ent.getClass()))
					return false;

				// To cross-breed, partner must be in love.
				if(!ent.isInLove())
					continue;

				// To cross-breed, animals must be in close proximity.  AI is
				// not modified, so they will not move together voluntarily,
				// i.e. moving them together is left as an exercise for the
				// player.
				double dx = ent.posX - self.posX;
				double dy = ent.posY - self.posY;
				double dz = ent.posZ - self.posZ;
				double dr = (dx * dx) + (dy * dy) + (dz * dz);
				if(dr > (3.5D * 3.5D))
					continue;

				// Tamable animals must be tamed and not sitting to cross-breed.
				if(ent instanceof EntityTameable)
					{
					EntityTameable tame = (EntityTameable)ent;
					if(!tame.isTamed() || tame.isSitting())
						continue;
					}

				// Cross-breeding partner must also be in low light conditions.
				if(world.getBlockLightValue(MathHelper.floor_double(ent.posX),
					MathHelper.floor_double(ent.posY), MathHelper.floor_double(ent.posZ)) > 4)
					continue;

				found = ent;
				break;
				}
		if(found == null)
			return false;

		// ########## STEP 2: DETERMINE CHILD SPECIES

		// Setup a table of probabilities for each creature type.
		Map<Integer, Integer> weights = new HashMap<Integer, Integer>();

		// Standard weights: animals that bear live offspring have
		// a default base probability, while chickens, which reproduce
		// via egg-laying, are a less likely mutation.
		addProb(weights, eidCow, 100);
		addProb(weights, eidPig, 100);
		addProb(weights, eidSheep, 100);
		addProb(weights, eidChicken, 50);

		// Add high probability that parents' genetic contribution
		// is completely dominant and no mutation occurs.
		addProb(weights, EntityList.getEntityID(self), 100);
		addProb(weights, EntityList.getEntityID(found), 100);

		// Add base probabilties of certain abominations.
		addProb(weights, eidSilverfish, 200);
		addProb(weights, eidCaveSpider, 100);
		addProb(weights, eidSlime, 50);

		// Calculate child's location, and measure environment there.
		double cx = (self.posX + found.posX) / 2.0D;
		double cy = (self.posY + found.posY) / 2.0D;
		double cz = (self.posZ + found.posZ) / 2.0D;
		int cbx = MathHelper.floor_double(cx);
		int cby = MathHelper.floor_double(cy);
		int cbz = MathHelper.floor_double(cz);
		int[] habitat = scanHabitat(world, cbx, cby, cbz);

		// Choose a random child species based on weighted probabilities
		// If the tile into which the child would spawn is water, then
		// a squid is possible, and encouraged by nearby water.
		if(world.getBlockMaterial(cbx, cby, cbz) == Material.water)
			addProb(weights, eidSquid, habitat[Block.waterMoving.blockID * 16]
				+ habitat[Block.waterStill.blockID * 16]);

		// Add probabilities for wolves based on ratios of pine
		// logs, leaves, and snow to simuate a tundra biome.
		int pinelogs = habitat[Block.wood.blockID * 16 + 1] * 20;
		int pineleaves = habitat[Block.leaves.blockID * 16 + 1];
		int pine = (pinelogs > pineleaves) ? pineleaves : pinelogs;
		pine /= 2;
		pine = (pine > 100) ? 100 : pine;
		addProb(weights, eidWolf, pine);
		int snow = habitat[Block.snow.blockID] + habitat[Block.blockSnow.blockID] * 2;
		snow = (snow > pine) ? pine : snow;
		addProb(weights, eidWolf, snow / 10);

		// Ocelots are encouraged by jungle wood and leaves, vines,
		// and cocoa pods.
		int junglogs = habitat[Block.wood.blockID * 16 + 3] * 5;
		int jungleaves = habitat[Block.leaves.blockID * 16 + 3];
		int jung = (junglogs > jungleaves) ? jungleaves : junglogs;
		jung /= 2;
		jung = (jung > 100) ? 100 : jung;
		addProb(weights, eidOcelot, jung);
		int vines = habitat[Block.vine.blockID] * 2;
		vines = (vines > jung) ? jung : vines;
		addProb(weights, eidOcelot, vines);
		int cocoa = habitat[Block.cocoaPlant.blockID] * 3;
		cocoa = (cocoa > vines) ? vines : cocoa;
		addProb(weights, eidOcelot, cocoa);

		// Villagers will rarely spawn if surrounded by blocks they desire.
		addProb(weights, eidVillager, (habitat[Block.blockEmerald.blockID] / 100)
			+ (habitat[Block.blockDiamond.blockID] / 30));

		// And create a new instance.
		EntityLiving child = null;
		int max = 0;
		for(Map.Entry<Integer, Integer> p : weights.entrySet())
			max += p.getValue();
		int pick = self.rand.nextInt(max);
		for(Map.Entry<Integer, Integer> p : weights.entrySet())
			{
			pick -= p.getValue();
			if(pick <= 0)
				{
				child = (EntityLiving)EntityList.createEntityByID(
					p.getKey().intValue(), world);
				break;
				}
			}

		// Choose a random child species based on weighted probabilities
		// ########## STEP 3: COMPLETE BREEDING PROCESs

		// Final setup of child parameters, and place child in the world.
		if(child instanceof EntitySlime)
			((EntitySlime)child).setSlimeSize(1);
		if(child instanceof EntityAgeable)
			((EntityAgeable)child).setGrowingAge(-24000);
		child.setAttackTarget(self.rand.nextInt(2) == 0 ? self : found);
		child.setLocationAndAngles((self.posX + found.posX) / 2.0D, (self.posY + found.posY) / 2.0D,
			(self.posZ + found.posZ) / 2.0D, self.rotationYaw, self.rotationPitch);
		world.spawnEntityInWorld(child);

		// Use up the parents' "in love" status, and make them not breed
		// again for the normal refractory period.
		self.resetInLove();
		self.setGrowingAge(6000);
		found.resetInLove();
		found.setGrowingAge(6000);

		// Play breeding sounds for both parents.  Normally we only play one, but since the parents
		// are different species, they'll have different sounds which both need representation.
		world.playSoundAtEntity(self, self.getDeathSound(), self.getSoundVolume(),
			(self.rand.nextFloat() - self.rand.nextFloat()) * 0.2F + 1.0F);
		world.playSoundAtEntity(found, found.getDeathSound(), found.getSoundVolume(),
			(found.rand.nextFloat() - found.rand.nextFloat()) * 0.2F + 1.0F);

		// Add confusion effect to parents.  They'd have to be confused...
		self.addPotionEffect(new PotionEffect(Potion.confusion.id, 6000, 0));
		found.addPotionEffect(new PotionEffect(Potion.confusion.id, 6000, 0));

		// Create placenta.
		world.playAuxSFX(2222, MathHelper.floor_double(child.posX), MathHelper.floor_double(child.posY),
			MathHelper.floor_double(child.posZ), 0);

		return true;
		}
	}
