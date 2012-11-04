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

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.server.MinecraftServer;

// Main singleton representing overall BWR add-on, which receives events from
// external hooks and managed overall mod functionality.
public class mod_BetterWithRenewables {
	// Central mod meta-info strings, easily changeable for updates.
	public static final String bwrProductString = "Better With Renewables";
	public static final String bwrAbbrString = "BWR";
	public static final String bwrVersionString = "0.19.0430";
	public static final String bwrCopyrightString = "(C)2012, MIT License.  https://gitorious.org/bwr";

	// Singleton variables.
	public static boolean HasInitialized = false;
	public static mod_BetterWithRenewables m_instance = new mod_BetterWithRenewables();

	// Mappings for entity replacements.
	private Map EntityTypeMap = new HashMap();

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

	// Helper to register Entity replacements with the two places where they need
	// to be done: on chunk loading by EntityList, and on spawn by this mod.
	public void MapEntityReplacement(Class<?> orig, Class<?> repl, String name, int id)
		{
		// Register replacement class with EntityList, so that the correct class is
		// instantiated when loading chunks.
		EntityList.addMapping(repl, name, id);

		// Register replacement class mapping with this mod, so that entities that
		// are transformed upon being added to the world get replaced with the correct
		// custom subclass.  New class must have a constructor that takes just
		// the world as a parameter.
		try
			{
			Constructor Found = repl.getConstructor(new Class[] { World.class });
			if(Found == null)
				throw new RuntimeException("Unable to find constructor "
					+ repl.toString() + "(World)");
			EntityTypeMap.put(orig, Found);
			}
		catch(NoSuchMethodException ex)
			{
			throw new RuntimeException(ex);
			}
		Log("Install " + repl.toString() + " over " + orig.toString());
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
			MapEntityReplacement(EntityXPOrb.class, BWREntityXPOrb.class, "XPOrb", 2);
			MapEntityReplacement(EntitySheep.class, BWREntitySheep.class, "Sheep", 91);
			MapEntityReplacement(EntityPig.class, BWREntityPig.class, "Pig", 90);
			MapEntityReplacement(EntityCow.class, BWREntityCow.class, "Cow", 92);
			MapEntityReplacement(EntityWolf.class, BWREntityWolf.class, "Wolf", 95);
			MapEntityReplacement(EntityMooshroom.class, BWREntityMooshroom.class, "MushroomCow", 96);
			MapEntityReplacement(EntityOcelot.class, BWREntityOcelot.class, "Ozelot", 98);

			// Add custom BWR recipes.
			BWRRecipes.m_instance.AddRecipes();

			// Initialize the plant/fungus cross-breeding engine.
			BWRPlantBreedEngine.m_instance.Initialize();

			Log(bwrProductString + " Initialization Complete.");
			}

		HasInitialized = true;
		}

	// Helper for TransformEntityOnSpawn to replace entities with BWR-specific-subclassed
	// ones.  Uses NBT as a cheap serialization hack to copy all relevant properties.
	public Entity ReplaceEntity(Entity original, Entity replacement)
		{
		NBTTagCompound Tag = new NBTTagCompound();
		original.writeToNBT(Tag);
		replacement.readFromNBT(Tag);
		return replacement;
		}

	// Called by a custom hook World upon an entity being spawned.  This gives
	// the mod an opportunity to replace existing Entity class types with its
	// own subclasses.  Note that we cannot intercept entity creation, and we have
	// to replace existing entities later, when they're added to the world.
	public Entity TransformEntityOnSpawn(Entity original)
		{
		// See if the type of this entity is mapped to a replacement.
		// If it is, use NBT to copy properties from the original to a
		// new instance of the replacement class, and return it instead.
		Constructor ctor = (Constructor)EntityTypeMap.get(original.getClass());
		if(ctor != null)
			{
			Entity Repl = null;
			try
				{
				Repl = (Entity)ctor.newInstance(new Object[] { original.worldObj });
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
			NBTTagCompound Tag = new NBTTagCompound();
			original.writeToNBT(Tag);
			Repl.readFromNBT(Tag);
			return Repl;
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
