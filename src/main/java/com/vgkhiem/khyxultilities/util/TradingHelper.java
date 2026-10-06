package com.vgkhiem.khyxultilities.util;

import com.vgkhiem.khyxultilities.FastTrading;
import com.vgkhiem.khyxultilities.client.audio.FakeSubtitleSound;
import com.vgkhiem.khyxultilities.client.gui.GuiMerchantOverride;
import com.vgkhiem.khyxultilities.config.ConfigJson;
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

   public void updateMap(MerchantRecipeList list) {
      if (list == null) return;
      for(MerchantRecipe recipe : list) {
         this.map.put(recipe, ConfigJson.isContain(recipe, FastTrading.configLoader.recipeList));
      }
   }

   public void init(MerchantRecipeList list) {
      this.updateMap(list);

      if (FastTrading.configLoader.config.isAuto) {
         this.gui.startNonBlockingTrading(-1, true);
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

      if (lastHave != null && (excludeSlot == null || !VGKhiemUtils.areItemEqualIgnoreCount(excludeSlot.getStack(), itemStack))) {
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
            this.moveExactCount(slot1, this.buy1, buy1Item.getCount());
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
            this.moveExactCount(slot1, this.buy1, buy1Item.getCount());
            this.moveExactCount(slot2, this.buy2, buy2Item.getCount());
         }
         return true;
      }

      return this.prepareSameInputs(buy1Item, buy1Item.getCount(), buy2Item.getCount(), tradeAll);
   }

   public int trading(MerchantRecipeList list) {
      int totalTrades = 0;
      int i = 0;
      for(MerchantRecipe recipe : list) {
         ConfigJson.SimpleRecipe simpleRecipe = this.map.get(recipe);
         if (null != simpleRecipe) {
            if (simpleRecipe.lockPrice) {
               if (ConfigJson.isSamePrice(recipe, simpleRecipe)) {
                  totalTrades += this.trading(recipe, i);
               }
            } else {
               totalTrades += this.trading(recipe, i);
            }
         }
         ++i;
      }
      return totalTrades;
   }

   public int trading(MerchantRecipe recipe, int index) {
      if (recipe == null || recipe.isRecipeDisabled()) {
         return 0;
      }
      this.gui.setCurrentRecipe(index);
      int tradeCount = 0;
      int safetyLimit = 0;
      while (!recipe.isRecipeDisabled() && safetyLimit++ < 64) {
         if (!this.prepareInputs(recipe, true)) {
            this.clearSlot(this.buy1, this.buy2);
            return tradeCount;
         }

         if (!this.sell.getHasStack()) {
            this.clearSlot(this.buy1, this.buy2);
            return tradeCount;
         }

         int buy1Before = this.buy1.getHasStack() ? this.buy1.getStack().getCount() : 0;
         int buy2Before = this.buy2.getHasStack() ? this.buy2.getStack().getCount() : 0;

         this.gui.click(this.sell, 0, ClickType.QUICK_MOVE);

         int buy1After = this.buy1.getHasStack() ? this.buy1.getStack().getCount() : 0;
         int buy2After = this.buy2.getHasStack() ? this.buy2.getStack().getCount() : 0;

         boolean consumed = (buy1Before != buy1After) || (buy2Before != buy2After) || (!this.sell.getHasStack());

         this.clearSlot(this.buy1, this.buy2);
         tradeCount++;

         if (!consumed) {
            break;
         }
      }
      this.clearSlot(this.buy1, this.buy2);
      return tradeCount;
   }

   public int tradingOnce(MerchantRecipe recipe, int index) {
      if (recipe == null || recipe.isRecipeDisabled()) {
         return 0;
      }
      this.gui.setCurrentRecipe(index);
      if (!this.prepareInputs(recipe, false)) {
         this.clearSlot(this.buy1, this.buy2);
         return 0;
      }

      int buy1Before = this.buy1.getHasStack() ? this.buy1.getStack().getCount() : 0;
      int buy2Before = this.buy2.getHasStack() ? this.buy2.getStack().getCount() : 0;

      if (this.sell.getHasStack()) {
         this.gui.click(this.sell, 0, ClickType.QUICK_MOVE);
         int buy1After = this.buy1.getHasStack() ? this.buy1.getStack().getCount() : 0;
         int buy2After = this.buy2.getHasStack() ? this.buy2.getStack().getCount() : 0;
         boolean consumed = (buy1Before != buy1After) || (buy2Before != buy2After) || (!this.sell.getHasStack());

         this.clearSlot(this.buy1, this.buy2);
         if (consumed) {
            return 1;
         }
         return 0;
      }
      this.clearSlot(this.buy1, this.buy2);
      return 0;
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

   private boolean prepareSameInputs(ItemStack itemToBuy, int req1, int req2, boolean tradeAll) {
      int totalReq = req1 + req2;
      List<Slot> slots = this.inventorySlots.inventorySlots;
      int totalAvailable = 0;
      for (int i = 3; i < slots.size(); i++) {
         ItemStack stack = slots.get(i).getStack();
         if (VGKhiemUtils.areItemEqualIgnoreCount(stack, itemToBuy)) {
            totalAvailable += stack.getCount();
         }
      }

      if (totalAvailable < totalReq) {
         return false;
      }

      int maxStack = itemToBuy.getMaxStackSize();
      int toBuy1 = req1;
      int toBuy2 = req2;

      if (tradeAll) {
         int candidateT = 0;

         for (int i = 3; i < slots.size(); i++) {
            Slot s = slots.get(i);
            if (s.getHasStack() && VGKhiemUtils.areItemEqualIgnoreCount(s.getStack(), itemToBuy)) {
               int t = s.getStack().getCount() / totalReq;
               if (t > candidateT) {
                  candidateT = t;
               }
            }
         }

         for (int i = 3; i < slots.size(); i++) {
            Slot a = slots.get(i);
            if (a.getHasStack() && VGKhiemUtils.areItemEqualIgnoreCount(a.getStack(), itemToBuy)) {
               for (int j = 3; j < slots.size(); j++) {
                  if (i == j) continue;
                  Slot b = slots.get(j);
                  if (b.getHasStack() && VGKhiemUtils.areItemEqualIgnoreCount(b.getStack(), itemToBuy)) {
                     int t = Math.min(a.getStack().getCount() / req1, b.getStack().getCount() / req2);
                     if (t > candidateT) {
                        candidateT = t;
                     }
                  }
               }
            }
         }

         int maxTrades = Math.min(totalAvailable / totalReq, Math.min(maxStack / req1, maxStack / req2));
         if (candidateT > 0) {
            candidateT = Math.min(candidateT, maxTrades);
         } else {
            candidateT = 1;
         }

         toBuy1 = candidateT * req1;
         toBuy2 = candidateT * req2;
      }

      Slot singleSlot = null;
      for (int i = 3; i < slots.size(); i++) {
         Slot s = slots.get(i);
         if (s.getHasStack() && VGKhiemUtils.areItemEqualIgnoreCount(s.getStack(), itemToBuy) && s.getStack().getCount() >= toBuy1 + toBuy2) {
            singleSlot = s;
            break;
         }
      }

      if (singleSlot != null) {
         this.splitSlotToBoth(singleSlot, this.buy1, toBuy1, this.buy2, toBuy2);
         return true;
      }

      Slot s1 = null;
      Slot s2 = null;
      for (int i = 3; i < slots.size(); i++) {
         Slot a = slots.get(i);
         if (a.getHasStack() && VGKhiemUtils.areItemEqualIgnoreCount(a.getStack(), itemToBuy) && a.getStack().getCount() >= toBuy1) {
            for (int j = 3; j < slots.size(); j++) {
               if (i == j) continue;
               Slot b = slots.get(j);
               if (b.getHasStack() && VGKhiemUtils.areItemEqualIgnoreCount(b.getStack(), itemToBuy) && b.getStack().getCount() >= toBuy2) {
                  s1 = a;
                  s2 = b;
                  break;
               }
            }
            if (s1 != null && s2 != null) break;
         }
      }

      if (s1 != null && s2 != null) {
         this.moveExactCount(s1, this.buy1, toBuy1);
         this.moveExactCount(s2, this.buy2, toBuy2);
         return true;
      }

      Slot firstSlot = null;
      for (int i = 3; i < slots.size(); i++) {
         Slot s = slots.get(i);
         if (s.getHasStack() && VGKhiemUtils.areItemEqualIgnoreCount(s.getStack(), itemToBuy)) {
            firstSlot = s;
            break;
         }
      }

      if (firstSlot != null) {
         this.gui.click(firstSlot, 0, ClickType.PICKUP);
         this.gui.click(firstSlot, 0, ClickType.PICKUP_ALL);
         this.gui.click(firstSlot, 0, ClickType.PICKUP);

         if (firstSlot.getStack().getCount() < toBuy1 + toBuy2 && firstSlot.getStack().getCount() >= totalReq) {
            toBuy1 = req1;
            toBuy2 = req2;
         }

         if (firstSlot.getStack().getCount() >= toBuy1 + toBuy2) {
            this.splitSlotToBoth(firstSlot, this.buy1, toBuy1, this.buy2, toBuy2);
            return true;
         }

         for (int i = 3; i < slots.size(); i++) {
            Slot a = slots.get(i);
            if (a.getHasStack() && VGKhiemUtils.areItemEqualIgnoreCount(a.getStack(), itemToBuy) && a.getStack().getCount() >= toBuy1) {
               for (int j = 3; j < slots.size(); j++) {
                  if (i == j) continue;
                  Slot b = slots.get(j);
                  if (b.getHasStack() && VGKhiemUtils.areItemEqualIgnoreCount(b.getStack(), itemToBuy) && b.getStack().getCount() >= toBuy2) {
                     this.moveExactCount(a, this.buy1, toBuy1);
                     this.moveExactCount(b, this.buy2, toBuy2);
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private void moveExactCount(Slot from, Slot to, int count) {
      if (from == null || to == null || count <= 0) return;
      ItemStack stack = from.getStack();
      if (stack.isEmpty()) return;

      if (stack.getCount() == count) {
         this.moveItem(from, to);
         return;
      }

      if (count == (stack.getCount() + 1) / 2) {
         this.gui.click(from, 1, ClickType.PICKUP);
         this.gui.click(to, 0, ClickType.PICKUP);
         return;
      }

      this.gui.click(from, 0, ClickType.PICKUP);
      for (int i = 0; i < count; i++) {
         this.gui.click(to, 1, ClickType.PICKUP);
      }
      if (!this.mc.player.inventory.getItemStack().isEmpty()) {
         this.gui.click(from, 0, ClickType.PICKUP);
      }
   }

   private void splitSlotToBoth(Slot from, Slot to1, int count1, Slot to2, int count2) {
      if (from == null || to1 == null || to2 == null) return;
      ItemStack stack = from.getStack();
      if (stack.isEmpty()) return;

      int total = stack.getCount();
      if (count1 == count2 && count1 + count2 == total) {
         this.gui.click(from, 1, ClickType.PICKUP);
         this.gui.click(to2, 0, ClickType.PICKUP);
         this.gui.click(from, 0, ClickType.PICKUP);
         this.gui.click(to1, 0, ClickType.PICKUP);
         return;
      }

      this.gui.click(from, 0, ClickType.PICKUP);
      for (int i = 0; i < count2; i++) {
         this.gui.click(to2, 1, ClickType.PICKUP);
      }
      if (this.mc.player.inventory.getItemStack().getCount() == count1) {
         this.gui.click(to1, 0, ClickType.PICKUP);
      } else {
         for (int i = 0; i < count1; i++) {
            this.gui.click(to1, 1, ClickType.PICKUP);
         }
         if (!this.mc.player.inventory.getItemStack().isEmpty()) {
            this.gui.click(from, 0, ClickType.PICKUP);
         }
      }
   }
}
