package net.minecraft.src;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

// Main singleton representing overall BWR add-on, which receives events from
// external hooks and manages overall mod functionality.
public class BWREngineCore extends AddonExt {
	// Central mod name and copyright strings.
	public static final String BWR_PRODUCT = "Better With Renewables";
	public static final String BWR_ABBREV = "BWR";
	public static final String BWR_COPYRIGHT = "(C)2021, MIT License.  https://github.com/btw-community/BWR";

	public BWREngineCore() {
		super(BWR_PRODUCT, BWRVersionInfo.BWR_VERSION, BWR_ABBREV);
		// TODO Auto-generated constructor stub
	}

	// Latest version announcement string, set by the auto update check thread.
	public static volatile String versionUpdateAlert = null;

	// Singleton variables.
	public static boolean isInitialized_ = false;
	public static BWREngineCore instance_ = null;

	// Mappings for entity replacements.
	private Map EntityTypeMap = new HashMap();

	// Get the singleton instance of the engine.
	public static BWREngineCore getInstance() {
		if (instance_ == null)
			instance_ = new BWREngineCore();
		return instance_;
	}

	// log a message to the server console log.
	public void log(String message) {
		FCAddOnHandler.LogMessage(BWR_ABBREV + ": " + message);
	}

	// This method will be removed eventually. DawnLibrary has a replacement for it
	// Find a block definition matching the old class type, and replace it with
	// the new class type, which can inherit from it, but override selected
	// methods and add/change functionality.
	public Block replaceBlock(Class<?> oldType, Class<?> newType) {
		// Search the block list and find the first block that matches
		// the original block class, or a subclass.
		for (int index = 0; index < Block.blocksList.length; index++) {
			Block block = Block.blocksList[index];
			if ((block != null) && oldType.isAssignableFrom(block.getClass())) {
				// If the block has already been installed, just return it.
				if (newType.isAssignableFrom(block.getClass()))
					return block;

				// Search for one of 2 different constructor types and install
				// the new block. Any errors are thrown as runtime exceptions
				// and crash the server on startup.
				log("Install " + newType.toString() + " @ " + index);
				float hardness = block.blockHardness;
				float resistance = block.blockResistance;
				String blockName = block.getUnlocalizedName();
				Block.blocksList[index] = null;
				try {
					block = (Block) newType.getConstructor(new Class[] { Integer.TYPE })
							.newInstance(new Object[] { index });
				} catch (NoSuchMethodException ex) {
					throw new RuntimeException(ex);
				} catch (InvocationTargetException ex) {
					throw new RuntimeException(ex);
				} catch (IllegalAccessException ex) {
					throw new RuntimeException(ex);
				} catch (InstantiationException ex) {
					throw new RuntimeException(ex);
				}

				// Preserve some stats that are defined outside of the constructor.
				return block.setHardness(hardness).setResistance(resistance).setUnlocalizedName(blockName);
			}
		}

		// If no suitable block to replace could be found, crash.
		throw new RuntimeException("FAILED Install " + newType.toString());
	}

