// ==========================================================================
// Copyright (C)2018 by Aaron Suen <warr1024@gmail.com>
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
	private static boolean[] tradingWhitelist;

	// Number of recipes that were rejected contraband and need to be
	// regenerated.
	private int contrabandCount;

	public BWREntityVillager(World world)
		{
		super(world);
		this.contrabandCount = 0;

		// If the trading whitelist is not initialized, initialize it.
		if(tradingWhitelist == null)
			{
			boolean[] wl = new boolean[Item.itemsList.length];

			// Villagers can buy items for emeralds.
			wl[Item.emerald.itemID] = true;

			// Villagers can sell only finished products, nothing
			// made of gold or diamond, and no raw materials.
			// All meats must be cooked (no tallow sources).
			wl[Item.shears.itemID] = true;
			wl[Item.shovelIron.itemID] = true;
			wl[Item.pickaxeIron.itemID] = true;
			wl[Item.axeIron.itemID] = true;
			wl[Item.flintAndSteel.itemID] = true;
			wl[Item.appleRed.itemID] = true;
			wl[Item.swordIron.itemID] = true;
			wl[Item.swordWood.itemID] = true;
			wl[Item.shovelWood.itemID] = true;
			wl[Item.pickaxeWood.itemID] = true;
			wl[Item.axeWood.itemID] = true;
			wl[Item.swordStone.itemID] = true;
			wl[Item.shovelStone.itemID] = true;
			wl[Item.pickaxeStone.itemID] = true;
			wl[Item.axeStone.itemID] = true;
			wl[Item.hoeWood.itemID] = true;
			wl[Item.hoeStone.itemID] = true;
			wl[Item.hoeIron.itemID] = true;
			wl[Item.bread.itemID] = true;
			wl[Item.helmetLeather.itemID] = true;
			wl[Item.plateLeather.itemID] = true;
			wl[Item.legsLeather.itemID] = true;
			wl[Item.bootsLeather.itemID] = true;
			wl[Item.helmetChain.itemID] = true;
			wl[Item.plateChain.itemID] = true;
			wl[Item.legsChain.itemID] = true;
			wl[Item.bootsChain.itemID] = true;
			wl[Item.helmetIron.itemID] = true;
			wl[Item.plateIron.itemID] = true;
			wl[Item.legsIron.itemID] = true;
			wl[Item.bootsIron.itemID] = true;
			wl[Item.saddle.itemID] = true;
			wl[Item.fishCooked.itemID] = true;
			wl[Item.chickenCooked.itemID] = true;
			wl[Item.expBottle.itemID] = true;

			tradingWhitelist = wl;
			}
		}

	// Save/load the number of trades still pending replacement.
	public void writeEntityToNBT(NBTTagCompound tag)
		{
		super.writeEntityToNBT(tag);
		tag.setInteger("Contraband", this.contrabandCount);
		}
 	public void readEntityFromNBT(NBTTagCompound tag)
		{
		super.readEntityFromNBT(tag);
		if(tag.hasKey("Contraband"))
			this.contrabandCount = tag.getInteger("Contraband");
		}

	// Called by entity AI subsystem.
	protected void updateAITick()
		{
		super.updateAITick();

		// Search the villager's trade list and remove any contraband
		// recipes.  Determine if there is at least one trade that is
		// not used.
		MerchantRecipeList merch = this.getRecipes(null);
		boolean hasTrade = false;
		for(int i = merch.size() - 1; i >= 0; i--)
			{
			MerchantRecipe mr = (MerchantRecipe)merch.get(i);
			if(!tradingWhitelist[mr.getItemToSell().itemID])
				{
				this.contrabandCount++;
				merch.remove(i);
				}
			}

		// If we've replaced all contraband trades already, bail.
		if(this.contrabandCount < 1)
			return;

		// If all trades are exhausted, normally Vanilla would create a new
		// trade, but it's possible that BWR blacklisted the trade, and the
		// villager is no longer offering any trades.  If this is the case,
		// then try to generate a replacement trade.
		if(!hasTrade)
			{
			// Create a surrogate villager as a duplicate of this one,
			// but with the trade array removed, so new trades will
			// generate on accessing the recipe list.  This is a workaround
			// for accessibility limitations of the trade list, without
			// having to call an entire AI cycle.
			NBTTagCompound tag = new NBTTagCompound();
			this.writeEntityToNBT(tag);
			tag.removeTag("Offers");
			EntityVillager surrogate = new EntityVillager(this.worldObj);
			surrogate.readEntityFromNBT(tag);

			// Loop through surrogate's now freshly-initialized recipe
			// list and remove any contraband.
			MerchantRecipeList smerch = surrogate.getRecipes(null);
			for(int i = smerch.size() - 1; i >= 0; i--)
				{
				MerchantRecipe mr = (MerchantRecipe)smerch.get(i);
				if(!tradingWhitelist[mr.getItemToSell().itemID])
					smerch.remove(i);
				}

			// Copy any valid trades from the surrogate back to the original.
			for(int i = smerch.size() - 1; i >= 0; i--)
				{
				merch.addToListWithCheck((MerchantRecipe)smerch.get(i));
				if((--this.contrabandCount) < 1)
					break;
				}

			surrogate.setDead();
			}
		}

	// Called by interact.  Search for an emerald portal frame given a specific location
	// and orientation, and if found, convert it to obsidian and create the nether portal.
	private boolean searchPortal(World world, int x, int y, int z, int dx, int dz)
		{
		int em = Block.blockEmerald.blockID;

		// Make sure the portal is filled with a 2x3 air space...
		if(!world.getBlockMaterial(x, y, z).isReplaceable()
			|| !world.getBlockMaterial(x + dx, y, z + dz).isReplaceable()
			|| !world.getBlockMaterial(x, y + 1, z).isReplaceable()
			|| !world.getBlockMaterial(x + dx, y + 1, z + dz).isReplaceable()
			|| !world.getBlockMaterial(x, y + 2, z).isReplaceable()
			|| !world.getBlockMaterial(x + dx, y + 2, z + dz).isReplaceable()

			// ...and the top and bottom frames are made of emerald block...
			|| (world.getBlockId(x, y - 1, z) != em)
			|| (world.getBlockId(x + dx, y - 1, z + dz) != em)
			|| (world.getBlockId(x, y + 3, z) != em)
			|| (world.getBlockId(x + dx, y + 3, z + dz) != em)

			// ...and the sides are also emerald block.
			|| (world.getBlockId(x + dx * 2, y, z + dz * 2) != em)
			|| (world.getBlockId(x + dx * 2, y + 1, z + dz * 2) != em)
			|| (world.getBlockId(x + dx * 2, y + 2, z + dz * 2) != em)
			|| (world.getBlockId(x - dx, y, z - dz) != em)
			|| (world.getBlockId(x - dx, y + 1, z - dz) != em)
			|| (world.getBlockId(x - dx, y + 2, z - dz) != em))
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
					if(world.getBlockId(nx, ny, nz) == em)
						world.setBlockAndMetadataWithNotify(nx, ny, nz, Block.obsidian.blockID, 0);
					}

		// Explosive consequences.
		world.newExplosion((Entity)null, x + world.rand.nextDouble() * dx * 2,
			y + world.rand.nextDouble() * 3,
			z + world.rand.nextDouble() * dz * 2,
			4.0F, true, true);

		// Ghastly consequences.
		int success = 0;
		for(int attempt = 0; attempt < 25; attempt++)
			{
			EntityGhast ghast = new EntityGhast(world);
			ghast.setLocationAndAngles(x + world.rand.nextDouble() * dx * 2 + world.rand.nextGaussian() * 5,
				y + world.rand.nextDouble() * 3 + world.rand.nextGaussian() * 5,
				z + world.rand.nextDouble() * dz * 2 + world.rand.nextGaussian() * 5,
				this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
			if(ghast.getCanSpawnHere())
				{
				world.spawnEntityInWorld(ghast);
				if(++success >= 3)
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
		ItemStack tool = player.inventory.getCurrentItem();
		if((tool != null) && (tool.itemID == FCBetterThanWolves.fcArcaneScroll.itemID)
			&& (this.getProfession() == 2) && this.isBurning())
			{
			int bx = MathHelper.floor_double(this.posX);
			int by = MathHelper.floor_double(this.posY);
			int bz = MathHelper.floor_double(this.posZ);

			// Search for each possible orientation of an emerald frame for
			// nether portal creation.  If one is found, destroy the villager.
			if(searchPortal(this.worldObj, bx, by, bz, 0, 1)
				|| searchPortal(this.worldObj, bx, by, bz, 0, -1)
				|| searchPortal(this.worldObj, bx, by, bz, 1, 0)
				|| searchPortal(this.worldObj, bx, by, bz, -1, 0)
				|| searchPortal(this.worldObj, bx, by - 1, bz, 0, 1)
				|| searchPortal(this.worldObj, bx, by - 1, bz, 0, -1)
				|| searchPortal(this.worldObj, bx, by - 1, bz, 1, 0)
				|| searchPortal(this.worldObj, bx, by - 1, bz, -1, 0))
				this.setDead();

			// Item was consumed.
			tool.stackSize--;
			return true;
			}

		// Call base code if the nether portal creation checks did not pass.
		return super.interact(player);
		}
	}
