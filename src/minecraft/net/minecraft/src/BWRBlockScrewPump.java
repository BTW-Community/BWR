//DEV CLASS!!!!!

// MAY NEED MODIFICATION IN THE FUTURE DEPENDING ON UPSTREAM

package net.minecraft.src;

import java.util.Random;

public class BWRBlockScrewPump extends FCBlockScrewPump {
	public BWRBlockScrewPump(int id) {
		super(id);
		setTickRandomly(true);
	}

	boolean hasBedrockPipe(World world, int i, int j, int k) {
		FCUtilsBlockPos source = new FCUtilsBlockPos(i, j, k);
		source.AddFacingAsOffset(GetFacing(world, i, j, k));
		for (int dy = 0; dy < 3; dy++) {
			int blkid = world.getBlockId(source.i, source.j - dy, source.k);
			if (blkid == Block.bedrock.blockID)
				return true;
			if (blkid != Block.blockIron.blockID)
				return false;
		}
		return false;
	}

	// ------------- FCBlockScrewPump Methods modified ------------//

	@Override
	public boolean IsPumpingWater(World world, int i, int j, int k) {
		if (IsMechanicalOn(world, i, j, k) && !IsJammed(world, i, j, k)) {
			FCUtilsBlockPos sourcePos = new FCUtilsBlockPos(i, j, k);

			sourcePos.AddFacingAsOffset(GetFacing(world, i, j, k));
			if (hasBedrockPipe(world, i, j, k)) {
				return true;
			}

			int iSourceBlockID = world.getBlockId(sourcePos.i, sourcePos.j, sourcePos.k);

			if (iSourceBlockID == Block.waterMoving.blockID || iSourceBlockID == Block.waterStill.blockID) {
				return true;
			}
		}

		return false;
	}

	private boolean StartPumpSourceCheck(World world, int i, int j, int k) {
		// initial source check to prevent any dickery with getting pumps started with
		// temporary water

		FCUtilsBlockPos sourcePos = new FCUtilsBlockPos(i, j, k);

		sourcePos.AddFacingAsOffset(GetFacing(world, i, j, k));

		if (hasBedrockPipe(world, i, j, k)) {
			return true;
		}

		int iSourceBlockID = world.getBlockId(sourcePos.i, sourcePos.j, sourcePos.k);

		if (iSourceBlockID == Block.waterMoving.blockID || iSourceBlockID == Block.waterStill.blockID) {
			int iDistanceToCheck = 128;

			return FCUtilsMisc.DoesWaterHaveValidSource(world, sourcePos.i, sourcePos.j, sourcePos.k, iDistanceToCheck);
		}

		return false;
	}

	private boolean OnNeighborChangeShortPumpSourceCheck(World world, int i, int j, int k) {
		// this test just checks for an immediate infinite loop with the pump itself

		FCUtilsBlockPos sourcePos = new FCUtilsBlockPos(i, j, k);

		sourcePos.AddFacingAsOffset(GetFacing(world, i, j, k));

		if (hasBedrockPipe(world, i, j, k)) {
			return true;
		}

		int iSourceBlockID = world.getBlockId(sourcePos.i, sourcePos.j, sourcePos.k);

		if (iSourceBlockID == Block.waterMoving.blockID || iSourceBlockID == Block.waterStill.blockID) {
			int iDistanceToCheck = 4;

			return FCUtilsMisc.DoesWaterHaveValidSource(world, sourcePos.i, sourcePos.j, sourcePos.k, iDistanceToCheck);
		}

		return false;
	}

	// ------------- FCBlockScrewPump UnModified Methods ------------//

