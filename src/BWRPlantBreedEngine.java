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

import java.util.HashSet;

// Singleton that provides some utility functions for plant and fungus cross-
// breeding, allowing otherwise unavailable plants/fungi to be obtained in
// worlds where only a limited set are available, e.g. obtaining sugarcane
// on a Classic SuperFlat world.
public class BWRPlantBreedEngine {
	public static BWRPlantBreedEngine m_instance = new BWRPlantBreedEngine();

	// Tables of information about plant and fungus types that can be bred.
	private static int[][] PlantTypes;
	private static int[][] FungusTypes;

	// Lookup tables to determine if a block is a plant/fungus, for cross-breeding
	// neighbor checks, based on blockID.  Note that metadata is not taken into
	// account for performance reasons.
	private static boolean[] PlantBlockIDs;
	private static boolean[] FungusBlockIDs;

	// Called by Initialize to convert PlantTypes/FungusTypes data to
	// PlantBlockIDs/FungusBlockIDs lookup tables.
	private boolean[] CreateBlockIDLookup(int[][] specs)
		{
		// Create a lookup table with a boolean for each
		// block ID and set to true those in the list.
		boolean[] IDs = new boolean[Block.blocksList.length];
		for(int I = 0; I < specs.length; I++)
			IDs[specs[I][0]] = true;
		return IDs;
		}

	public void Initialize()
		{
		// Define plant/fungus lookup tables, separated by kingdom.
		// The first column is the blockID of the plant that would be created.
		//  The second is its metadata value, with -1 meaning "choose a
		// random value."
		// The third is the probability weight, out of the total weights
		// for that kingdom.
		PlantTypes = new int[][]
			{
			new int[] { Block.tallGrass.blockID, 1, 100 },
			new int[] { Block.tallGrass.blockID, 2, 5 },
			new int[] { Block.plantYellow.blockID, -1, 20 },
			new int[] { Block.plantRed.blockID, 0, 10 },
			new int[] { Block.sapling.blockID, 0, 5 },
			new int[] { Block.sapling.blockID, 1, 2 },
			new int[] { Block.sapling.blockID, 2, 2 },
			new int[] { Block.sapling.blockID, 3, 1 },
			new int[] { Block.reed.blockID, 0, 3 },
			new int[] { Block.cactus.blockID, 0, 2 },
			new int[] { mod_FCBetterThanWolves.fcHempCrop.blockID, 0, 5 },
			new int[] { Block.melonStem.blockID, 0, 2 },
			new int[] { Block.pumpkinStem.blockID, 0, 2 },
			new int[] { Block.crops.blockID, 0, 10 },
			new int[] { Block.vine.blockID, -1, 1 },
			new int[] { Block.waterlily.blockID, 0, 1 },
			new int[] { Block.cocoaPlant.blockID, -1, 1 },
			// new int[] { Block.carrot.blockID, 0, 10 },
			// new int[] { Block.potato.blockID, 0, 10 },
			};
		FungusTypes = new int[][]
			{
			new int[] { Block.mushroomBrown.blockID, 0, 1 },
			new int[] { Block.mushroomRed.blockID, 0, 1 },
			new int[] { Block.netherStalk.blockID, 0, 1 },
			};

		// Create is-plant/fungus lookup tables.
		PlantBlockIDs = CreateBlockIDLookup(PlantTypes);
		FungusBlockIDs = CreateBlockIDLookup(FungusTypes);
		}

