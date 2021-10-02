package net.minecraft.src;

import java.util.Random;

public class BWRBlockAestheticOpaque extends FCBlockAestheticOpaque {

	public BWRBlockAestheticOpaque(int id) {
		super(id);

		this.setTickRandomly(true);
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
