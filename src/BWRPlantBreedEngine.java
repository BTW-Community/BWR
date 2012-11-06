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

// Singleton that provides some utility functions for plant and fungus cross-
// breeding, allowing otherwise unavailable plants/fungi to be obtained in
// worlds where only a limited set are available, e.g. obtaining sugarcane
// on a Classic SuperFlat world.
public class BWRPlantBreedEngine {
	public static BWRPlantBreedEngine m_instance = new BWRPlantBreedEngine();

	// Tables of information about plant and fungus types that can be bred.
	private int[][] PlantTypes;
	private int[][] FungusTypes;

	// Tables about evolutionary paths relating plant/fungi varieties.
	private List[] PlantEvolve;
	private List[] FungusEvolve;

	// Some miscelaneous helper functions for constructing
	// the flora evolutionary tree data.
	private static List[] CreateEvoTable(int[][] floraTypes)
		{
		List[] Table = new List[floraTypes.length];
		for(int I = 0; I < floraTypes.length; I++)
			{
			Table[I] = new ArrayList<int[]>();
			Table[I].add(new int[] { I, 25 });
			}
		return Table;
		}
	private static void AddEvoCore(List parent, int child, int val)
		{
		for(Object Entry : parent)
			if(((int[])Entry)[0] == child)
				{
				((int[])Entry)[1] += val;
				return;
				}
		parent.add(new int[] { child, val });
		}
	private static void AddEvo(List[] table, int parent, int child)
		{
		AddEvoCore(table[parent], child, 1);
		AddEvoCore(table[child], parent, 5);
		}

	// Called once on add-on initialization.
	public void Initialize()
		{
		// Define plant/fungus lookup tables, separated by kingdom.
		// - The first column is the blockID of the plant that would be created.
		// - The second is its metadata value.
		//   - -1 means that any metadata matches this plant, but 0 should be
		//     used when creating a new one.
		//   - -2 means that any metadata matches this plant, and a random
		//     value should be used when creating a new one.
		// - The first (index 0) entry is special; it's the one one that's
		//   considered the "root" of the evolution tree, and gets an automatic
		//   probability boost.
		PlantTypes = new int[][]
			{
			// 0: Tall Grass
			new int[] { Block.tallGrass.blockID, 1 },

			// 1: Wheat
			new int[] { Block.crops.blockID, -1 },
			// 2: Hemp
			new int[] { mod_FCBetterThanWolves.fcHempCrop.blockID, -1 },
			// 3: Dandelion
			new int[] { Block.plantYellow.blockID, -1 },

			// 4: Pumpkin
			new int[] { Block.pumpkinStem.blockID, -1 },
			// 5: Melon
			new int[] { Block.melonStem.blockID, -1 },

			// 6: Sugarcane
			new int[] { Block.reed.blockID, -1 },
			// 7: Cactus
			new int[] { Block.cactus.blockID, -1 },

			// 8: Rose
			new int[] { Block.plantRed.blockID, -1 },
			// 9: Fern
			new int[] { Block.tallGrass.blockID, 2 },
			// 10: Lilypad
			new int[] { Block.waterlily.blockID, -1 },
			// 11: Vines
			new int[] { Block.vine.blockID, -2 },
			// 12: Cocoa
			new int[] { Block.cocoaPlant.blockID, -2 },

			// 13: Oak
			new int[] { Block.sapling.blockID, 0 },
			// 14: Birch
			new int[] { Block.sapling.blockID, 2 },
			// 15: Spruce
			new int[] { Block.sapling.blockID, 1 },
			// 16: Jungle
			new int[] { Block.sapling.blockID, 3 },

			// 17: Potatoes
			new int[] { Block.field_82514_ch.blockID, -1 },
			// 18: Carrots
			new int[] { Block.field_82513_cg.blockID, -1 },
			};
		FungusTypes = new int[][]
			{
			// 0: Brown Mushroom
			new int[] { Block.mushroomBrown.blockID, -1 },
			// 1: Red Mushroom
			new int[] { Block.mushroomRed.blockID, -1 },
			// 2: Nether Wart
			new int[] { Block.netherStalk.blockID, -1 },
			};

		// Define evolutionary paths for plants.
		PlantEvolve = CreateEvoTable(PlantTypes);
		AddEvo(PlantEvolve, 0, 1);
		AddEvo(PlantEvolve, 0, 2);
		AddEvo(PlantEvolve, 0, 3);
		AddEvo(PlantEvolve, 2, 4);
		AddEvo(PlantEvolve, 4, 5);
		AddEvo(PlantEvolve, 2, 6);
		AddEvo(PlantEvolve, 6, 7);
		AddEvo(PlantEvolve, 3, 8);
		AddEvo(PlantEvolve, 8, 9);
		AddEvo(PlantEvolve, 9, 10);
		AddEvo(PlantEvolve, 10, 11);
		AddEvo(PlantEvolve, 11, 12);
		AddEvo(PlantEvolve, 3, 13);
		AddEvo(PlantEvolve, 13, 14);
		AddEvo(PlantEvolve, 14, 15);
		AddEvo(PlantEvolve, 15, 16);
		AddEvo(PlantEvolve, 1, 17);
		AddEvo(PlantEvolve, 17, 18);

		// Define evolutionary paths for fungi.
		FungusEvolve = CreateEvoTable(FungusTypes);
		AddEvo(FungusEvolve, 0, 1);
		AddEvo(FungusEvolve, 1, 2);
		}

