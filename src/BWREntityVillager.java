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

public class BWREntityVillager extends EntityVillager
	{
	private static boolean[] TradingWhitelist;

	public BWREntityVillager(World world)
		{
		super(world);

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
			WL[Item.porkCooked.shiftedIndex] = true;
			WL[Item.saddle.shiftedIndex] = true;
			WL[Item.fishCooked.shiftedIndex] = true;
			WL[Item.beefCooked.shiftedIndex] = true;
			WL[Item.chickenCooked.shiftedIndex] = true;
			WL[Item.expBottle.shiftedIndex] = true;

			TradingWhitelist = WL;
			}
		}

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
			Tag.func_82580_o("Offers");
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
			}
		}
	}
