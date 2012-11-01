package net.minecraft.src;
import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.server.MinecraftServer;

public class mod_BetterWithRenewables {
	// Central mod meta-info strings, easily changeable for updates.
	public static final String bwrProductString = "Better With Renewables";
	public static final String bwrAbbrString = "BWR";
	public static final String bwrVersionString = "0.17.0430";
	public static final String bwrCopyrightString = "(C)2012, MIT License.  https://gitorious.org/bwr";

	// Singleton variables.
	public static boolean HasInitialized = false;
	public static mod_BetterWithRenewables m_instance = new mod_BetterWithRenewables();

	// Log a message to the server console log.
	public void Log(String msg)
		{
		MinecraftServer.getServer();
		MinecraftServer.logger.info(bwrAbbrString + ": " + msg);
		}

	// Find a block definition matching the old class type, and replace it with
	// the new class type, which can inherit from it, but override selected
	// methods and add/change functionality.
	public Block ReplaceBlock(Class<?> oldType, Class<?> newType)
		{
		// Search the block list and find the first block that matches
		// the original block class, or a subclass.
		for(int I = 0; I < Block.blocksList.length; I++)
			{
			Block B = Block.blocksList[I];
			if((B != null) && oldType.isAssignableFrom(B.getClass()))
				{
				// If the block has already been installed, just return it.
				if(newType.isAssignableFrom(B.getClass()))
					return B;

				// Search the new class constructors and try to find one that
				// takes the block ID, or one that takes block ID and
				// texture index.
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

				// Try to create an instance of the new class using one of
				// the constructors found, with the original block ID and
				// texture index.  Rethrow any errors as runtime exceptions;
				// this will probably cause the server to crash on startup if
				// there is a failure here.
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

				// If neither constructor type could be found, throw an error and
				// let the server crash.
				throw new RuntimeException("FAILED Install " + newType.toString() + " @ " + I);
				}
			}

		// If no suitable block to replace could be found, crash.
		throw new RuntimeException("FAILED Install " + newType.toString());
		}

	// Initialize the mod; called by a custom SMP server hook in World.
	public void load()
		{
		// Initialize the mod if it hasn't already been initialized.
		if(!HasInitialized)
			{
			Log(bwrProductString + " v" + bwrVersionString + " Initializing...");

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

	// Called by a custom hook World upon an entity being spawned.  This gives
	// the mod an opportunity to replace existing Entity class types with its
	// own subclasses.  Note that we cannot intercept entity creation, and we have
	// to replace existing entities later, when they're added to the world.
	public Entity TransformEntityOnSpawn(Entity original)
		{
		// Replace Dragon Orbs with custom BWR variety.  Use NBT serialization
		// to copy all appropriate properties from the original to the replacement.
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

	// Send a standard-formatted message to the user.
	public void Announce(NetServerHandler net, String msg)
		{
		net.sendPacket(new Packet3Chat("\u00a7a" + bwrAbbrString + ": " + msg));
		}

	// Called by a custom hook in ServerConfigurationManager when a new player logs in.
	public void ServerPlayerConnectionInitialized(NetServerHandler net, EntityPlayerMP player)
		{
		// Let the player know of the add-on.  We don't have to do any version checks here
		// beyond those already done by BTW, as we're compatible with the BTW client.
		Announce(net, bwrProductString + " BTW Add-On v" + bwrVersionString);
		Announce(net, bwrCopyrightString);
		}
	}