	// This method will be removed eventually. DawnLibrary has a replacement for it
	// Find an item definition matching the old class type, and replace it with
	// the new class type, which can inherit from it, but override selected
	// methods and add/change functionality.
	public Item replaceItem(Class<?> oldType, Class<?> newType) {
		// Search the item list and find the first item that matches
		// the original item class, or a subclass.
		final int INDEX_SHIFT = 256;
		for (int index = INDEX_SHIFT; index < Item.itemsList.length; index++) {
			Item item = Item.itemsList[index];
			if ((item != null) && oldType.isAssignableFrom(item.getClass())) {
				// If the item has already been installed, just return it.
				if (newType.isAssignableFrom(item.getClass()))
					return item;

				// Search for an appropriate constructor and install the item.
				// Any errors are thrown as runtime exceptions and crash the
				// server on startup.
				log("Install " + newType.toString() + " @ " + index);
				Item.itemsList[index] = null;
				try {
					return (Item) newType.getConstructor(new Class[] { Integer.TYPE })
							.newInstance(new Object[] { index - INDEX_SHIFT });
				} catch (NoSuchMethodException ex) {
					throw new RuntimeException(ex);
				} catch (InvocationTargetException ex) {
					throw new RuntimeException(ex);
				} catch (IllegalAccessException ex) {
					throw new RuntimeException(ex);
				} catch (InstantiationException ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		// If no suitable block to replace could be found, crash.
		throw new RuntimeException("FAILED Install " + newType.toString());
	}

	// This method will be removed eventually. DawnLibrary has a replacement for it
	// Helper to register Entity replacements with the two places where they need
	// to be done: on chunk loading by EntityList, and on spawn by this mod.
	public void mapEntityReplacement(Class<?> original, Class<?> replacement, String name, int id) {
		// Register replacement class with EntityList, so that the correct class is
		// instantiated when loading chunks.
		EntityList.addMapping(replacement, name, id);

		// Register replacement class mapping with this mod, so that entities that
		// are transformed upon being added to the world get replaced with the correct
		// custom subclass. New class must have a constructor that takes just
		// the world as a parameter.
		try {
			Constructor constructor = replacement.getConstructor(new Class[] { World.class });
			if (constructor == null)
				throw new RuntimeException("Unable to find constructor " + replacement.toString() + "(World)");
			EntityTypeMap.put(original, constructor);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
		log("Install " + replacement.toString() + " over " + original.toString());
	}

	@Override
	public void Initialize() {
		// Initialize the mod if it hasn't already been initialized.
		if (isInitialized_)
			return;
		isInitialized_ = true;

		log(BWR_PRODUCT + " v" + BWRVersionInfo.BWR_VERSION + " Initializing...");

		if (BWRVersionInfo.BWR_IS_DEV)
			log("THIS IS A PRE-RELEASE VERSION, NOT FOR PRODUCTION USE");

		// Start auto-update check.
		// BWRThreadUpdateCheck.launch();

		// Replace some upstream block definitions with our custom ones, so our
		// custom logic is run for these blocks.

		Block.redstoneWire = (BlockRedstoneWire) Block.replaceBlock(Block.redstoneWire.blockID,
				BWRBlockRedstoneWire.class);

		Block.skull = (BlockSkull) Block.replaceBlock(Block.skull.blockID, BWRBlockSkull.class);

		Block.waterlily = (BlockLilyPad) Block.replaceBlock(Block.waterlily.blockID, BWRBlockLilyPad.class);

		Block.slowSand = (BlockSoulSand) Block.replaceBlock(Block.slowSand.blockID, BWRBlockSoulSand.class);

		Block.tallGrass = (BlockTallGrass) Block.replaceBlock(Block.tallGrass.blockID, BWRBlockTallGrass.class);
		Item.itemsList[Block.tallGrass.blockID] = (new ItemColored(Block.tallGrass.blockID - 256, true))
				.setBlockNames(new String[] { "shrub", "grass", "fern" });

		FCBetterThanWolves.fcBlockFarmlandFertilized = (FCBlockFarmlandFertilized) Block
				.replaceBlock(FCBetterThanWolves.fcBlockFarmlandFertilized.blockID, BWRBlockFarmlandFertilized.class);

		FCBetterThanWolves.fcAestheticOpaque = (FCBlockAestheticOpaque) Block
				.replaceBlock(FCBetterThanWolves.fcAestheticOpaque.blockID, BWRBlockAestheticOpaque.class);

		FCBetterThanWolves.fcPlanter = (FCBlockPlanter) Block.replaceBlock(FCBetterThanWolves.fcPlanter.blockID,
				BWRBlockPlanter.class);

		// Replace upstream item definitions.
		FCBetterThanWolves.fcItemBreedingHarness = replaceItem(FCItemBreedingHarness.class,
				BWRItemBreedingHarness.class);

		// Add mapping for custom Dragon Orb entities.
		mapEntityReplacement(EntityXPOrb.class, BWREntityXPOrb.class, "XPOrb", 2);
		mapEntityReplacement(EntityBlaze.class, BWREntityBlaze.class, "Blaze", 61);
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

	// Called by a custom hook World upon an entity being spawned. This gives
	// the mod an opportunity to replace existing Entity class types with its
	// own subclasses. Note that we cannot intercept entity creation, and we have
	// to replace existing entities later, when they're added to the world.

	public Entity transformEntityOnSpawn(Entity original) {
		// See if the type of this entity is mapped to a replacement.
		// If it is, use NBT to copy properties from the original to a
		// new instance of the replacement class, and return it instead.
		Constructor constructor = (Constructor) EntityTypeMap.get(original.getClass());
		if (constructor != null) {
			Entity replacement = null;
			try {
				replacement = (Entity) constructor.newInstance(new Object[] { original.worldObj });
			} catch (InvocationTargetException ex) {
				throw new RuntimeException(ex);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			} catch (InstantiationException ex) {
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
	public String chatColor(String colorCode) {
		return "\u00a7" + colorCode;
	}

	// Send a standard-formatted message to the user.
	public void announce(NetServerHandler net, String message) {
		net.sendPacket(new Packet3Chat(chatColor("a") + BWR_ABBREV + ": " + message));
	}

	// Called by a custom hook in ServerConfigurationManager when a new player logs
	// in.
	public void serverPlayerConnectionInitialized(NetServerHandler net, EntityPlayerMP player) {
		// Announce the presence, and version, of BWR to the client in a
		// machine-readable fashion, so that client add-ons can detect BWR, e.g.
		// via public void clientCustomPayload(NetClientHandler, Packet250CustomPayload)
		// in the ModLoader API.
		ByteArrayOutputStream verstream = new ByteArrayOutputStream();
		try {
			new DataOutputStream(verstream).writeUTF(BWRVersionInfo.BWR_VERSION);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		net.sendPacket(new Packet250CustomPayload("BWR|VC", verstream.toByteArray()));

		// Let the player know of the add-on. We don't have to do any version checks
		// here
		// beyond those already done by BTW, as we're compatible with the BTW client.
		announce(net, BWR_PRODUCT + " BTW Add-On v" + BWRVersionInfo.BWR_VERSION);
		announce(net, BWR_COPYRIGHT);

		// If this is a development version, announce a warning to players.
		if (BWRVersionInfo.BWR_IS_DEV)
			announce(net, chatColor("4") + "THIS IS A PRE-RELEASE VERSION OF " + BWR_ABBREV.toUpperCase());

		// If this version is not the same as the release version, announce
		// a warning to any player connecting (ostensibly, even if admins do
		// not log in, their players may alert them to the update).
		String upd = versionUpdateAlert;
		if (upd != null)
			announce(net, chatColor("6") + upd);
	}

}
