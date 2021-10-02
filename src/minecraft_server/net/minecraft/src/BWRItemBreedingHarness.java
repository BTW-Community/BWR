package net.minecraft.src;

//Replacement class for the Breeding Harness that works around an issue with
//cow subclassing (used to support animal cross-breeding).
public class BWRItemBreedingHarness extends FCItemBreedingHarness {
	public BWRItemBreedingHarness(int id) {
		super(id);
	}

	// Called when the item is used on a living entity, i.e. by a player
	// right-clicking an animal with it equipped.
	public boolean useItemOnEntity(ItemStack stack, EntityLiving orig) {
		if ((orig instanceof EntityCow) && !((EntityCow) orig).isChild()
				&& !((EntityCow) orig).getWearingBreedingHarness()) {
			// Handle cows specially, external to upstream code. The effect
			// is the same as originally intended, except that the correct
			// cow subclass is used, and the breeding harness is applied before
			// spawning the animal in the world, i.e. before BWR's code for
			// transforming (replacing) entities on spawn is called.
			BWREntityCow cow = new BWREntityCow(orig.worldObj);
			cow.setLocationAndAngles(orig.posX, orig.posY, orig.posZ, orig.rotationYaw, orig.rotationPitch);
			cow.setEntityHealth(orig.getHealth());
			cow.renderYawOffset = orig.renderYawOffset;
			cow.setWearingBreedingHarness(true);
			orig.worldObj.spawnEntityInWorld(cow);
			return true;
		} else
			return false;
	}
}
