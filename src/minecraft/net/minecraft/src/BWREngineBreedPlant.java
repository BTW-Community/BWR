package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// Singleton that provides some utility functions for plant and fungus cross-
// breeding, allowing otherwise unavailable plants/fungi to be obtained in
// worlds where only a limited set are available, e.g. obtaining sugarcane
// on a Classic SuperFlat world.
public class BWREngineBreedPlant {
	public static BWREngineBreedPlant instance_ = null;

	// Tables of information about plant and fungus types that can be bred.
	private int[][] plantTypes;
	private int[][] fungusTypes;

	// Tables about evolutionary paths relating plant/fungi varieties.
	private List[] plantEvolve;
	private List[] fungusEvolve;

	// Singleton access method
	public static BWREngineBreedPlant getInstance() {
		if (instance_ == null)
			instance_ = new BWREngineBreedPlant();
		return instance_;
	}

	// Some miscelaneous helper functions for constructing
	// the flora evolutionary tree data.
	private static List[] createEvoTable(int[][] floraTypes) {
		List[] table = new List[floraTypes.length];
		for (int I = 0; I < floraTypes.length; I++) {
			table[I] = new ArrayList<int[]>();
			table[I].add(new int[] { I, 25 });
		}
		return table;
	}

	private static void addEvoCore(List parent, int child, int val) {
		for (Object entry : parent)
			if (((int[]) entry)[0] == child) {
				((int[]) entry)[1] += val;
				return;
			}
		parent.add(new int[] { child, val });
	}

	private static void addEvo(List[] table, int parent, int child) {
		addEvoCore(table[parent], child, 1);
		addEvoCore(table[child], parent, 5);
	}

	// Called once on add-on initialization.
	public void initialize() {
		// Define plant/fungus lookup tables, separated by kingdom.
		// - The first column is the blockID of the plant that would be created.
		// - The second is its metadata value.
		// - -1 means that any metadata matches this plant, but 0 should be
		// used when creating a new one.
		// - -2 means that any metadata matches this plant, and a random
		// value should be used when creating a new one.
		// - The first (index 0) entry is special; it's the one one that's
		// considered the "root" of the evolution tree, and gets an automatic
		// probability boost.
		plantTypes = new int[][] {
				// 0: Tall Grass
				new int[] { Block.tallGrass.blockID, 1 },

				// 1: Wheat
				new int[] { FCBetterThanWolves.fcBlockWheatCrop.blockID, -1 },
				// 2: Hemp
				new int[] { FCBetterThanWolves.fcBlockHempCrop.blockID, -1 },
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
				new int[] { Block.potato.blockID, -1 },
				// 18: Carrots
				new int[] { Block.carrot.blockID, -1 }, };
		fungusTypes = new int[][] {
				// 0: Brown Mushroom
				new int[] { Block.mushroomBrown.blockID, -1 },
				// 1: Red Mushroom
				new int[] { Block.mushroomRed.blockID, -1 },
				// 2: Nether Wart
				new int[] { Block.netherStalk.blockID, -1 }, };

		// Define evolutionary paths for plants.
		plantEvolve = createEvoTable(plantTypes);
		addEvo(plantEvolve, 0, 1);
		addEvo(plantEvolve, 0, 2);
		addEvo(plantEvolve, 0, 3);
		addEvo(plantEvolve, 2, 4);
		addEvo(plantEvolve, 4, 5);
		addEvo(plantEvolve, 2, 6);
		addEvo(plantEvolve, 6, 7);
		addEvo(plantEvolve, 3, 8);
		addEvo(plantEvolve, 8, 9);
		addEvo(plantEvolve, 9, 10);
		addEvo(plantEvolve, 10, 11);
		addEvo(plantEvolve, 11, 12);
		addEvo(plantEvolve, 3, 13);
		addEvo(plantEvolve, 13, 14);
		addEvo(plantEvolve, 14, 15);
		addEvo(plantEvolve, 15, 16);
		addEvo(plantEvolve, 1, 17);
		addEvo(plantEvolve, 17, 18);

		// Define evolutionary paths for fungi.
		fungusEvolve = createEvoTable(fungusTypes);
		addEvo(fungusEvolve, 0, 1);
		addEvo(fungusEvolve, 1, 2);
	}

