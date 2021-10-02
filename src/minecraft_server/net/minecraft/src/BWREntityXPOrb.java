package net.minecraft.src;

import java.util.List;

// Replacement class for BTW/Vanilla XP Orb entity, which represents both
// Vanilla XP Orbs and BTW Dragon Orbs, that adds some custom behavior.
public class BWREntityXPOrb extends EntityXPOrb {
	public static boolean[] soulPressBlocksAllowed = null;

	public BWREntityXPOrb(World world, double par2, double par4, double par6, int par8, boolean bNotPlayerOwned) {
		super(world, par2, par4, par6, par8, bNotPlayerOwned);
		// On first instance, create, and cache, a list of all
		// block ID's that are allowed as materials in the press
		// mechanism.

		if (world.isRemote)
			return;

		if (soulPressBlocksAllowed == null) {
			boolean[] allow = new boolean[Block.blocksList.length];
			for (int idx = 0; idx < Block.blocksList.length; idx++) {
				Block block = Block.blocksList[idx];
				allow[idx] = (block != null) && (block.blockHardness >= 5.0F) && block.isNormalCube(idx);
			}
			soulPressBlocksAllowed = allow;
		}
	}

	// useless piece of fucking garbage i fucking hate everything ever made
	public BWREntityXPOrb(World world, double par2, double par4, double par6, int par8) {
		super(world, par2, par4, par6, par8);

		if (world.isRemote)
			return;

	}

	// Called by Entity superclass on each update cycle to determine
	// if the entity is trapped inside solid blocks, and should be
	// pushed out in any direction.
	protected boolean pushOutOfBlocks(double x, double y, double z) {

		// Detect if this XP orb is not destroyed, and trapped
		// inside a sand block that's surrounded by suitable blocks
		// to form a "press."
		int bx = MathHelper.floor_double(x);
		int by = MathHelper.floor_double(y);
		int bz = MathHelper.floor_double(z);

		if (isDead || !m_bNotPlayerOwned || (this.worldObj.getBlockId(bx, by, bz) != Block.sand.blockID)
				|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx - 1, by, bz)]
				|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx + 1, by, bz)]
				|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx, by - 1, bz)]
				|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx, by + 1, bz)]
				|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx, by, bz - 1)]
				|| !soulPressBlocksAllowed[this.worldObj.getBlockId(bx, by, bz + 1)]) {
			return super.pushOutOfBlocks(x, y, z);
		}

		// If trapped in sand, destroy this XP orb and any others trapped
		// in the same block, and add up total XP value.
		int xp = this.xpValue;
		this.setDead();
		List found = this.worldObj.getEntitiesWithinAABB(BWREntityXPOrb.class,
				AxisAlignedBB.getAABBPool().getAABB(bx, by, bz, bx + 1, by + 1, bz + 1));
		if ((found != null) && (found.size() > 0))
			for (int i = 0; i < found.size(); i++) {
				BWREntityXPOrb orb = (BWREntityXPOrb) found.get(i);
				if (!orb.isDead && orb.m_bNotPlayerOwned) {
					xp += orb.xpValue;
					orb.setDead();
				}
			}
		// 2% probability per trapped XP point of converting sand to soulsand.
		// Large flame particle effect from soulsand conversion.
		if (this.worldObj.rand.nextInt(1) < xp) {
			// Burn all entities that are too close to the press.
			List hurt = this.worldObj.getEntitiesWithinAABB(Entity.class,
					AxisAlignedBB.getAABBPool().getAABB(bx - 2, by - 2, bz - 2, bx + 3, by + 4, bz + 3));
			if ((hurt != null) && (hurt.size() > 0))
				for (int i = 0; i < hurt.size(); i++) {

					Entity ent = (Entity) hurt.get(i);
					ent.attackEntityFrom(DamageSource.onFire, 1);
					ent.setFire(9);
				}

			// Set a bunch of fire around the press, so an automated one needs
			// to be sufficiently fire-proofed to keep running.
			for (int n = 0; n < 20; n++) {
				int px = bx + MathHelper.floor_double(this.worldObj.rand.nextGaussian() + 0.5D);
				int py = by + MathHelper.floor_double(this.worldObj.rand.nextGaussian() + 0.5D);
				int pz = bz + MathHelper.floor_double(this.worldObj.rand.nextGaussian() + 0.5D);
				if (this.worldObj.getBlockId(px, py, pz) == 0)
					this.worldObj.setBlockAndMetadataWithNotify(px, py, pz, Block.fire.blockID, 0);
			}

			// Play some sound/visual effects.
			for (int dx = -1; dx <= 1; dx++)
				for (int dy = -1; dy <= 1; dy++)
					for (int dz = -1; dz <= 1; dz++)
						this.worldObj.playAuxSFX(2004, bx + dx, by + dy, bz + dz, 0);
			for (int dx = -1; dx <= 1; dx += 2)
				for (int dy = -1; dy <= 1; dy += 2)
					for (int dz = -1; dz <= 1; dz += 2) {
						this.worldObj.playSoundEffect(x + dx, y + dy, z + dz, "mob.wither.shoot", 0.2F,
								(this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.8F);
						this.worldObj.playSoundEffect(x + dx, y + dy, z + dz, "random.fizz", 0.2F,
								(this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.8F);
					}

			// Replace sand with soulsand.
			this.worldObj.setBlockAndMetadataWithNotify(bx, by, bz, Block.slowSand.blockID, 0);
		}

		return super.pushOutOfBlocks(x, y, z);
	}
}
