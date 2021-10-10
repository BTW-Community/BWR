package net.minecraft.src;

import java.util.List;
import java.util.Random;

public class BWRDecoBlockAestheticOpaque extends FCBlockAestheticOpaque {

	public BWRDecoBlockAestheticOpaque(int id) {
		super(id);
		this.setTickRandomly(true);
	}

	public StepSound GetStepSound(World var1, int var2, int var3, int var4) {
		int meta = var1.getBlockMetadata(var2, var3, var4);
		switch (meta) {
		case 1:
			return FCBetterThanWolves.fcStepSoundSquish;
		case 15:
			return DecoManager.getNewSoundsInstalled() ? DecoDefs.stepSoundBone : soundGravelFootstep;
		default:
			return this.stepSound;
		}
	}

	public int getItemIDDroppedOnStonecutter(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		return meta == this.m_iSubtypeWhiteStone ? FCBetterThanWolves.fcBlockWhiteStoneSidingAndCorner.blockID : 0;
	}

	public int getItemCountDroppedOnStonecutter(World world, int x, int y, int z) {
		return 2;
	}

	public int getItemDamageDroppedOnStonecutter(World world, int x, int y, int z) {
		return 0;
	}

	public boolean doesBlockBreakStonecutter(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		return meta == m_iSubtypeBarrel || meta == m_iSubtypeDung || meta == m_iSubtypePadding || meta == m_iSubtypeRope
				|| meta == m_iSubtypeSoap || meta == m_iSubtypeWicker;
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood
	 * returns 4 blocks)
	 */
	public void getSubBlocks(int var1, CreativeTabs var2, List var3) {
		var3.add(new ItemStack(var1, 1, 3));
		var3.add(new ItemStack(var1, 1, 4));
		var3.add(new ItemStack(var1, 1, 5));
		var3.add(new ItemStack(var1, 1, 7));
		var3.add(new ItemStack(var1, 1, 9));
		var3.add(new ItemStack(var1, 1, 10));
		var3.add(new ItemStack(var1, 1, 13));
		var3.add(new ItemStack(var1, 1, 14));
		var3.add(new ItemStack(var1, 1, 15));
	}

	// Called randomly by World.
	@Override
	public void updateTick(World world, int x, int y, int z, Random r) {
		super.updateTick(world, x, y, z, r);

		int type = world.getBlockMetadata(x, y, z);
		if (type == FCBlockAestheticOpaque.m_iSubtypeHellfire) {
			// Concentrated hellfire blocks can
			// turn into lava source blocks when
			// placed near existing lava. Count the
			// number of lava and source
			// blocks nearby.
			int nearby = 0;
			int sources = 0;
			for (int dx = -1; dx <= 1; dx++)
				for (int dy = -1; dy <= 1; dy++)
					for (int dz = -1; dz <= 1; dz++) {
						int id = world.getBlockId(x + dx, y + dy, z + dz);
						if (id == Block.lavaStill.blockID) {
							sources++;
							nearby++;
							// Create some fire visual and sound effects, and replace
							for (int i = 0; i < 3; i++) {
								world.playAuxSFX(2004, x, y, z, 0);
								world.playSoundEffect(x, y, z, "random.fizz", 1.0F, world.rand.nextFloat() * 0.5F);
							}
						} else if (id == Block.lavaMoving.blockID)
							nearby++;
					}

			// There must be at least one lava source block adjacent to the hellfire,
			// but flowing lava also counts towards reaction heat, so reflowing lava
			// to surround the hellfire produces the fastest reaction speed.
			if ((sources > 0) && (r.nextInt(1200) < nearby)) {
				// Create some fire visual and sound effects, and replace
				// the hellfire block with a lava source block.
				for (int i = 0; i < 3; i++)
					world.playAuxSFX(2004, x, y, z, 0);
				world.playSoundEffect(x, y, z, "lava.pop", 1.0F, world.rand.nextFloat() * 0.5F);
				world.setBlockAndMetadataWithNotify(x, y, z, Block.lavaStill.blockID, 0);
			}
		}

	}

}