	// Attempt to grow flora of the specified type, plant or fungus.  The determination
	// of plant/fungus is already made by the caller, and the appropriate type definitions
	// are passed in.  Called by GrowPlant and GrowFungus.
	private boolean Grow(World world, int x, int y, int z, int[][] blockTypes, boolean[] ids)
		{
		// Select a random flora definition based on probability weights.
		int Max = 0;
		for(int[] T : blockTypes)
			Max += T[2];
		int Pick = world.rand.nextInt(Max);
		Max = 0;
		int[] Sel = null;
		for(int[] T : blockTypes)
			{
			Sel = T;
			Max += T[2];
			if(Max > Pick)
				break;
			}

		// Extract the block ID and metadata value from the definition.
		// If the metadata is to be random, choose one now.
		int CreateID = Sel[0];
		int CreateMeta = Sel[1];
		if(CreateMeta < 0)
			CreateMeta = world.rand.nextInt(16);

		// Count the number of different types of plant/fungus in adjacent
		// spaces.  Note that this check is fast and sloppy, by block ID only,
		// so e.g. different types of saplings all count only once.  There
		// must be at least 2 different species adjacent, and more species
		// increases the probability of breeding.
		HashSet NearIDs = new HashSet();
		for(int dx = -1; dx <= 1; dx++)
			for(int dz = -1; dz <= 1; dz++)
				{
				int B = world.getBlockId(x + dx, y, z + dz);
				if((B != CreateID) && ids[B])
					NearIDs.add(B);
				}
		int Neighbors = NearIDs.size();
		if((Neighbors < 2) || (world.rand.nextInt(12) < Neighbors))
			return false;

		// Attempt to create the block in its target location.
		world.setBlockAndMetadata(x, y, z, CreateID, CreateMeta);

		// Vines have special behavior; they do not inherit from BlockFlower like
		// most flora, and have different hooks for can-stay checks.  Check if
		// the vine can stay by sending it a Block Update; it will destroy itself
		// with no drop if its location is unsuitable, and change its metadata
		// otherwise.
		if(CreateID == Block.vine.blockID)
			{
			Block.vine.onNeighborBlockChange(world, x, y, z, 0);
			return true;
			}

		// Check if the block can stay, i.e. the blocks surrounding the florum
		// are suitable for its survival.
		int BelowID = world.getBlockId(x, y - 1, z);
		if(!Block.blocksList[CreateID].canBlockStay(world, x, y, z))
			{
			// If the block below is farmland, try converting it back to dirt
			// to see if the block can stay on dirt.
			if((BelowID == mod_FCBetterThanWolves.fcBlockFarmlandFertilized.blockID)
				|| (BelowID == Block.tilledField.blockID))
				{
				// Save the old blockID and metadata; if the dirt attempt
				// fails, we will revert to farmland.
				int BelowMeta = world.getBlockMetadata(x, y - 1, z);

				// Remove the plant, so it doesn't pop off as an item when
				// changing the material below it causes a Block Update.
				world.setBlockAndMetadata(x, y, z, 0, 0);

				// Fungus will convert blocks below it to mycelium 2% of the time.
				// Tall grass and ferns will convert blocks below to grass.  All
				// other blocks will change the farmland to dirt.
				if(FungusBlockIDs[CreateID] && (world.rand.nextInt(50) == 0))
					world.setBlockWithNotify(x, y - 1, z, Block.mycelium.blockID);
				else if(CreateID == Block.tallGrass.blockID)
					world.setBlockAndMetadata(x, y - 1, z, Block.grass.blockID, 0);
				else
					world.setBlockAndMetadata(x, y - 1, z, Block.dirt.blockID, 0);

				// Replace the florum and check if it can stay on the new solid block.
				world.setBlockAndMetadata(x, y, z, CreateID, CreateMeta);
				if(!Block.blocksList[CreateID].canBlockStay(world, x, y, z))
					{
					// If it still can't stay (e.g. lilypads, cocoa pods), then
					// revert all block changes and fail.
					world.setBlockAndMetadata(x, y, z, 0, 0);
					world.setBlockAndMetadata(x, y - 1, z, BelowID, BelowMeta);
					return false;
					}
				}
			else
				{
				// If the plant can't stay, but the block below is not farmland,
				// there is nothing else to try, so revert and fail.
				world.setBlockAndMetadata(x, y, z, 0, 0);
				return false;
				}
			}
		else if(BelowID == mod_FCBetterThanWolves.fcBlockFarmlandFertilized.blockID)
			{
			// If the florum was able to stay, remove fertilization from farmland
			// underneath.  Additionally, fungus will convert the block below
			// into mycelium 2% of the time, and tall grass / ferns will convert
			// the farmland below into grass blocks.
			if(FungusBlockIDs[CreateID] && (world.rand.nextInt(50) == 0))
				world.setBlockWithNotify(x, y - 1, z, Block.mycelium.blockID);
			else if(CreateID == Block.tallGrass.blockID)
				world.setBlockWithNotify(x, y - 1, z, Block.grass.blockID);
			else
				world.setBlockWithNotify(x, y - 1, z, Block.tilledField.blockID);
			}
		else if(BelowID ==  mod_FCBetterThanWolves.fcPlanter.blockID)
			{
			// If the florum was able to stay, remove fertilization from planters
			// underneath.  Additionally, tall grass / ferns will convert the
			// soil inside the planter into grass.
			int BelowMeta = world.getBlockMetadata(x, y - 1, z);
			if(BelowMeta == FCBlockPlanter.m_iTypeSoilFertilized)
				{
				if(CreateID == Block.tallGrass.blockID)
					world.setBlockMetadata(x, y - 1, z, FCBlockPlanter.m_iTypeGrass0);
				else
					world.setBlockMetadata(x, y - 1, z, FCBlockPlanter.m_iTypeSoil);
				}
			}

		return true;
		}

	// Attempt to grow a plant into this space; called on UpdateTick
	// by various blocks onto which plants can cross-breed.
	public boolean GrowPlant(World world, int x, int y, int z)
		{
		// The space into which the plant is to grow must be air.
		if(world.getBlockId(x, y, z) > 0)
			return false;

		// Very high immediate light levels are required for plant
		// growth; view of sky does not affect this, and the requirement
		// is stricter than most plants require to survive.
		if(world.getFullBlockLightValue(x, y, z) < 14)
			return false;

		// High probability of failure; this reduces the speed
		// of flora cross-breeding.
		if(world.rand.nextInt(100) != 0)
			return false;

		// Pick a plant type and attempt to grow it.
		return Grow(world, x, y, z, PlantTypes, PlantBlockIDs);
		}

	// Attempt to grow a fungus into this space; called on UpdateTick
	// by various blocks onto which fungi can cross-breed.
	public boolean GrowFungus(World world, int x, int y, int z)
		{
		// The space into which the plant is to grow must be air.
		if(world.getBlockId(x, y, z) > 0)
			return false;

		// Very low light levels are required for fungus growth.
		// This is stricter than the requirements for survival would
		// be once the fungus is grown.
		if(world.getFullBlockLightValue(x, y, z) > 1)
			return false;

		// High probability of failure; this reduces the speed
		// of flora cross-breeding.
		if(world.rand.nextInt(100) != 0)
			return false;

		// Pick a fungus type and attempt to grow it.
		return Grow(world, x, y, z, FungusTypes, FungusBlockIDs);
		}
	}
