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

import java.util.List;
import java.util.Random;

// Subclass for villagers that adds custom functionality, such as trading
// limitations, and alternative nether portal creation.
public class BWREntityVillager extends EntityVillager
	{
	// The list of items the villagers are allowed to sell.  Any trade
	// whose output is not one of the items on this list will be blocked.
	private static boolean[] TradingWhitelist;

	public BWREntityVillager(World world)
		{
		super(world);

		// If the trading whitelist is not initialized, initialize it.
		if(TradingWhitelist == null)
			{
			boolean[] WL = new boolean[Item.itemsList.length];

			// Villagers can buy items for emeralds.
			WL[Item.emerald.shiftedIndex] = true;

			// Villagers can sell only finished products, nothing
			// made of gold or diamond, and no raw materials.
			// All meats must be cooked (no tallow sources).
			WL[Item.shears.shiftedIndex] = true;
			WL[Item.shovelSteel.shiftedIndex] = true;
			WL[Item.pickaxeSteel.shiftedIndex] = true;
			WL[Item.axeSteel.shiftedIndex] = true;
			WL[Item.flintAndSteel.shiftedIndex] = true;
			WL[Item.appleRed.shiftedIndex] = true;
			WL[Item.swordSteel.shiftedIndex] = true;
			WL[Item.swordWood.shiftedIndex] = true;
			WL[Item.shovelWood.shiftedIndex] = true;
			WL[Item.pickaxeWood.shiftedIndex] = true;
			WL[Item.axeWood.shiftedIndex] = true;
			WL[Item.swordStone.shiftedIndex] = true;
			WL[Item.shovelStone.shiftedIndex] = true;
			WL[Item.pickaxeStone.shiftedIndex] = true;
			WL[Item.axeStone.shiftedIndex] = true;
			WL[Item.hoeWood.shiftedIndex] = true;
			WL[Item.hoeStone.shiftedIndex] = true;
			WL[Item.hoeSteel.shiftedIndex] = true;
			WL[Item.bread.shiftedIndex] = true;
			WL[Item.helmetLeather.shiftedIndex] = true;
			WL[Item.plateLeather.shiftedIndex] = true;
			WL[Item.legsLeather.shiftedIndex] = true;
			WL[Item.bootsLeather.shiftedIndex] = true;
			WL[Item.helmetChain.shiftedIndex] = true;
			WL[Item.plateChain.shiftedIndex] = true;
			WL[Item.legsChain.shiftedIndex] = true;
			WL[Item.bootsChain.shiftedIndex] = true;
			WL[Item.helmetSteel.shiftedIndex] = true;
			WL[Item.plateSteel.shiftedIndex] = true;
			WL[Item.legsSteel.shiftedIndex] = true;
			WL[Item.bootsSteel.shiftedIndex] = true;
			WL[Item.saddle.shiftedIndex] = true;
			WL[Item.fishCooked.shiftedIndex] = true;
			WL[Item.chickenCooked.shiftedIndex] = true;
			WL[Item.expBottle.shiftedIndex] = true;

			TradingWhitelist = WL;
			}
		}

	// Called by entity AI subsystem.
	protected void updateAITick()
		{
		super.updateAITick();

		// Search the villager's trade list and remove any contraband
		// recipes.  Determine if there is at least one trade that is
		// not exhausted.
		MerchantRecipeList Merch = this.getRecipes(null);
		boolean HasTrade = false;
		for(int I = Merch.size() - 1; I >= 0; I--)
			{
			MerchantRecipe MR = (MerchantRecipe)Merch.get(I);
			if(!TradingWhitelist[MR.getItemToSell().itemID])
				Merch.remove(I);
			else if(!MR.func_82784_g())
				HasTrade = true;
			}

		// If all trades are exhausted, normally Vanilla would create a new
		// trade, but it's possible that BWR blacklisted the trade, and the
		// villager is no longer offering any trades.  If this is the case,
		// then try to generate a replacement trade.
		if(!HasTrade)
			{
			// Create a surrogate villager as a duplicate of this one,
			// but with the trade array removed, so new trades will
			// generate on accessing the recipe list.  This is a workaround
			// for accessibility limitations of the trade list, without
			// having to call an entire AI cycle.
			NBTTagCompound Tag = new NBTTagCompound();
			this.writeEntityToNBT(Tag);
			Tag.removeTag("Offers");
			EntityVillager Surrogate = new EntityVillager(this.worldObj);
			Surrogate.readEntityFromNBT(Tag);

			// Loop through surrogate's now freshly-initialized recipe
			// list and remove any contraband.
			MerchantRecipeList SMerch = Surrogate.getRecipes(null);
			for(int I = SMerch.size() - 1; I >= 0; I--)
				{
				MerchantRecipe MR = (MerchantRecipe)SMerch.get(I);
				if(!TradingWhitelist[MR.getItemToSell().itemID])
					SMerch.remove(I);
				}

			// Copy any valid trades from the surrogate back to the original.
			for(int I = SMerch.size() - 1; I >= 0; I--)
				Merch.add(SMerch.get(I));

			Surrogate.setDead();
			}
		}

	// Called by interact.  Search for an emerald portal frame given a specific location
	// and orientation, and if found, convert it to obsidian and create the nether portal.
	private boolean SearchPortal(World world, int x, int y, int z, int dx, int dz)
		{
		int Em = Block.blockEmerald.blockID;

		// Make sure the portal is filled with a 2x3 air space...
		if(!world.getBlockMaterial(x, y, z).isReplaceable()
			|| !world.getBlockMaterial(x + dx, y, z + dz).isReplaceable()
			|| !world.getBlockMaterial(x, y + 1, z).isReplaceable()
			|| !world.getBlockMaterial(x + dx, y + 1, z + dz).isReplaceable()
			|| !world.getBlockMaterial(x, y + 2, z).isReplaceable()
			|| !world.getBlockMaterial(x + dx, y + 2, z + dz).isReplaceable()

			// ...and the top and bottom frames are made of emerald block...
			|| (world.getBlockId(x, y - 1, z) != Em)
			|| (world.getBlockId(x + dx, y - 1, z + dz) != Em)
			|| (world.getBlockId(x, y + 3, z) != Em)
			|| (world.getBlockId(x + dx, y + 3, z + dz) != Em)

			// ...and the sides are also emerald block.
			|| (world.getBlockId(x + dx * 2, y, z + dz * 2) != Em)
			|| (world.getBlockId(x + dx * 2, y + 1, z + dz * 2) != Em)
			|| (world.getBlockId(x + dx * 2, y + 2, z + dz * 2) != Em)
			|| (world.getBlockId(x - dx, y, z - dz) != Em)
			|| (world.getBlockId(x - dx, y + 1, z - dz) != Em)
			|| (world.getBlockId(x - dx, y + 2, z - dz) != Em))
			return false;

		// Replace the outside frame with obsidian.  While the corner pieces
		// are not necessary to the check (similar to how a normal nether
		// portal works), replace them with obsidian if there is emerald there.
		for(int cx = -1; cx <= 2; cx++)
			for(int cy = -1; cy <= 3; cy++)
				for(int cz = -1; cz <= 2; cz++)
					{
					int nx = x + cx * dx;
					int ny = y + cy;
					int nz = z + cz * dz;
					if(world.getBlockId(nx, ny, nz) == Em)
						world.setBlockAndMetadata(nx, ny, nz, Block.obsidian.blockID, 0);
					}

		// Explosive consequences.
		world.newExplosion((Entity)null, x + world.rand.nextDouble() * dx * 2,
			y + world.rand.nextDouble() * 3,
			z + world.rand.nextDouble() * dz * 2,
			4.0F, true, true);

		// Ghastly consequences.
		int Success = 0;
		for(int Attempt = 0; Attempt < 25; Attempt++)
			{
			EntityGhast Ghast = new EntityGhast(world);
			Ghast.setLocationAndAngles(x + world.rand.nextDouble() * dx * 2 + world.rand.nextGaussian() * 5,
				y + world.rand.nextDouble() * 3 + world.rand.nextGaussian() * 5,
				z + world.rand.nextDouble() * dz * 2 + world.rand.nextGaussian() * 5,
				this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
			if(Ghast.getCanSpawnHere())
				{
				world.spawnEntityInWorld(Ghast);
				if(++Success >= 3)
					break;
				}
			}

		// Create a nether portal on the spot.
		Block.portal.tryToCreatePortal(world, x, y, z);

		return true;
		}

	// Called when a player right-clicks on the entity.
	public boolean interact(EntityPlayer player)
		{
		// If the player's currently-selected item while right-clicking is an arcane
		// scroll, and the villager is a Priest, and on fire, then try to create a
		// nether portal.
		ItemStack Tool = player.inventory.getCurrentItem();
		if((Tool != null) && (Tool.itemID == mod_FCBetterThanWolves.fcArcaneScroll.shiftedIndex)
			&& (this.getProfession() == 2) && this.isBurning())
			{
			int BX = MathHelper.floor_double(this.posX);
			int BY = MathHelper.floor_double(this.posY);
			int BZ = MathHelper.floor_double(this.posZ);

			// Search for each possible orientation of an emerald frame for
			// nether portal creation.  If one is found, destroy the villager.
			if(SearchPortal(this.worldObj, BX, BY, BZ, 0, 1)
				|| SearchPortal(this.worldObj, BX, BY, BZ, 0, -1)
				|| SearchPortal(this.worldObj, BX, BY, BZ, 1, 0)
				|| SearchPortal(this.worldObj, BX, BY, BZ, -1, 0)
				|| SearchPortal(this.worldObj, BX, BY - 1, BZ, 0, 1)
				|| SearchPortal(this.worldObj, BX, BY - 1, BZ, 0, -1)
				|| SearchPortal(this.worldObj, BX, BY - 1, BZ, 1, 0)
				|| SearchPortal(this.worldObj, BX, BY - 1, BZ, -1, 0))
				this.setDead();

			// Item was consumed.
			Tool.stackSize--;
			return true;
			}

		// Call base code if the nether portal creation checks did not pass.
		return super.interact(player);
		}
	}