	// Attempt to grow a plant/fungus in the specified space. Called by
	// blocks above which plants can cross-breed, i.e. planters, soulsand,
	// and fertilized farmland on world UpdateTick.
	public boolean grow(World world, int x, int y, int z) {
		// The space into which the plant is to grow must be air.
		if (world.getBlockId(x, y, z) > 0)
			return false;

		// Check for appropriate light levels. These pivot at the 8/9
		// boundary to be consistent with mob spawning logic and
		// ender specs, so an immersion-breaking light measuring method
		// is not required.
		int[][] blockTypes = null;
		List<int[]>[] evoTree = null;
		boolean isFungus = false;
		int light = world.getBlockLightValue(x, y, z);
		if (light >= 8) {
			blockTypes = plantTypes;
			evoTree = plantEvolve;
		} else {
			blockTypes = fungusTypes;
			evoTree = fungusEvolve;
			isFungus = true;
		}

		// Only small chance of continuing; this slows down cross-breeding,
		// making it more important to plan, and cuts down on the performance
		// cost cross-breed checks.
		if (world.rand.nextInt(480) != 0)
			return false;

		// Look at neighboring blocks and determine the probabilities of each type
		// of florum to grow based on its evolutionary neighbors.
		HashSet near = new HashSet();
		int[] probs = new int[blockTypes.length];
		probs[0] = 10;
		for (int dx = -1; dx <= 1; dx++)
			for (int dz = -1; dz <= 1; dz++) {
				int b = world.getBlockId(x + dx, y, z + dz);
				int m = -1;
				for (int i = 0; i < blockTypes.length; i++) {
					int[] def = blockTypes[i];
					if (def[0] != b)
						continue;
					if ((def[1] >= 0) && (m < 0))
						m = world.getBlockMetadata(x + dx, y, z + dz);
					if ((def[1] < 0) || (def[1] == m)) {
						near.add(i);
						for (int[] p : evoTree[i])
							probs[p[0]] += p[1];
						break;
					}
				}
			}

		// There must be at least 2 different types of neighboring plants
		// for the cross-breed to be allowed.
		if (near.size() < 2)
			return false;

		// Select a random type of plant/fungus to try to grow based
		// on the weighted probabilities determined.
		int max = 0;
		for (int p : probs)
			max += p;
		int pick = world.rand.nextInt(max) + 1;
		int[] sel = null;
		for (int i = 0; i < blockTypes.length; i++) {
			pick -= probs[i];
			if (pick <= 0) {
				sel = blockTypes[i];
				break;
			}
		}

		// Extract the block ID and metadata value from the definition.
		// If the metadata is to be random, choose one now.
		int createID = sel[0];
		int createMeta = sel[1];
		if (createMeta < -1)
			createMeta = world.rand.nextInt(16);
		if (createMeta < 0)
			createMeta = 0;

		// Count the number of different types of plant/fungus in adjacent
		// spaces. Note that this check is fast and sloppy, by block ID only,
		// so e.g. different types of saplings all count only once. There
		// must be at least 2 different species adjacent, and more species
		// increases the probability of breeding.

		// Attempt to create the block in its target location.
		world.setBlockAndMetadata(x, y, z, createID, createMeta);

		// Vines have special behavior; they do not inherit from BlockFlower like
		// most flora, and have different hooks for can-stay checks. Check if
		// the vine can stay by sending it a Block Update; it will destroy itself
		// with no drop if its location is unsuitable, and change its metadata
		// otherwise.
		if (createID == Block.vine.blockID) {
			Block.vine.onNeighborBlockChange(world, x, y, z, 0);
			return true;
		}

		// Check if the block can stay, i.e. the blocks surrounding the florum
		// are suitable for its survival.
		int belowID = world.getBlockId(x, y - 1, z);
		int newBelowID = belowID;
		boolean updateBelow = true;
		if (!Block.blocksList[createID].canBlockStay(world, x, y, z)) {
			// If the block below is farmland, try converting it back to dirt
			// to see if the block can stay on dirt.
			if ((belowID == FCBetterThanWolves.fcBlockFarmlandFertilized.blockID)
					|| (belowID == Block.tilledField.blockID)) {
				// Save the old blockID and metadata; if the dirt attempt
				// fails, we will revert to farmland.
				int belowMeta = world.getBlockMetadata(x, y - 1, z);

				// Remove the plant, so it doesn't pop off as an item when
				// changing the material below it causes a Block Update.
				world.setBlockAndMetadata(x, y, z, 0, 0);

				// Fungus will convert blocks below it to mycelium 2% of the time.
				// Tall grass and ferns will convert blocks below to grass. All
				// other blocks will change the farmland to dirt.
				if (isFungus && (world.rand.nextInt(50) == 0))
					newBelowID = Block.mycelium.blockID;
				else if (createID == Block.tallGrass.blockID)
					newBelowID = Block.grass.blockID;
				else
					newBelowID = Block.dirt.blockID;
				world.setBlockAndMetadata(x, y - 1, z, newBelowID, 0);

				// Replace the florum and check if it can stay on the new solid block.
				world.setBlockAndMetadata(x, y, z, createID, createMeta);
				if (!Block.blocksList[createID].canBlockStay(world, x, y, z)) {
					// If it still can't stay (e.g. lilypads, cocoa pods), then
					// revert all block changes and fail.
					world.setBlockAndMetadata(x, y, z, 0, 0);
					world.setBlockAndMetadata(x, y - 1, z, belowID, belowMeta);
					return false;
				}
			} else {
				// If the plant can't stay, but the block below is not farmland,
				// there is nothing else to try, so revert and fail.
				world.setBlockAndMetadata(x, y, z, 0, 0);
				return false;
			}
		} else if (belowID == FCBetterThanWolves.fcBlockFarmlandFertilized.blockID) {
			// If the florum was able to stay, remove fertilization from farmland
			// underneath. Additionally, fungus will convert the block below
			// into mycelium 2% of the time, and tall grass / ferns will convert
			// the farmland below into grass blocks.
			if (isFungus && (world.rand.nextInt(50) == 0))
				newBelowID = Block.mycelium.blockID;
			else if (createID == Block.tallGrass.blockID)
				newBelowID = Block.grass.blockID;
			else
				newBelowID = Block.tilledField.blockID;
			world.setBlockAndMetadata(x, y - 1, z, newBelowID, 0);
		} else if (belowID == FCBetterThanWolves.fcBlockPlanterSoil.blockID) {
			// If the florum was able to stay, remove fertilization from planters
			// underneath. Additionally, tall grass / ferns will convert the
			// soil inside the planter into grass.
			int belowMeta = world.getBlockMetadata(x, y - 1, z);
			if ((belowMeta & 2) != 0) {
				if (createID == Block.tallGrass.blockID)
					world.setBlockMetadata(x, y - 1, z, FCBlockPlanter.m_iTypeGrass0);
				else
					world.setBlockMetadata(x, y - 1, z, FCBlockPlanter.m_iTypeSoil);
			}
		} else
			updateBelow = false;

		// Trigger appropriate block update notifications, to ensure that Buddy
		// Block based builds function as expected.
		world.notifyBlockChange(x, y, z, createID);
		if (updateBelow)
			world.notifyBlockChange(x, y - 1, z, newBelowID);

		return true;
	}
}
