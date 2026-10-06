package com.vgkhiem.khyxultilities.util;

import com.vgkhiem.khyxultilities.config.FastCraftConfig;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class FastCraftHelper {

   public static class CraftableRecipe {
      public final IRecipe recipe;
      public final ItemStack output;
      public final int craftableCount;
      public final int totalOutputCount;
      public final String displayName;

      public CraftableRecipe(IRecipe recipe, ItemStack output, int craftableCount) {
         this.recipe = recipe;
         this.output = output.copy();
         this.craftableCount = craftableCount;
         this.totalOutputCount = craftableCount * Math.max(1, output.getCount());
         this.displayName = output.getDisplayName();
      }
   }

   public static boolean isApplicable(Object gui) {
      return (gui instanceof GuiCrafting) || (gui instanceof GuiInventory);
   }

   public static int getGridSize(GuiContainer gui) {
      return (gui instanceof GuiCrafting) ? 3 : 2;
   }

   public static int getFirstCraftSlot(GuiContainer gui) {
      return 1;
   }

   public static int getResultSlot(GuiContainer gui) {
      return 0;
   }

   public static int getFirstInventorySlot(GuiContainer gui) {
      return (gui instanceof GuiCrafting) ? 10 : 9;
   }

   public static int getRecipeWidth(IRecipe recipe) {
      if (recipe instanceof ShapedRecipes) {
         return ((ShapedRecipes) recipe).recipeWidth;
      }
      if (recipe instanceof IShapedRecipe) {
         return ((IShapedRecipe) recipe).getRecipeWidth();
      }
      try {
         Method m = recipe.getClass().getMethod("getWidth");
         return (Integer) m.invoke(recipe);
      } catch (Exception ignored) {
      }
      try {
         Method m = recipe.getClass().getMethod("getRecipeWidth");
         return (Integer) m.invoke(recipe);
      } catch (Exception ignored) {
      }
      return -1;
   }

   public static int getRecipeHeight(IRecipe recipe) {
      if (recipe instanceof ShapedRecipes) {
         return ((ShapedRecipes) recipe).recipeHeight;
      }
      if (recipe instanceof IShapedRecipe) {
         return ((IShapedRecipe) recipe).getRecipeHeight();
      }
      try {
         Method m = recipe.getClass().getMethod("getHeight");
         return (Integer) m.invoke(recipe);
      } catch (Exception ignored) {
      }
      try {
         Method m = recipe.getClass().getMethod("getRecipeHeight");
         return (Integer) m.invoke(recipe);
      } catch (Exception ignored) {
      }
      return -1;
   }

   public static String getRecipeKey(IRecipe recipe) {
      if (recipe == null) {
         return "";
      }
      if (recipe.getRegistryName() != null) {
         return recipe.getRegistryName().toString();
      }
      ItemStack out = recipe.getRecipeOutput();
      if (out != null && !out.isEmpty() && out.getItem().getRegistryName() != null) {
         return out.getItem().getRegistryName().toString() + ":" + out.getMetadata();
      }
      return recipe.toString();
   }

   public static boolean isAutoCraft(IRecipe recipe) {
      if (recipe == null) {
         return false;
      }
      List<String> list = FastCraftConfig.get().autoCraftRecipes;
      if (list == null || list.isEmpty()) {
         return false;
      }
      return list.contains(getRecipeKey(recipe));
   }

   public static boolean toggleAutoCraft(IRecipe recipe) {
      if (recipe == null) {
         return false;
      }
      FastCraftConfig config = FastCraftConfig.get();
      if (config.autoCraftRecipes == null) {
         config.autoCraftRecipes = new ArrayList<>();
      }
      String key = getRecipeKey(recipe);
      boolean added;
      if (config.autoCraftRecipes.contains(key)) {
         config.autoCraftRecipes.remove(key);
         added = false;
      } else {
         config.autoCraftRecipes.add(key);
         added = true;
      }
      FastCraftConfig.save(config);
      return added;
   }

   public static void performAutoCraft(GuiContainer gui) {
      if (!isApplicable(gui)) {
         return;
      }
      FastCraftConfig config = FastCraftConfig.get();
      if (!config.isAutoCraftEnabled()) {
         return;
      }
      List<String> autoList = config.autoCraftRecipes;
      if (autoList == null || autoList.isEmpty()) {
         return;
      }

      int maxRounds = 36;
      for (int round = 0; round < maxRounds; round++) {
         List<CraftableRecipe> craftable = findCraftableRecipes(gui);
         CraftableRecipe toCraft = null;
         for (CraftableRecipe cr : craftable) {
            if (isAutoCraft(cr.recipe)) {
               toCraft = cr;
               break;
            }
         }
         if (toCraft == null) {
            break;
         }
         craftRecipe(gui, toCraft, true);
      }
   }

   public static List<CraftableRecipe> findAllDisplayRecipes(GuiContainer gui) {
      List<CraftableRecipe> result = new ArrayList<>();
      if (!isApplicable(gui)) {
         return result;
      }

      int gridSize = getGridSize(gui);
      List<CraftableRecipe> available = findCraftableRecipes(gui);
      Map<String, CraftableRecipe> availableMap = new LinkedHashMap<>();
      for (CraftableRecipe cr : available) {
         availableMap.put(getRecipeKey(cr.recipe), cr);
      }

      List<CraftableRecipe> autoList = new ArrayList<>();
      Set<String> autoKeys = new HashSet<>();

      if (FastCraftConfig.get().autoCraftRecipes != null) {
         for (Object obj : CraftingManager.REGISTRY) {
            if (!(obj instanceof IRecipe)) {
               continue;
            }
            IRecipe recipe = (IRecipe) obj;
            if (!isAutoCraft(recipe)) {
               continue;
            }
            if (!recipe.canFit(gridSize, gridSize)) {
               continue;
            }

            String key = getRecipeKey(recipe);
            autoKeys.add(key);
            if (availableMap.containsKey(key)) {
               autoList.add(availableMap.get(key));
            } else {
               ItemStack out = recipe.getRecipeOutput();
               if (out != null && !out.isEmpty() && out.getItem() != Items.AIR) {
                  autoList.add(new CraftableRecipe(recipe, out, 0));
               }
            }
         }

         autoList.sort(Comparator.comparing((CraftableRecipe a) -> a.displayName.toLowerCase())
            .thenComparing(a -> a.output.getItem().getRegistryName() != null ? a.output.getItem().getRegistryName().toString() : ""));
      }

      result.addAll(autoList);

      for (CraftableRecipe cr : available) {
         if (!autoKeys.contains(getRecipeKey(cr.recipe))) {
            result.add(cr);
         }
      }

      return result;
   }

   public static List<CraftableRecipe> findCraftableRecipes(GuiContainer gui) {
      List<CraftableRecipe> result = new ArrayList<>();
      if (!isApplicable(gui)) {
         return result;
      }

      int gridSize = getGridSize(gui);
      int firstInvSlot = getFirstInventorySlot(gui);

      ItemStack[] invStacks = new ItemStack[36];
      int[] baseCounts = new int[36];
      for (int i = 0; i < 36; i++) {
         Slot slot = gui.inventorySlots.getSlot(firstInvSlot + i);
         if (slot != null && slot.getHasStack()) {
            invStacks[i] = slot.getStack();
            baseCounts[i] = slot.getStack().getCount();
         } else {
            invStacks[i] = ItemStack.EMPTY;
            baseCounts[i] = 0;
         }
      }

      for (Object obj : CraftingManager.REGISTRY) {
         if (!(obj instanceof IRecipe)) {
            continue;
         }
         IRecipe recipe = (IRecipe) obj;
         ItemStack out = recipe.getRecipeOutput();
         if (out == null || out.isEmpty() || out.getItem() == Items.AIR) {
            continue;
         }
         if (!recipe.canFit(gridSize, gridSize)) {
            continue;
         }

         NonNullList<Ingredient> ingredients = recipe.getIngredients();
         List<Ingredient> activeIngredients = new ArrayList<>();
         for (Ingredient ing : ingredients) {
            if (ing != Ingredient.EMPTY && ing.getMatchingStacks().length > 0) {
               activeIngredients.add(ing);
            }
         }

         if (activeIngredients.isEmpty()) {
            continue;
         }

         int[] counts = baseCounts.clone();
         int craftCount = 0;
         int maxStack = Math.max(1, out.getMaxStackSize());

         while (craftCount < maxStack) {
            boolean possible = true;
            for (Ingredient ing : activeIngredients) {
               boolean matched = false;
               for (int s = 0; s < 36; s++) {
                  if (counts[s] > 0 && ing.apply(invStacks[s])) {
                     counts[s]--;
                     matched = true;
                     break;
                  }
               }
               if (!matched) {
                  possible = false;
                  break;
               }
            }
            if (possible) {
               craftCount++;
            } else {
               break;
            }
         }

         if (craftCount > 0) {
            result.add(new CraftableRecipe(recipe, out, craftCount));
         }
      }

      result.sort(Comparator.comparing((CraftableRecipe a) -> a.displayName.toLowerCase())
         .thenComparing(a -> a.output.getItem().getRegistryName() != null ? a.output.getItem().getRegistryName().toString() : ""));
      return result;
   }

   private static class SlotRequirement {
      final int targetSlot;
      final Ingredient ingredient;
      final int count;

      SlotRequirement(int targetSlot, Ingredient ingredient, int count) {
         this.targetSlot = targetSlot;
         this.ingredient = ingredient;
         this.count = count;
      }
   }

   private static class TargetDeposit {
      final int targetSlot;
      final int count;

      TargetDeposit(int targetSlot, int count) {
         this.targetSlot = targetSlot;
         this.count = count;
      }
   }

   public static void craftRecipe(GuiContainer gui, CraftableRecipe cr, boolean craftAll) {
      if (!isApplicable(gui) || cr == null) {
         return;
      }

      int gridSize = getGridSize(gui);
      int firstCraftSlot = getFirstCraftSlot(gui);
      int resultSlot = getResultSlot(gui);
      int firstInvSlot = getFirstInventorySlot(gui);

      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player != null && !mc.player.inventory.getItemStack().isEmpty()) {
         for (int i = 0; i < 36; i++) {
            Slot s = gui.inventorySlots.getSlot(firstInvSlot + i);
            if (s != null && (!s.getHasStack() || (s.getStack().isItemEqual(mc.player.inventory.getItemStack()) && s.getStack().getCount() < s.getStack().getMaxStackSize()))) {
               slotClick(gui, firstInvSlot + i, 0, ClickType.PICKUP);
               if (mc.player.inventory.getItemStack().isEmpty()) {
                  break;
               }
            }
         }
         if (!mc.player.inventory.getItemStack().isEmpty()) {
            return;
         }
      }

      for (int i = 0; i < gridSize * gridSize; i++) {
         Slot slot = gui.inventorySlots.getSlot(firstCraftSlot + i);
         if (slot != null && slot.getHasStack()) {
            slotClick(gui, firstCraftSlot + i, 0, ClickType.QUICK_MOVE);
         }
      }

      for (int i = 0; i < gridSize * gridSize; i++) {
         Slot slot = gui.inventorySlots.getSlot(firstCraftSlot + i);
         if (slot != null && slot.getHasStack()) {
            return;
         }
      }

      int times = craftAll ? Math.min(cr.craftableCount, cr.output.getMaxStackSize()) : 1;
      if (times <= 0) {
         times = 1;
      }

      int[] available = new int[36];
      ItemStack[] invStacks = new ItemStack[36];
      for (int i = 0; i < 36; i++) {
         Slot s = gui.inventorySlots.getSlot(firstInvSlot + i);
         if (s != null && s.getHasStack()) {
            invStacks[i] = s.getStack();
            available[i] = s.getStack().getCount();
         } else {
            invStacks[i] = ItemStack.EMPTY;
            available[i] = 0;
         }
      }

      List<SlotRequirement> requirements = new ArrayList<>();
      int recipeW = getRecipeWidth(cr.recipe);
      int recipeH = getRecipeHeight(cr.recipe);
      NonNullList<Ingredient> ingredients = cr.recipe.getIngredients();

      if (recipeW > 0 && recipeH > 0 && recipeW <= gridSize && recipeH <= gridSize) {
         for (int r = 0; r < recipeH; r++) {
            for (int c = 0; c < recipeW; c++) {
               int ingIdx = r * recipeW + c;
               if (ingIdx < ingredients.size()) {
                  Ingredient ing = ingredients.get(ingIdx);
                  if (ing != Ingredient.EMPTY && ing.getMatchingStacks().length > 0) {
                     int targetSlot = firstCraftSlot + (r * gridSize + c);
                     requirements.add(new SlotRequirement(targetSlot, ing, times));
                  }
               }
            }
         }
      } else {
         int matrixSlotIndex = 0;
         for (Ingredient ing : ingredients) {
            if (ing != Ingredient.EMPTY && ing.getMatchingStacks().length > 0) {
               if (matrixSlotIndex < gridSize * gridSize) {
                  int targetSlot = firstCraftSlot + matrixSlotIndex;
                  requirements.add(new SlotRequirement(targetSlot, ing, times));
                  matrixSlotIndex++;
               }
            }
         }
      }

      if (requirements.isEmpty()) {
         return;
      }

      Map<Integer, List<TargetDeposit>> plan = new LinkedHashMap<>();
      for (SlotRequirement req : requirements) {
         int remaining = req.count;
         for (int s = 0; s < 36 && remaining > 0; s++) {
            if (available[s] > 0 && req.ingredient.apply(invStacks[s])) {
               int toTake = Math.min(remaining, available[s]);
               available[s] -= toTake;
               remaining -= toTake;
               int invSlotId = firstInvSlot + s;
               List<TargetDeposit> deposits = plan.computeIfAbsent(invSlotId, k -> new ArrayList<>());
               deposits.add(new TargetDeposit(req.targetSlot, toTake));
            }
         }
         if (remaining > 0) {
            return;
         }
      }

      for (Map.Entry<Integer, List<TargetDeposit>> entry : plan.entrySet()) {
         int fromSlot = entry.getKey();
         List<TargetDeposit> deposits = entry.getValue();

         Slot from = gui.inventorySlots.getSlot(fromSlot);
         if (from == null || !from.getHasStack()) {
            continue;
         }

         slotClick(gui, fromSlot, 0, ClickType.PICKUP);

         for (TargetDeposit td : deposits) {
            ItemStack cursorStack = (mc.player != null) ? mc.player.inventory.getItemStack() : ItemStack.EMPTY;
            int cursorCount = cursorStack.isEmpty() ? 0 : cursorStack.getCount();
            if (cursorCount <= 0) {
               break;
            }

            if (td.count >= cursorCount) {
               slotClick(gui, td.targetSlot, 0, ClickType.PICKUP);
            } else {
               for (int i = 0; i < td.count; i++) {
                  slotClick(gui, td.targetSlot, 1, ClickType.PICKUP);
               }
            }
         }

         if (mc.player != null && !mc.player.inventory.getItemStack().isEmpty()) {
            slotClick(gui, fromSlot, 0, ClickType.PICKUP);
         }
      }

      Slot resSlot = gui.inventorySlots.getSlot(resultSlot);
      if (resSlot != null) {
         resSlot.putStack(cr.output.copy());
      }
      slotClick(gui, resultSlot, 0, ClickType.QUICK_MOVE);

      if (mc.player != null && !mc.player.inventory.getItemStack().isEmpty()) {
         for (int i = 0; i < 36; i++) {
            Slot s = gui.inventorySlots.getSlot(firstInvSlot + i);
            if (s != null && (!s.getHasStack() || (s.getStack().isItemEqual(mc.player.inventory.getItemStack()) && s.getStack().getCount() < s.getStack().getMaxStackSize()))) {
               slotClick(gui, firstInvSlot + i, 0, ClickType.PICKUP);
               if (mc.player.inventory.getItemStack().isEmpty()) {
                  break;
               }
            }
         }
      }
   }

   private static void slotClick(GuiContainer gui, int slotId, int mouseButton, ClickType clickType) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.playerController != null && mc.player != null) {
         mc.playerController.windowClick(gui.inventorySlots.windowId, slotId, mouseButton, clickType, mc.player);
      }
   }
}