	// Attempt to grow a plant/fungus in the specified space.  Called by
	// blocks above which plants can cross-breed, i.e. planters, soulsand,
	// and fertilized farmland on world UpdateTick.
	public boolean Grow(World world, int x, int y, int z)
		{
		// The space into which the plant is to grow must be air.
		if(world.getBlockId(x, y, z) > 0)
			return false;

		// Plants need very high immediate light levels.  Fungus needs
		// very low immediate light levels.  These will be stricter than
		// the requirements necessary for normal survival, and will
		// determine whether we try to grow plants or fungi.
		int[][] BlockTypes = null;
		List<int[]>[] EvoTree = null;
		boolean IsFungus = false;
		int Light = world.getBlockLightValue(x, y, z);
		if(Light >= 14)
			{
			BlockTypes = PlantTypes;
			EvoTree = PlantEvolve;
			}
		else if(Light <= 1)
			{
			BlockTypes = FungusTypes;
			EvoTree = FungusEvolve;
			IsFungus = true;
			}
		else
			return false;

		// Only small chance of continuing; this slows down cross-breeding,
		// making it more important to plan, and cuts down on the performance
		// cost cross-breed checks.
		if(world.rand.nextInt(100) != 0)
			return false;

		// Look at neighboring blocks and determine the probabilities of each type
		// of florum to grow based on its evolutionary neighbors.
		HashSet Near = new HashSet();
		int[] Probs = new int[BlockTypes.length];
		Probs[0] = 10;
		for(int dx = -1; dx <= 1; dx++)
			for(int dz = -1; dz <= 1; dz++)
				{
				int B = world.getBlockId(x + dx, y, z + dz);
				int M = -1;
				for(int I = 0; I < BlockTypes.length; I++)
					{
					int[] Def = BlockTypes[I];
					if(Def[0] != B)
						continue;
					if((Def[1] >= 0) && (M < 0))
						M = world.getBlockMetadata(x + dx, y, z + dz);
					if((Def[1] < 0) || (Def[1] == M))
						{
						Near.add(I);
						for(int[] P : EvoTree[I])
							Probs[P[0]] += P[1];
						break;
						}
					}
				}

		// There must be at least 2 different types of neighboring plants
		// for the cross-breed to be allowed.
		if(Near.size() < 2)
			return false;

		// Select a random type of plant/fungus to try to grow based
		// on the weighted probabilities determined.
		int Max = 0;
		for(int P : Probs)
			Max += P;
		int Pick = world.rand.nextInt(Max) + 1;
		int[] Sel = null;
		for(int I = 0; I < BlockTypes.length; I++)
			{
			Pick -= Probs[I];
			if(Pick <= 0)
				{
				Sel = BlockTypes[I];
				break;
				}
			}

		// If in development, log cross-breeding probability profile.
		if(mod_BetterWithRenewables.bwrDevVersion)
			{
			String PDebug = "Plant Cross-Breeding Weights:";
			for(int I = 0; I < BlockTypes.length; I++)
				if(Probs[I] > 0)
					PDebug += " " + Block.blocksList[BlockTypes[I][0]].getBlockName()
						+ "[" + BlockTypes[I][1] + "]:" + Probs[I];
			PDebug += " TOTAL:" + Max;
			mod_BetterWithRenewables.m_instance.Log(PDebug);
			}

		// Extract the block ID and metadata value from the definition.
		// If the metadata is to be random, choose one now.
		int CreateID = Sel[0];
		int CreateMeta = Sel[1];
		if(CreateMeta < -1)
			CreateMeta = world.rand.nextInt(16);
		if(CreateMeta < 0)
			CreateMeta = 0;

		// Count the number of different types of plant/fungus in adjacent
		// spaces.  Note that this check is fast and sloppy, by block ID only,
		// so e.g. different types of saplings all count only once.  There
		// must be at least 2 different species adjacent, and more species
		// increases the probability of breeding.

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
		int NewBelowID = BelowID;
		boolean UpdateBelow = true;
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
				if(IsFungus && (world.rand.nextInt(50) == 0))
					NewBelowID = Block.mycelium.blockID;
				else if(CreateID == Block.tallGrass.blockID)
					NewBelowID = Block.grass.blockID;
				else
					NewBelowID = Block.dirt.blockID;
				world.setBlockAndMetadata(x, y - 1, z, NewBelowID, 0);

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
			if(IsFungus && (world.rand.nextInt(50) == 0))
				NewBelowID = Block.mycelium.blockID;
			else if(CreateID == Block.tallGrass.blockID)
				NewBelowID = Block.grass.blockID;
			else
				NewBelowID = Block.tilledField.blockID;
			world.setBlockAndMetadata(x, y - 1, z, NewBelowID, 0);
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
		else
			UpdateBelow = false;

		// Trigger appropriate block update notifications, to ensure that Buddy
		// Block based builds function as expected.
		world.notifyBlockChange(x, y, z, CreateID);
		if(UpdateBelow)
			world.notifyBlockChange(x, y - 1, z, NewBelowID);

		return true;
		}
	}
