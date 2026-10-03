package com.vgkhiem.fasttrading.util;

import com.vgkhiem.fasttrading.FastTrading;
import com.vgkhiem.fasttrading.client.gui.GuiMerchantOverride;
import com.vgkhiem.fasttrading.config.ConfigJson;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class TradingHelper {
   public HashMap<MerchantRecipe, ConfigJson.SimpleRecipe> map = new HashMap<>();
   private Minecraft mc;
   private Container inventorySlots;
   private GuiMerchantOverride gui;
   private Slot buy1;
   private Slot buy2;
   private Slot sell;

   public TradingHelper(GuiMerchantOverride guiIn) {
      this.gui = guiIn;
      this.inventorySlots = this.gui.inventorySlots;
      this.buy1 = this.inventorySlots.getSlot(0);
      this.buy2 = this.inventorySlots.getSlot(1);
      this.sell = this.inventorySlots.getSlot(2);
      this.mc = Minecraft.getMinecraft();
   }

   public void init(MerchantRecipeList list) {
      for(MerchantRecipe recipe : list) {
         this.map.put(recipe, ConfigJson.isContain(recipe, FastTrading.configLoader.recipeList));
      }

      if (FastTrading.configLoader.config.isAuto) {
         this.trading(list);
      }
   }

   protected Slot findItem(ItemStack itemStack, int minCount, Slot excludeSlot) {
      if (itemStack == null || itemStack.isEmpty()) {
         return null;
      }
      List<Slot> slots = this.inventorySlots.inventorySlots;
      Slot lastHave = null;
      for (int i = 3; i < slots.size(); i++) {
         Slot slot = slots.get(i);
         if (excludeSlot != null && slot.slotNumber == excludeSlot.slotNumber) {
            continue;
         }
         ItemStack temp = slot.getStack();
         if (VGKhiemUtils.areItemEqualIgnoreCount(temp, itemStack)) {
            if (temp.getCount() >= minCount) {
               return slot;
            }
            lastHave = slot;
         }
      }

      if (lastHave != null) {
         this.gui.click(lastHave, 0, ClickType.PICKUP);
         this.gui.click(lastHave, 0, ClickType.PICKUP_ALL);
         this.gui.click(lastHave, 0, ClickType.PICKUP);
         if (lastHave.getStack().getCount() >= minCount) {
            return lastHave;
         }
      }

      return null;
   }

   private boolean prepareInputs(MerchantRecipe recipe, boolean tradeAll) {
      this.clearSlot(this.buy1, this.buy2);
      if (!this.mc.player.inventory.getItemStack().isEmpty()) {
         return false;
      }

      ItemStack buy1Item = recipe.getItemToBuy();
      if (buy1Item == null || buy1Item.isEmpty()) {
         return false;
      }
      boolean hasBuy2 = recipe.hasSecondItemToBuy();
      ItemStack buy2Item = hasBuy2 ? recipe.getSecondItemToBuy() : ItemStack.EMPTY;
      boolean sameItem = hasBuy2 && VGKhiemUtils.areItemEqualIgnoreCount(buy1Item, buy2Item);

      if (!hasBuy2) {
         Slot slot1 = this.findItem(buy1Item, buy1Item.getCount(), null);
         if (slot1 == null) {
            return false;
         }
         if (tradeAll) {
            this.moveItem(slot1, this.buy1);
         } else {
            this.moveExactItem(slot1, this.buy1, buy1Item.getCount());
         }
         return true;
      }

      if (!sameItem) {
         Slot slot1 = this.findItem(buy1Item, buy1Item.getCount(), null);
         if (slot1 == null) {
            return false;
         }
         Slot slot2 = this.findItem(buy2Item, buy2Item.getCount(), null);
         if (slot2 == null) {
            return false;
         }

         if (tradeAll) {
            this.moveItem(slot1, this.buy1);
            this.moveItem(slot2, this.buy2);
         } else {
            this.moveExactItem(slot1, this.buy1, buy1Item.getCount());
            this.moveExactItem(slot2, this.buy2, buy2Item.getCount());
         }
         return true;
      }

      int req1 = buy1Item.getCount();
      int req2 = buy2Item.getCount();

      Slot slot1 = this.findItem(buy1Item, req1, null);
      if (slot1 == null) {
         return false;
      }

      Slot slot2 = this.findItem(buy2Item, req2, slot1);
      if (slot2 != null) {
         if (tradeAll) {
            this.moveItem(slot1, this.buy1);
            this.moveItem(slot2, this.buy2);
         } else {
            this.moveExactItem(slot1, this.buy1, req1);
            this.moveExactItem(slot2, this.buy2, req2);
         }
         return true;
      }

      this.gui.click(slot1, 0, ClickType.PICKUP);
      this.gui.click(slot1, 0, ClickType.PICKUP_ALL);
      this.gui.click(slot1, 0, ClickType.PICKUP);

      int totalAvailable = slot1.getStack().getCount();
      if (totalAvailable < req1 + req2) {
         return false;
      }

      if (!tradeAll) {
         this.moveExactToBoth(slot1, this.buy1, req1, this.buy2, req2);
      } else {
         int maxTrades = totalAvailable / (req1 + req2);
         int count2 = Math.min(64, maxTrades * req2);
         this.moveSplitToBoth(slot1, this.buy1, this.buy2, count2);
      }
      return true;
   }

   public void trading(MerchantRecipeList list) {
      int i = 0;
      for(MerchantRecipe recipe : list) {
         ConfigJson.SimpleRecipe simpleRecipe = this.map.get(recipe);
         if (null != simpleRecipe) {
            if (simpleRecipe.lockPrice) {
               if (ConfigJson.isSamePrice(recipe, simpleRecipe)) {
                  this.trading(recipe, i);
               }
            } else {
               this.trading(recipe, i);
            }
         }
         ++i;
      }
   }

   public void trading(MerchantRecipe recipe, int index) {
      if (recipe == null || recipe.isRecipeDisabled()) {
         return;
      }
      this.gui.setCurrentRecipe(index);
      int safetyLimit = 0;
      while (!recipe.isRecipeDisabled() && safetyLimit++ < 64) {
         if (!this.prepareInputs(recipe, true)) {
            this.clearSlot(this.buy1, this.buy2);
            return;
         }

         if (!this.sell.getHasStack()) {
            this.clearSlot(this.buy1, this.buy2);
            return;
         }

         this.gui.click(this.sell, 0, ClickType.QUICK_MOVE);
         this.clearSlot(this.buy1, this.buy2);
      }
      this.clearSlot(this.buy1, this.buy2);
   }

   public void tradingOnce(MerchantRecipe recipe, int index) {
      if (recipe == null || recipe.isRecipeDisabled()) {
         return;
      }
      this.gui.setCurrentRecipe(index);
      if (!this.prepareInputs(recipe, false)) {
         this.clearSlot(this.buy1, this.buy2);
         return;
      }

      if (this.sell.getHasStack()) {
         this.gui.click(this.sell, 0, ClickType.QUICK_MOVE);
      }
      this.clearSlot(this.buy1, this.buy2);
   }

   private void clearSlot(Slot... slots) {
      for(Slot slot : slots) {
         if (slot != null && slot.getHasStack()) {
            this.gui.click(slot, 0, ClickType.QUICK_MOVE);
         }
      }
      if (!this.mc.player.inventory.getItemStack().isEmpty()) {
         List<Slot> invSlots = this.inventorySlots.inventorySlots;
         for (int i = 3; i < invSlots.size(); i++) {
            if (!invSlots.get(i).getHasStack()) {
               this.gui.click(invSlots.get(i), 0, ClickType.PICKUP);
               break;
            }
         }
      }
   }

   private void moveItem(Slot from, Slot to) {
      if (from == null || to == null) return;
      this.gui.click(from, 0, ClickType.PICKUP);
      this.gui.click(to, 0, ClickType.PICKUP);
      if (!this.mc.player.inventory.getItemStack().isEmpty()) {
         this.gui.click(from, 0, ClickType.PICKUP);
      }
   }

   private void moveExactItem(Slot from, Slot to, int count) {
      if (from == null || to == null) return;
      this.gui.click(from, 0, ClickType.PICKUP);
      for (int i = 0; i < count; i++) {
         this.gui.click(to, 1, ClickType.PICKUP);
      }
      if (!this.mc.player.inventory.getItemStack().isEmpty()) {
         this.gui.click(from, 0, ClickType.PICKUP);
      }
   }

   private void moveExactToBoth(Slot from, Slot to1, int count1, Slot to2, int count2) {
      if (from == null || to1 == null || to2 == null) return;
      this.gui.click(from, 0, ClickType.PICKUP);
      for (int i = 0; i < count2; i++) {
         this.gui.click(to2, 1, ClickType.PICKUP);
      }
      for (int i = 0; i < count1; i++) {
         this.gui.click(to1, 1, ClickType.PICKUP);
      }
      if (!this.mc.player.inventory.getItemStack().isEmpty()) {
         this.gui.click(from, 0, ClickType.PICKUP);
      }
   }

   private void moveSplitToBoth(Slot from, Slot to1, Slot to2, int countForTo2) {
      if (from == null || to1 == null || to2 == null) return;
      this.gui.click(from, 0, ClickType.PICKUP);
      for (int i = 0; i < countForTo2; i++) {
         this.gui.click(to2, 1, ClickType.PICKUP);
      }
      this.gui.click(to1, 0, ClickType.PICKUP);
      if (!this.mc.player.inventory.getItemStack().isEmpty()) {
         this.gui.click(from, 0, ClickType.PICKUP);
      }
   }
}