	@Override
	public void updateTick(World world, int i, int j, int k, Random random) {
		boolean bIsJammed = IsJammed(world, i, j, k);

		if (bIsJammed) {
			FCUtilsBlockPos sourcePos = new FCUtilsBlockPos(i, j, k);

			sourcePos.AddFacingAsOffset(GetFacing(world, i, j, k));

			int iSourceBlockID = world.getBlockId(sourcePos.i, sourcePos.j, sourcePos.k);

			if (iSourceBlockID != Block.waterMoving.blockID && iSourceBlockID != Block.waterStill.blockID) {
				// there is no longer any water at our input, so clear the jam

				SetIsJammed(world, i, j, k, false);
			}
		}

		boolean bReceivingPower = IsInputtingMechanicalPower(world, i, j, k);
		boolean bOn = IsMechanicalOn(world, i, j, k);

		if (bReceivingPower != bOn) {
			SetMechanicalOn(world, i, j, k, bReceivingPower);

			world.markBlockForUpdate(i, j, k);

			if (IsPumpingWater(world, i, j, k)) {
				// we just turned on, schedule another update to start pumping
				// (to give the impression the water has time to travel up)

				world.scheduleBlockUpdate(i, j, k, blockID, tickRate(world));
			}

			if (!bReceivingPower) {
				// clear any jams if we're turned off

				if (IsJammed(world, i, j, k)) {
					SetIsJammed(world, i, j, k, false);
				}
			}
		} else {
			if (bOn) {
				if (IsPumpingWater(world, i, j, k)) {
					boolean bSourceValidated = false;

					int iTargetBlockID = world.getBlockId(i, j + 1, k);

					if (iTargetBlockID == Block.waterMoving.blockID || iTargetBlockID == Block.waterStill.blockID) {
						if (OnNeighborChangeShortPumpSourceCheck(world, i, j, k)) {
							int iTargetHeight = world.getBlockMetadata(i, j + 1, k);

							if (iTargetHeight > 1 && iTargetHeight < 8) {
								// gradually increase the fluid height until it maxes at 1

								world.setBlockAndMetadataWithNotify(i, j + 1, k, Block.waterMoving.blockID,
										iTargetHeight - 1);

								// schedule another update to increase it further

								world.scheduleBlockUpdate(i, j, k, blockID, tickRate(world));
							}
						} else {
							SetIsJammed(world, i, j, k, true);
						}
					} else {
						// FCTODO: Break blocks here that water normally destroys

						if (world.isAirBlock(i, j + 1, k)) {
							if (StartPumpSourceCheck(world, i, j, k)) {
								// start the water off at min height

								world.setBlockAndMetadataWithNotify(i, j + 1, k, Block.waterMoving.blockID, 7);

								// schedule another update to increase it further

								world.scheduleBlockUpdate(i, j, k, blockID, tickRate(world));
							} else {
								SetIsJammed(world, i, j, k, true);
							}
						}
					}
				} else {
					int iTargetBlockID = world.getBlockId(i, j + 1, k);

					if (iTargetBlockID == Block.waterMoving.blockID || iTargetBlockID == Block.waterStill.blockID) {
						// if there is water above us, notify it that we are no longer pumping

						Block.blocksList[iTargetBlockID].onNeighborBlockChange(world, i, j + 1, k, blockID);
					}
				}
			}
		}
	}

	@Override
	public void RandomUpdateTick(World world, int i, int j, int k, Random rand) {
		boolean bWasJammed = IsJammed(world, i, j, k);
		boolean bIsJammed = bWasJammed;
		boolean bMechanicalOn = IsMechanicalOn(world, i, j, k);
		boolean bReceivingPower = IsInputtingMechanicalPower(world, i, j, k);

		if (bReceivingPower != bMechanicalOn) {
			// verify we have a tick already scheduled to prevent jams on chunk load

			if (!world.IsUpdateScheduledForBlock(i, j, k, blockID)) {
				world.scheduleBlockUpdate(i, j, k, blockID, tickRate(world));

				return;
			}
		}

		if (bMechanicalOn) {
			FCUtilsBlockPos sourcePos = new FCUtilsBlockPos(i, j, k);

			sourcePos.AddFacingAsOffset(GetFacing(world, i, j, k));

			int iSourceBlockID = world.getBlockId(sourcePos.i, sourcePos.j, sourcePos.k);

			if (iSourceBlockID != Block.waterMoving.blockID && iSourceBlockID != Block.waterStill.blockID) {
				// there is no longer any water at our input, so clear any jams

				bIsJammed = false;
			} else {
				int iDistanceToCheck = GetRandomDistanceForSourceCheck(rand);

				bIsJammed = !hasBedrockPipe(world, sourcePos.i, sourcePos.j, sourcePos.k) && !FCUtilsMisc
						.DoesWaterHaveValidSource(world, sourcePos.i, sourcePos.j, sourcePos.k, iDistanceToCheck);

				if (!bIsJammed && bWasJammed) {
					// schedule an update to start pumping again

					world.scheduleBlockUpdate(i, j, k, blockID, tickRate(world));
				}
			}
		} else {
			bIsJammed = false;
		}

		if (bWasJammed != bIsJammed) {
			SetIsJammed(world, i, j, k, bIsJammed);
		}
	}

	private int GetRandomDistanceForSourceCheck(Random rand) {
		// Select random distance here, favoring the lower end to save on performance

		int iDistanceToCheck = 32;
		int iRandomFactor = rand.nextInt(32);

		if (iRandomFactor == 0) {
			// this is the maximum distance at which the user could conceivably construct
			// an efficient infinite water loop (world height * 8 )

			iDistanceToCheck = 512;
		} else if (iRandomFactor <= 2) {
			iDistanceToCheck = 256;
		} else if (iRandomFactor <= 6) {
			iDistanceToCheck = 128;
		} else if (iRandomFactor <= 14) {
			iDistanceToCheck = 64;
		}

		return iDistanceToCheck;
	}

}
