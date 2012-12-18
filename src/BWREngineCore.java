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
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import net.minecraft.server.MinecraftServer;

// Main singleton representing overall BWR add-on, which receives events from
// external hooks and manages overall mod functionality.
public class BWREngineCore
	{
	// Central mod name and copyright strings.
	public static final String BWR_PRODUCT = "Better With Renewables";
	public static final String BWR_ABBREV = "BWR";
	public static final String BWR_COPYRIGHT = "(C)2012, MIT License.  https://gitorious.org/bwr";

	// Latest version announcement string, set by the auto update check thread.
	public static volatile String versionUpdateAlert = null;

	// Singleton variables.
	public static boolean isInitialized_ = false;
	public static BWREngineCore instance_ = null;

	// Mappings for entity replacements.
	private Map EntityTypeMap = new HashMap();

	// Get the singleton instance of the engine.
	public static BWREngineCore getInstance()
		{
		if(instance_ == null)
			instance_ = new BWREngineCore();
		return instance_;
		}

	// log a message to the server console log.
	public void log(String message)
		{
		MinecraftServer.getServer();
		MinecraftServer.logger.info(BWR_ABBREV + ": " + message);
		}

	// Find a block definition matching the old class type, and replace it with
	// the new class type, which can inherit from it, but override selected
	// methods and add/change functionality.
	public Block replaceBlock(Class<?> oldType, Class<?> newType)
		{
		// Search the block list and find the first block that matches
		// the original block class, or a subclass.
		for(int index = 0; index < Block.blocksList.length; index++)
			{
			Block block = Block.blocksList[index];
			if((block != null) && oldType.isAssignableFrom(block.getClass()))
				{
				// If the block has already been installed, just return it.
				if(newType.isAssignableFrom(block.getClass()))
					return block;

				// Search for one of 2 different constructor types and install
				// the new block.  Any errors are thrown as runtime exceptions
				// and crash the server on startup.
				log("Install " + newType.toString() + " @ " + index);
				float hardness = block.blockHardness;
				float resistance = block.blockResistance;
				String blockName = block.getBlockName();
				Block.blocksList[index] = null;
				try
					{
					try
						{
						block = (Block)newType
							.getConstructor(new Class[] { Integer.TYPE })
							.newInstance(new Object[] { index });
						}
					catch(NoSuchMethodException ex)
						{
						int blockIndexInTexture = block.blockIndexInTexture;
						block = (Block)newType
							.getConstructor(new Class[] { Integer.TYPE, Integer.TYPE })
							.newInstance(new Object[] { index, blockIndexInTexture });
						}
					}
				catch(NoSuchMethodException ex) { throw new RuntimeException(ex); }
				catch(InvocationTargetException ex) { throw new RuntimeException(ex); }
				catch(IllegalAccessException ex) { throw new RuntimeException(ex); }
				catch(InstantiationException ex) { throw new RuntimeException(ex); }

				// Preserve some stats that are defined outside of the constructor.
				return block.setHardness(hardness).setResistance(resistance).setBlockName(blockName);
				}
			}

		// If no suitable block to replace could be found, crash.
		throw new RuntimeException("FAILED Install " + newType.toString());
		}

	// Find an item definition matching the old class type, and replace it with
	// the new class type, which can inherit from it, but override selected
	// methods and add/change functionality.
	public Item replaceItem(Class<?> oldType, Class<?> newType)
		{
		// Search the item list and find the first item that matches
		// the original item class, or a subclass.
		final int INDEX_SHIFT = 256;
		for(int index = INDEX_SHIFT; index < Item.itemsList.length; index++)
			{
			Item item = Item.itemsList[index];
			if((item != null) && oldType.isAssignableFrom(item.getClass()))
				{
				// If the item has already been installed, just return it.
				if(newType.isAssignableFrom(item.getClass()))
					return item;

				// Search for an appropriate constructor and install the item.
				// Any errors are thrown as runtime exceptions and crash the
				// server on startup.
				log("Install " + newType.toString() + " @ " + index);
				Item.itemsList[index] = null;
				try
					{
					return (Item)newType
						.getConstructor(new Class[] { Integer.TYPE })
						.newInstance(new Object[] { index - INDEX_SHIFT });
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
	public void mapEntityReplacement(Class<?> original, Class<?> replacement, String name, int id)
		{
		// Register replacement class with EntityList, so that the correct class is
		// instantiated when loading chunks.
		EntityList.addMapping(replacement, name, id);

		// Register replacement class mapping with this mod, so that entities that
		// are transformed upon being added to the world get replaced with the correct
		// custom subclass.  New class must have a constructor that takes just
		// the world as a parameter.
		try
			{
			Constructor constructor = replacement.getConstructor(new Class[] { World.class });
			if(constructor == null)
				throw new RuntimeException("Unable to find constructor "
					+ replacement.toString() + "(World)");
			EntityTypeMap.put(original, constructor);
			}
		catch(NoSuchMethodException ex)
			{
			throw new RuntimeException(ex);
			}
		log("Install " + replacement.toString() + " over " + original.toString());
		}

	// Initialize the mod; called by a custom SMP server hook in World.
	public void initialize()
		{
		// Initialize the mod if it hasn't already been initialized.
		if(isInitialized_)
			return;
		isInitialized_ = true;

		log(BWR_PRODUCT + " v" + BWRVersionInfo.BWR_VERSION + " Initializing...");

		if(BWRVersionInfo.BWR_IS_DEV)
			log("THIS IS A PRE-RELEASE VERSION, NOT FOR PRODUCTION USE");

		// Start auto-update check.
		BWRThreadUpdateCheck.launch();
			
		// Replace some upstream block definitions with our custom ones, so our
		// custom logic is run for these blocks.
		mod_FCBetterThanWolves.fcAestheticOpaque = replaceBlock(FCBlockAestheticOpaque.class, BWRBlockAestheticOpaque.class);
		mod_FCBetterThanWolves.fcBlockFarmlandFertilized = replaceBlock(FCBlockFarmlandFertilized.class, BWRBlockFarmlandFertilized.class);
		mod_FCBetterThanWolves.fcPlanter = replaceBlock(FCBlockPlanter.class, BWRBlockPlanter.class);
		replaceBlock(BlockLilyPad.class, BWRBlockLilyPad.class);
		replaceBlock(BlockSoulSand.class, BWRBlockSoulSand.class);
		replaceBlock(BlockRedstoneWire.class, BWRBlockRedstoneWire.class);
		replaceBlock(BlockTallGrass.class, BWRBlockTallGrass.class);

		// After replacing blocks, some tool items have references to the old blocks by reference instead
		// of by ID in their list of blocks against which they're effective.  Search through and replace all
		// blocks with the current instances.
		Field blockField = null;
		for(Field field : ItemTool.class.getDeclaredFields())
			if(field.getType() == Block[].class)
				{
				blockField = field;
				break;
				}
		blockField.setAccessible(true);
		for(int index = 0; index < Item.itemsList.length; index++)
			{
			Item item = Item.itemsList[index];
			if((item != null) && (item instanceof ItemTool))
				try
					{
					Block[] blocks = (Block[])blockField.get(item);
					if(blocks != null)
						for(int blockIndex = 0; blockIndex < blocks.length; blockIndex++)
							blocks[blockIndex] = Block.blocksList[blocks[blockIndex].blockID];
					}
				catch(IllegalAccessException ex) { throw new RuntimeException(ex); }
			}
			
		// Replace upstream item definitions.
		mod_FCBetterThanWolves.fcBreedingHarness = replaceItem(FCItemBreedingHarness.class, BWRItemBreedingHarness.class);

		// Add mapping for custom Dragon Orb entities.
		mapEntityReplacement(EntityXPOrb.class, BWREntityXPOrb.class, "XPOrb", 2);
		mapEntityReplacement(EntitySheep.class, BWREntitySheep.class, "Sheep", 91);
		mapEntityReplacement(EntityPig.class, BWREntityPig.class, "Pig", 90);
		mapEntityReplacement(EntityCow.class, BWREntityCow.class, "Cow", 92);
		mapEntityReplacement(EntityWolf.class, BWREntityWolf.class, "Wolf", 95);
		mapEntityReplacement(EntityMooshroom.class, BWREntityMooshroom.class, "MushroomCow", 96);
		mapEntityReplacement(EntityOcelot.class, BWREntityOcelot.class, "Ozelot", 98);
		mapEntityReplacement(EntityVillager.class, BWREntityVillager.class, "Villager", 120);

		// Add custom BWR recipes.
		BWREngineRecipes.getInstance().addRecipes();

		// Initialize the plant/fungus and animal cross-breeding engines.
		BWREngineBreedAnimal.getInstance().initialize();
		BWREngineBreedPlant.getInstance().initialize();

		log(BWR_PRODUCT + " Initialization Complete.");
		}

	// Called by a custom hook World upon an entity being spawned.  This gives
	// the mod an opportunity to replace existing Entity class types with its
	// own subclasses.  Note that we cannot intercept entity creation, and we have
	// to replace existing entities later, when they're added to the world.
	public Entity transformEntityOnSpawn(Entity original)
		{
		// See if the type of this entity is mapped to a replacement.
		// If it is, use NBT to copy properties from the original to a
		// new instance of the replacement class, and return it instead.
		Constructor constructor = (Constructor)EntityTypeMap.get(original.getClass());
		if(constructor != null)
			{
			Entity replacement = null;
			try
				{
				replacement = (Entity)constructor.newInstance(new Object[] { original.worldObj });
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
			replacement.readFromNBT(Tag);
			original.setDead();
			return replacement;
			}

		return original;
		}

	// Get the string to set the following text to a specific color in
	// chat messages.
	public String chatColor(String colorCode)
		{
		return "\u00a7" + colorCode;
		}

	// Send a standard-formatted message to the user.
	public void announce(NetServerHandler net, String message)
		{
		net.sendPacket(new Packet3Chat(chatColor("a") + BWR_ABBREV + ": " + message));
		}

	// Called by a custom hook in ServerConfigurationManager when a new player logs in.
	public void serverPlayerConnectionInitialized(NetServerHandler net, EntityPlayerMP player)
		{
		// Announce the presence, and version, of BWR to the client in a
		// machine-readable fashion, so that client add-ons can detect BWR, e.g.
		// via public void clientCustomPayload(NetClientHandler, Packet250CustomPayload)
		// in the ModLoader API.
		ByteArrayOutputStream verstream = new ByteArrayOutputStream();
		try { new DataOutputStream(verstream).writeUTF(BWRVersionInfo.BWR_VERSION); }
		catch(Exception ex) { ex.printStackTrace(); }
		new Packet250CustomPayload("BWR|VC", verstream.toByteArray());

		// Let the player know of the add-on.  We don't have to do any version checks here
		// beyond those already done by BTW, as we're compatible with the BTW client.
		announce(net, BWR_PRODUCT + " BTW Add-On v" + BWRVersionInfo.BWR_VERSION);
		announce(net, BWR_COPYRIGHT);

		// If this is a development version, announce a warning to players.
		if(BWRVersionInfo.BWR_IS_DEV)
			announce(net, chatColor("4") + "THIS IS A PRE-RELEASE VERSION OF "
				+ BWR_ABBREV.toUpperCase());

		// If this version is not the same as the release version, announce
		// a warning to any player connecting (ostensibly, even if admins do
		// not log in, their players may alert them to the update).
		String upd = versionUpdateAlert;
		if(upd != null)
			announce(net, chatColor("6") + upd);
		}

	// Stopgap "Flatcore Spawn" to replace "Hardcore Spawn" until FC fixes HC Spawn to work well with
	// superflat worlds with most terrain at y < 64.  This is on his todo list, but we have no idea
	// when he'll be able to get to it.
	public boolean assignNewFlatcoreSpawnLocation(World world, EntityPlayerMP player)
		{
		for(int attempt = 0; attempt < 20; attempt++)
			{
			double r = world.rand.nextDouble() * 2000.0D;
			float a = (float)(world.rand.nextDouble() * 3.14159265358979323D * 2000.0D);
			int x = MathHelper.floor_double((double)MathHelper.cos(a) * r);
			int z = MathHelper.floor_double((double)MathHelper.sin(a) * r);
			int y = world.getTopSolidOrLiquidBlock(x, z);
			if((y < 0) || ((attempt >= 15) && (y < 64)))
				continue;
			Material material = world.getBlockMaterial(x, y, z);
			if((material != null) && material.isLiquid())
				continue;
			player.setLocationAndAngles((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D,
				world.rand.nextFloat() * 360.0F, 0.0F);
			while(!world.getCollidingBoundingBoxes(player, player.boundingBox).isEmpty())
				player.setLocationAndAngles(player.posX, player.posY + 1.0D, player.posZ,
					world.rand.nextFloat() * 360.0F, 0.0F);
			player.m_lTimeOfLastSpawnAssignment = MinecraftServer.getServer().worldServers[0].getWorldTime();
			player.setSpawnChunk(new ChunkCoordinates(x, MathHelper.floor_double(player.posY), z), false);
			return true;
			}
		return false;
		}
	}
