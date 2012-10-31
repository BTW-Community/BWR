package net.minecraft.src;
import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.server.MinecraftServer;

public class mod_BetterWithRenewables {
	public static final String bwrProductString = "Better With Renewables";
	public static final String bwrAbbrString = "BWR";
	public static final String bwrVersionString = "0.17.0430";
	public static boolean HasInitialized = false;
	public static mod_BetterWithRenewables m_instance = new mod_BetterWithRenewables();

	public String getVersion()
		{
		return bwrVersionString;
		}

	public void Log(String msg)
		{
		MinecraftServer.getServer();
		MinecraftServer.logger.info(bwrAbbrString + ": " + msg);
		}

	public Block ReplaceBlock(Class<?> oldType, Class<?> newType)
		{
		for(int I = 0; I < Block.blocksList.length; I++)
			{
			Block B = Block.blocksList[I];
			if((B != null) && oldType.isAssignableFrom(B.getClass()))
				{
				if(newType.isAssignableFrom(B.getClass()))
					return B;
				Constructor IdOnly = null;
				Constructor IdTxr = null;
				for(Constructor ctor : newType.getDeclaredConstructors())
					{
					Class<?>[] p = ctor.getParameterTypes();
					if((p.length == 1) && p[0].toString().equals("int"))
						IdOnly = ctor;
					if((p.length == 2) && p[0].toString().equals("int")
						&& p[1].toString().equals("int"))
						IdTxr = ctor;
					}
				try
					{
					if(IdOnly != null)
						{
						Log("Install " + newType.toString() + " @ " + I);
						Block.blocksList[I] = null;
						return (Block)IdOnly.newInstance(new Object[] { I });
						}
					if(IdTxr != null)
						{
						Log("Install " + newType.toString() + " @ " + I);
						int blockIndexInTexture = B.blockIndexInTexture;
						Block.blocksList[I] = null;
						return (Block)IdTxr.newInstance(new Object[] { I, blockIndexInTexture });
						}
					}
				catch(InvocationTargetException ex)
					{
					throw new RuntimeException(ex);
					}
				catch(IllegalAccessException ex)
					{
					throw new RuntimeException(ex);
					}
				catch(InstantiationException ex)
					{
					throw new RuntimeException(ex);
					}
				throw new RuntimeException("FAILED Install " + newType.toString() + " @ " + I);
				}
			}
		throw new RuntimeException("FAILED Install " + newType.toString());
		}

	public void load()
		{
		if(!HasInitialized)
			{
			Log(bwrProductString + " v" + this.getVersion() + " Initializing...");

			// Replace some upstream block definitions with our custom ones, so our
			// custom logic is run for these blocks.
			mod_FCBetterThanWolves.fcAestheticOpaque = ReplaceBlock(FCBlockAestheticOpaque.class, BWRBlockAestheticOpaque.class);
			mod_FCBetterThanWolves.fcBlockFarmlandFertilized = ReplaceBlock(FCBlockFarmlandFertilized.class, BWRBlockFarmlandFertilized.class);
			mod_FCBetterThanWolves.fcPlanter = ReplaceBlock(FCBlockPlanter.class, BWRBlockPlanter.class);
			ReplaceBlock(BlockLilyPad.class, BWRBlockLilyPad.class);
			ReplaceBlock(BlockSoulSand.class, BWRBlockSoulSand.class);

			// Add mapping for custom Dragon Orb entities.
			EntityList.addMapping(BWREntityXPOrb.class, "XPOrb", 2);

			// Add custom BWR recipes.
			BWRRecipes.m_instance.AddRecipes();

			// Initialize the plant/fungus cross-breeding engine.
			BWRPlantBreedEngine.m_instance.Initialize();

			Log(bwrProductString + " Initialization Complete.");
			}

		HasInitialized = true;
		}

	public Entity TransformEntityOnSpawn(Entity original)
		{
		// Replace Dragon Orbs with custom BWR variety.
		if(original instanceof EntityXPOrb)
			{
			BWREntityXPOrb New = new BWREntityXPOrb(original.worldObj);
			NBTTagCompound Tag = new NBTTagCompound();
			original.writeToNBT(Tag);
			New.readFromNBT(Tag);
			return New;
			}

		return original;
		}

	public void ServerPlayerConnectionInitialized(NetServerHandler net, EntityPlayerMP player)
		{
		net.sendPacket(new Packet3Chat("\u00a7aBTW Add-On: " + bwrProductString + " v" + bwrVersionString));
		}
	}
