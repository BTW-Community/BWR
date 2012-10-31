package net.minecraft.src;
import java.util.List;
import java.util.HashSet;
import net.minecraft.server.MinecraftServer;

public class BWRPlantBreedEngine {
	public static BWRPlantBreedEngine m_instance = new BWRPlantBreedEngine();

	private static int[][] PlantTypes;
	private static int[][] FungusTypes;
	private static boolean[] PlantBlockIDs;
	private static boolean[] FungusBlockIDs;

	private boolean[] CreateBlockIDLookup(int[][] specs)
		{
		boolean[] IDs = new boolean[Block.blocksList.length];
		for(int I = 0; I < specs.length; I++)
			IDs[specs[I][0]] = true;
		return IDs;
		}

	public void Initialize()
		{
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

		PlantBlockIDs = CreateBlockIDLookup(PlantTypes);
		FungusBlockIDs = CreateBlockIDLookup(FungusTypes);
		}

	private boolean Grow(World world, int x, int y, int z, int[][] blockTypes, boolean[] ids)
		{
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
		int CreateID = Sel[0];
		int CreateMeta = Sel[1];
		if(CreateMeta < 0)
			CreateMeta = world.rand.nextInt(16);

		HashSet NearIDs = new HashSet();
		for(int dx = -1; dx <= 1; dx++)
			for(int dy = -1; dy <= 1; dy++)
				for(int dz = -1; dz <= 1; dz++)
					{
					int B = world.getBlockId(x + dx, y + dy, z + dz);
					if((B == CreateID) && (world.rand.nextInt(20) != 0))
						return false;
					if(ids[B])
						NearIDs.add(B);
					}
		if(NearIDs.size() < 2)
			return false;

		world.setBlockAndMetadata(x, y, z, CreateID, CreateMeta);
		int BelowID = world.getBlockId(x, y - 1, z);
		if(CreateID == Block.vine.blockID)
			{
			Block.vine.onNeighborBlockChange(world, x, y, z, 0);
			return true;
			}
		if(!Block.blocksList[CreateID].canBlockStay(world, x, y, z))
			{
			if((BelowID == mod_FCBetterThanWolves.fcBlockFarmlandFertilized.blockID)
				|| (BelowID == Block.tilledField.blockID))
				{
				int BelowMeta = world.getBlockMetadata(x, y - 1, z);
				world.setBlockAndMetadata(x, y, z, 0, 0);
				if(FungusBlockIDs[CreateID]  && (world.rand.nextInt(50) == 0))
					world.setBlockWithNotify(x, y - 1, z, Block.mycelium.blockID);
				else if(CreateID == Block.tallGrass.blockID)
					world.setBlockAndMetadata(x, y - 1, z, Block.grass.blockID, 0);
				else
					world.setBlockAndMetadata(x, y - 1, z, Block.dirt.blockID, 0);
				world.setBlockAndMetadata(x, y, z, CreateID, CreateMeta);
				if(!Block.blocksList[CreateID].canBlockStay(world, x, y, z))
					{
					world.setBlockAndMetadata(x, y - 1, z, BelowID, BelowMeta);
					world.setBlockAndMetadata(x, y, z, 0, 0);
					return false;
					}
				}
			else
				{
				world.setBlockAndMetadata(x, y, z, 0, 0);
				return false;
				}
			}
		else if(BelowID == mod_FCBetterThanWolves.fcBlockFarmlandFertilized.blockID)
			{
			if(FungusBlockIDs[CreateID] && (world.rand.nextInt(50) == 0))
				world.setBlockWithNotify(x, y - 1, z, Block.mycelium.blockID);
			else if(CreateID == Block.tallGrass.blockID)
				world.setBlockWithNotify(x, y - 1, z, Block.grass.blockID);
			else
				world.setBlockWithNotify(x, y - 1, z, Block.tilledField.blockID);
			}
		else if(BelowID ==  mod_FCBetterThanWolves.fcPlanter.blockID)
			{
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

	public boolean GrowPlant(World world, int x, int y, int z)
		{
		if(world.getBlockId(x, y, z) > 0)
			return false;
		if(world.getFullBlockLightValue(x, y, z) < 14)
			return false;
		if(world.rand.nextInt(1200) != 0)
			return false;
		return Grow(world, x, y, z, PlantTypes, PlantBlockIDs);
		}

	public boolean GrowFungus(World world, int x, int y, int z)
		{
		if(world.getBlockId(x, y, z) > 0)
			return false;
		if(world.getFullBlockLightValue(x, y, z) > 1)
			return false;
		if(world.rand.nextInt(1200) != 0)
			return false;
		return Grow(world, x, y, z, FungusTypes, FungusBlockIDs);
		}
	}
