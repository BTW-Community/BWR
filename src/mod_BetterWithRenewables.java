package net.minecraft.src;
import net.minecraft.server.MinecraftServer;

public class mod_BetterWithRenewables {
	public static final String bwrProductString = "Better With Renewables";
	public static final String bwrAbbrString = "BWR";
	public static final String bwrVersionString = "0.13.0430";
	public static boolean HasInitialized = false;
	public static mod_BetterWithRenewables m_instance = new mod_BetterWithRenewables();

	public String getVersion()
		{
		return bwrVersionString;
		}

	public void DiamondRecoveryRecipe(Item tool, int qty)
		{
		FCRecipes.AddStokedCauldronRecipe(
			new ItemStack(Item.diamond, qty),
			new ItemStack[]
				{
				new ItemStack(tool, 1, -1),
				new ItemStack(mod_FCBetterThanWolves.fcPotash, qty * 8, -1),
				new ItemStack(mod_FCBetterThanWolves.fcConcentratedHellfire, qty, -1),
				});
		}

	public void LapisRecoveryRecipe(int indmg, int outdmg1, int outdmg2, int qty)
		{
		FCRecipes.AddStokedCauldronRecipe(
			new ItemStack[]
				{
				new ItemStack(Item.dyePowder, 1, 4),
				new ItemStack(Block.cloth, 4 * qty, outdmg1),
				new ItemStack(Block.cloth, 4 * qty, outdmg2)
				},
			new ItemStack[]
				{
				new ItemStack(Block.cloth, 8 * qty, indmg),
				new ItemStack(mod_FCBetterThanWolves.fcSoap, 2 * qty, -1),
				new ItemStack(Item.clay, 1, -1)
				});
		FCRecipes.AddStokedCauldronRecipe(
			new ItemStack[]
				{
				new ItemStack(Block.cloth, 4 * qty, outdmg1),
				new ItemStack(Block.cloth, 4 * qty, outdmg2)
				},
			new ItemStack[]
				{
				new ItemStack(Block.cloth, 8 * qty, indmg),
				new ItemStack(mod_FCBetterThanWolves.fcSoap, 2 * qty, -1)
				});
		}

	public void load()
		{
		if(!HasInitialized)
			{
			MinecraftServer.getServer();
			MinecraftServer.logger.info(bwrProductString + " v" + this.getVersion() + " Initializing...");

			for(int i = 0; i < Block.blocksList.length; i++)
				{
				Block old = Block.blocksList[i];
				if((old instanceof FCBlockAestheticOpaque) && !(old instanceof BWRBlockAestheticOpaque))
					{
					Block.blocksList[i] = null;
					mod_FCBetterThanWolves.fcAestheticOpaque = new BWRBlockAestheticOpaque(i);
					MinecraftServer.logger.info("BWRBlockAestheticOpaque installed @ " + i);
					}
				if((old instanceof BlockLilyPad) && !(old instanceof BWRBlockLilyPad))
					{
					int blockIndexInTexture = Block.blocksList[i].blockIndexInTexture;
					Block.blocksList[i] = null;
					new BWRBlockLilyPad(i, blockIndexInTexture);
					MinecraftServer.logger.info("BWRBlockLilyPad installed @ " + i);
					}
				}
			
			DiamondRecoveryRecipe(Item.plateDiamond, 8);
			DiamondRecoveryRecipe(Item.legsDiamond, 7);
			DiamondRecoveryRecipe(Item.helmetDiamond, 5);
			DiamondRecoveryRecipe(Item.bootsDiamond, 4);
			DiamondRecoveryRecipe(Item.axeDiamond, 3);
			DiamondRecoveryRecipe(Item.pickaxeDiamond, 3);
			DiamondRecoveryRecipe(Item.swordDiamond, 2);
			DiamondRecoveryRecipe(Item.hoeDiamond, 2);
			DiamondRecoveryRecipe(Item.shovelDiamond, 1);

			LapisRecoveryRecipe(11, 0, 0, 1);
			LapisRecoveryRecipe(3, 0, 0, 2);
			LapisRecoveryRecipe(9, 13, 13, 2);
			LapisRecoveryRecipe(10, 14, 14, 2);
			LapisRecoveryRecipe(2, 6, 14, 4);
			
			FCRecipes.AddCauldronRecipe(
				new ItemStack[]
					{
					new ItemStack(Block.netherrack, 1),
					new ItemStack(mod_FCBetterThanWolves.fcSawDust, 1),
					},
				new ItemStack[]
					{
					new ItemStack(Block.cobblestoneMossy, 1, -1),
					new ItemStack(Item.netherStalkSeeds, 1, -1),
					new ItemStack(mod_FCBetterThanWolves.fcSoulDust, 1, -1)
					});
			FCRecipes.AddCauldronRecipe(
				new ItemStack(Block.netherrack, 8),
				new ItemStack[]
					{
					new ItemStack(Block.cobblestoneMossy, 8, -1),
					new ItemStack(Item.netherStalkSeeds, 8, -1),
					new ItemStack(mod_FCBetterThanWolves.fcSoulUrn, 1, -1)
					});

//			FCRecipes.AddStokedCauldronRecipe(
//				new ItemStack[]
//					{
//					new ItemStack(Block.dragonEgg, 1),
//					new ItemStack(Block.whiteStone, 8)
//					},
//				new ItemStack[]
//					{
//					new ItemStack(Block.dragonEgg, 1, -1),
//					new ItemStack(Block.cobblestoneMossy, 8, -1),
//					new ItemStack(Item.enderPearl, 1, -1)
//					});
//			FCRecipes.AddStokedCauldronRecipe(
//				new ItemStack[]
//					{
//					new ItemStack(Block.dragonEgg, 1),
//					new ItemStack(Block.whiteStone, 1)
//					},
//				new ItemStack[]
//					{
//					new ItemStack(Block.dragonEgg, 1, -1),
//					new ItemStack(Block.cobblestoneMossy, 1, -1)
//					});

//			FCRecipes.AddCauldronRecipe(
//				new ItemStack(Block.slowSand, 1),
//				new ItemStack[]
//					{
//					new ItemStack(Block.sand, 1, -1),
//					new ItemStack(mod_FCBetterThanWolves.fcSoulUrn, 1, -1)
//					});

			MinecraftServer.logger.info(bwrProductString + " Initialization Complete.");
			}

		HasInitialized = true;
		}

	public void ServerPlayerConnectionInitialized(NetServerHandler net, EntityPlayerMP player)
		{
		net.sendPacket(new Packet3Chat("\u00a7aBTW Add-On: " + bwrProductString + " v" + bwrVersionString));
		}
	}
