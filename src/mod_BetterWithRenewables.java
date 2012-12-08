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
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.server.MinecraftServer;

// Main singleton representing overall BWR add-on, which receives events from
// external hooks and managed overall mod functionality.
public class mod_BetterWithRenewables {
	// Central mod meta-info strings, easily changeable for updates.
	public static final String bwrVersionString = "0.23.0440";
	public static final boolean bwrDevVersion = true;
	public static final String bwrProductString = "Better With Renewables";
	public static final String bwrAbbrString = "BWR";
	public static final String bwrCopyrightString = "(C)2012, MIT License.  https://gitorious.org/bwr";

	// Latest version string, set by the auto update check thread.
	public static volatile String bwrUpdateVersion = null;

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

				// Search for one of 2 different constructor types and install
				// the new block.  Any errors are thrown as runtime exceptions
				// and crash the server on startup.
				Log("Install " + newType.toString() + " @ " + I);
				float Hardness = B.blockHardness;
				float Resistance = B.blockResistance;
				String BlockName = B.getBlockName();
				Block.blocksList[I] = null;
				try
					{
					try
						{
						B = (Block)newType
							.getConstructor(new Class[] { Integer.TYPE })
							.newInstance(new Object[] { I });
						}
					catch(NoSuchMethodException ex)
						{
						int blockIndexInTexture = B.blockIndexInTexture;
						B = (Block)newType
							.getConstructor(new Class[] { Integer.TYPE, Integer.TYPE })
							.newInstance(new Object[] { I, blockIndexInTexture });
						}
					}
				catch(NoSuchMethodException ex) { throw new RuntimeException(ex); }
				catch(InvocationTargetException ex) { throw new RuntimeException(ex); }
				catch(IllegalAccessException ex) { throw new RuntimeException(ex); }
				catch(InstantiationException ex) { throw new RuntimeException(ex); }

				// Preserve some stats that are defined outside of the constructor.
				return B.setHardness(Hardness).setResistance(Resistance).setBlockName(BlockName);
				}
			}

		// If no suitable block to replace could be found, crash.
		throw new RuntimeException("FAILED Install " + newType.toString());
		}

	// Find an item definition matching the old class type, and replace it with
	// the new class type, which can inherit from it, but override selected
	// methods and add/change functionality.
	public Item ReplaceItem(Class<?> oldType, Class<?> newType)
		{
		// Search the item list and find the first item that matches
		// the original item class, or a subclass.
		final int IndexShift = 256;
		for(int I = IndexShift; I < Item.itemsList.length; I++)
			{
			Item B = Item.itemsList[I];
			if((B != null) && oldType.isAssignableFrom(B.getClass()))
				{
				// If the item has already been installed, just return it.
				if(newType.isAssignableFrom(B.getClass()))
					return B;

				// Search for an appropriate constructor and install the item.
				// Any errors are thrown as runtime exceptions and crash the
				// server on startup.
				Log("Install " + newType.toString() + " @ " + I);
				Item.itemsList[I] = null;
				try
					{
					return (Item)newType
						.getConstructor(new Class[] { Integer.TYPE })
						.newInstance(new Object[] { I - IndexShift });
					}
				catch(NoSuchMethodException ex) { throw new RuntimeException(ex); }
				catch(InvocationTargetException ex) { throw new RuntimeException(ex); }
				catch(IllegalAccessException ex) { throw new RuntimeException(ex); }
				catch(InstantiationException ex) { throw new RuntimeException(ex); }
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

			if(bwrDevVersion)
				Log("THIS IS A PRE-RELEASE VERSION, NOT FOR PRODUCTION USE");

			// Start auto-update check.
			bwrUpdateVersion = bwrVersionString;
			new BWRUpdateCheckThread().Launch();
			
			// Replace some upstream block definitions with our custom ones, so our
			// custom logic is run for these blocks.
			mod_FCBetterThanWolves.fcAestheticOpaque = ReplaceBlock(FCBlockAestheticOpaque.class, BWRBlockAestheticOpaque.class);
			mod_FCBetterThanWolves.fcBlockFarmlandFertilized = ReplaceBlock(FCBlockFarmlandFertilized.class, BWRBlockFarmlandFertilized.class);
			mod_FCBetterThanWolves.fcPlanter = ReplaceBlock(FCBlockPlanter.class, BWRBlockPlanter.class);
			ReplaceBlock(BlockLilyPad.class, BWRBlockLilyPad.class);
			ReplaceBlock(BlockSoulSand.class, BWRBlockSoulSand.class);
			ReplaceBlock(BlockRedstoneWire.class, BWRBlockRedstoneWire.class);
			ReplaceBlock(BlockTallGrass.class, BWRBlockTallGrass.class);

			// After replacing blocks, some tool items have references to the old blocks by reference instead
			// of by ID in their list of blocks against which they're effective.  Search through and replace all
			// blocks with the current instances.
			Field BlockField = null;
			for(Field F : ItemTool.class.getDeclaredFields())
				if(F.getType() == Block[].class)
					{
					BlockField = F;
					break;
					}
			BlockField.setAccessible(true);
			for(int I = 0; I < Item.itemsList.length; I++)
				{
				Item T = Item.itemsList[I];
				if((T != null) && (T instanceof ItemTool))
					try
						{
						Block[] Blocks = (Block[])BlockField.get(T);
						if(Blocks != null)
							for(int J = 0; J < Blocks.length; J++)
								Blocks[J] = Block.blocksList[Blocks[J].blockID];
						}
					catch(IllegalAccessException ex) { throw new RuntimeException(ex); }
				}
			
			// Replace upstream item definitions.
			mod_FCBetterThanWolves.fcBreedingHarness = ReplaceItem(FCItemBreedingHarness.class, BWRItemBreedingHarness.class);

			// Add mapping for custom Dragon Orb entities.
			MapEntityReplacement(EntityXPOrb.class, BWREntityXPOrb.class, "XPOrb", 2);
			MapEntityReplacement(EntitySheep.class, BWREntitySheep.class, "Sheep", 91);
			MapEntityReplacement(EntityPig.class, BWREntityPig.class, "Pig", 90);
			MapEntityReplacement(EntityCow.class, BWREntityCow.class, "Cow", 92);
			MapEntityReplacement(EntityWolf.class, BWREntityWolf.class, "Wolf", 95);
			MapEntityReplacement(EntityMooshroom.class, BWREntityMooshroom.class, "MushroomCow", 96);
			MapEntityReplacement(EntityOcelot.class, BWREntityOcelot.class, "Ozelot", 98);
			MapEntityReplacement(EntityVillager.class, BWREntityVillager.class, "Villager", 120);

			// Add custom BWR recipes.
			BWRRecipes.m_instance.AddRecipes();

			// Initialize the plant/fungus and animal cross-breeding engines.
			BWRPlantBreedEngine.m_instance.Initialize();
			BWRAnimalBreedEngine.m_instance.Initialize();

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
			original.setDead();
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

		// If this is a development version, announce a warning to players.
		if(bwrDevVersion)
			Announce(net, "\u00a74THIS IS A PRE-RELEASE VERSION OF "
				+ bwrAbbrString.toUpperCase());

		// If this version is not the same as the release version, announce
		// a warning to any player connecting (ostensibly, even if admins do
		// not log in, their players may alert them to the update).
		String Upd = bwrUpdateVersion;
		if(Upd != null)
			Announce(net, "\u00a76" + Upd);
		}
	}
