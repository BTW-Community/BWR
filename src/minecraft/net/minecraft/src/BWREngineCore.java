package net.minecraft.src;

// Main singleton representing overall BWR add-on, which receives events from
// external hooks and manages overall mod functionality.
public class BWREngineCore extends FCAddOn {
	// Central mod name and copyright strings.
	public static final String BWR_PRODUCT = "Better With Renewables";
	public static final String BWR_ABBREV = "BWR";
	public static final String BWR_COPYRIGHT = "(C)2021, MIT License.  https://github.com/btw-community/BWR";
	public static final String BWR_VERSION = "0.29.1 DEV";

	public BWREngineCore() {
		super(BWR_PRODUCT, BWR_VERSION, BWR_ABBREV);
	}

	// Singleton variables.
	public static boolean isInitialized_ = false;
	public static BWREngineCore instance_ = null;

	// Get the singleton instance of the engine.
	public static BWREngineCore getInstance() {
		if (instance_ == null)
			instance_ = new BWREngineCore();
		return instance_;
	}

	public void log(String message) {
		FCAddOnHandler.LogMessage(BWR_ABBREV + ": " + message);
	}

	@Override
	public void Initialize() {
		// Initialize the mod if it hasn't already been initialized.
		if (isInitialized_)
			return;
		isInitialized_ = true;

		BWREngineRecipes.getInstance().addRecipes();
		BWREngineDefs.getInstance().addDefinitions();
		BWREngineDefs.getInstance().addReplacements();

		// Initialize the plant/fungus and animal cross-breeding engines.
		BWREngineBreedAnimal.getInstance().initialize();
		BWREngineBreedPlant.getInstance().initialize();
	}

}
