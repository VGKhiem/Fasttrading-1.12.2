package com.dazo66.fasttrading.config;

import com.dazo66.fasttrading.FastTrading;
import com.dazo66.fasttrading.util.DazoUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;

public class ConfigJson implements Cloneable {
   public boolean isAuto;
   SimpleRecipe[] recipeList;

   public ConfigJson(boolean isAuto, SimpleRecipe[] recipeList) {
      this.isAuto = isAuto;
      this.recipeList = recipeList;
   }

   public static SimpleRecipe isContain(MerchantRecipe recipe, List<SimpleRecipe> simpleRecipes) {
      for(SimpleRecipe simpleRecipe : simpleRecipes) {
         SimpleRecipe simpleRecipe1 = isRecipeEqual(recipe, simpleRecipe);
         if (null != simpleRecipe1) {
            return simpleRecipe1;
         }
      }

      return null;
   }

   public static SimpleRecipe isRecipeEqual(MerchantRecipe recipe, SimpleRecipe simpleRecipe) {
      ItemStack buy11 = simpleRecipe.buy1.getItemStack();
      ItemStack buy22 = simpleRecipe.buy2.getItemStack();
      ItemStack sell1 = simpleRecipe.sell.getItemStack();
      if (!DazoUtils.areItemEqualIgnoreCount(buy11, recipe.getItemToBuy())) {
         return null;
      } else if (!DazoUtils.areItemEqualIgnoreCount(sell1, recipe.getItemToSell())) {
         return null;
      } else {
         return recipe.hasSecondItemToBuy() && !DazoUtils.areItemEqualIgnoreCount(buy22, recipe.getSecondItemToBuy()) ? null : simpleRecipe;
      }
   }

   public static boolean isSamePrice(MerchantRecipe recipe, SimpleRecipe simpleRecipe) {
      ItemStack buy11 = simpleRecipe.buy1.getItemStack();
      ItemStack buy22 = simpleRecipe.buy2.getItemStack();
      ItemStack sell1 = simpleRecipe.sell.getItemStack();
      if (simpleRecipe.lockPrice) {
         if (buy11.getCount() != recipe.getItemToBuy().getCount()) {
            return false;
         }

         if (sell1.getCount() != recipe.getItemToSell().getCount()) {
            return false;
         }
      } else if (recipe.hasSecondItemToBuy()) {
         return buy22.getCount() == recipe.getSecondItemToBuy().getCount();
      }

      return true;
   }

   public void setAuto(boolean auto) {
      Minecraft mc = Minecraft.getMinecraft();
      this.isAuto = auto;
      String msg = "FastTradingMod-" + (auto ? "ON" : "OFF");

      try {
         mc.player.sendMessage(new TextComponentString(msg));
      } catch (NullPointerException var5) {
         FastTrading.logger.info(msg);
      }

   }

   public boolean equals(Object o) {
      return o instanceof ConfigJson && ((ConfigJson)o).isAuto == this.isAuto && Arrays.equals(((ConfigJson)o).recipeList, this.recipeList);
   }

   public Object clone() {
      Object clone = null;

      try {
         clone = super.clone();
      } catch (CloneNotSupportedException e) {
         e.printStackTrace();
      }

      return clone;
   }

   public static class SimpleRecipe {
      public boolean lockPrice;
      SimpleItem buy1;
      SimpleItem buy2;
      SimpleItem sell;

      public SimpleRecipe(boolean lockPrice, MerchantRecipe recipe) {
         this.lockPrice = lockPrice;
         this.buy1 = new SimpleItem(recipe.getItemToBuy());
         this.buy2 = new SimpleItem(recipe.getSecondItemToBuy());
         this.sell = new SimpleItem(recipe.getItemToSell());
      }

      public SimpleRecipe setLockPrice(boolean lockPrice) {
         this.lockPrice = lockPrice;
         return this;
      }

      private class SimpleItem {
         private String itemID;
         private String nbt;

         private SimpleItem(ItemStack itemStack) {
            this.itemID = itemStack.getItem().getRegistryName().toString();
            this.nbt = this.nbtToJson(itemStack.serializeNBT());
         }

         private String nbtToJson(NBTTagCompound nbt) {
            StringBuilder stringBuilder = new StringBuilder("{");

            for(String s : nbt.getKeySet()) {
               if (stringBuilder.length() != 1) {
                  stringBuilder.append(',');
               }

               stringBuilder.append(s).append(':').append(nbt.getTag(s));
            }

            return stringBuilder.append('}').toString();
         }

         private ItemStack getItemStack() {
            try {
               return new ItemStack(JsonToNBT.getTagFromJson(this.nbt));
            } catch (NBTException e) {
               e.printStackTrace();
               return ItemStack.EMPTY;
            }
         }
      }
   }
}
