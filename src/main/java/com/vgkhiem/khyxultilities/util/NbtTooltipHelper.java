package com.vgkhiem.khyxultilities.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public class NbtTooltipHelper {

   public static boolean isAltKeyDown() {
      if (!Keyboard.isCreated()) {
         return false;
      }
      return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
   }

   public static void handleTooltip(ItemStack stack, List<String> tooltip) {
      if (stack.isEmpty()) {
         return;
      }

      if (!isAltKeyDown()) {
         return;
      }

      NBTTagCompound tag = stack.getTagCompound();
      if (tag == null || tag.isEmpty()) {
         return;
      }

      int hideFlags = tag.getInteger("HideFlags");

      appendEnchantments(stack, tag, hideFlags, tooltip);
      appendAttributes(tag, hideFlags, tooltip);
      appendCanDestroyAndPlace(tag, hideFlags, tooltip);
   }

   private static void appendEnchantments(ItemStack stack, NBTTagCompound tag, int hideFlags, List<String> tooltip) {
      NBTTagList enchList = null;

      if (stack.getItem() == Items.ENCHANTED_BOOK) {
         enchList = ItemEnchantedBook.getEnchantments(stack);
      }
      if (enchList == null || enchList.isEmpty()) {
         if (tag.hasKey("ench", 9)) {
            enchList = tag.getTagList("ench", 10);
         } else if (tag.hasKey("StoredEnchantments", 9)) {
            enchList = tag.getTagList("StoredEnchantments", 10);
         }
      }

      if (enchList != null && !enchList.isEmpty()) {
         List<String> validEnchants = new ArrayList<String>();
         for (int i = 0; i < enchList.tagCount(); ++i) {
            NBTTagCompound enchTag = enchList.getCompoundTagAt(i);
            short id = enchTag.getShort("id");
            short lvl = enchTag.getShort("lvl");
            if (lvl <= 0) {
               continue;
            }
            Enchantment ench = Enchantment.getEnchantmentByID(id);
            if (ench != null) {
               validEnchants.add("  " + TextFormatting.AQUA + ench.getTranslatedName(lvl));
            } else {
               validEnchants.add("  " + TextFormatting.RED + "Enchant #" + id + " Cấp: " + lvl);
            }
         }
         if (!validEnchants.isEmpty()) {
            tooltip.add(TextFormatting.LIGHT_PURPLE + "Enchantments:");
            tooltip.addAll(validEnchants);
         }
      }
   }

   private static void appendAttributes(NBTTagCompound tag, int hideFlags, List<String> tooltip) {
      if (tag.hasKey("AttributeModifiers", 9)) {
         NBTTagList attrList = tag.getTagList("AttributeModifiers", 10);
         if (!attrList.isEmpty()) {
            tooltip.add(TextFormatting.GOLD + "Attribute Modifiers:");
            for (int i = 0; i < attrList.tagCount(); ++i) {
               NBTTagCompound attr = attrList.getCompoundTagAt(i);
               String name = attr.getString("AttributeName");
               double amount = attr.getDouble("Amount");
               int operation = attr.getInteger("Operation");
               String slot = attr.getString("Slot");
               String slotDisplay = slot.isEmpty() ? "all" : slot;
               String formattedAmt = formatAmount(name, amount, operation);
               tooltip.add("  " + TextFormatting.YELLOW + name + ": " + TextFormatting.GREEN + formattedAmt + " " + TextFormatting.DARK_GRAY + "(Slot: " + slotDisplay + ")");
            }
         }
      }
   }

   private static String formatAmount(String attrName, double amount, int operation) {
      boolean isPercent = (operation == 1 || operation == 2 || "generic.knockbackResistance".equals(attrName));
      if (isPercent) {
         double pct = amount * 100.0;
         if (Math.abs(pct - Math.round(pct)) < 1e-6) {
            return (pct >= 0 ? "+" : "") + Math.round(pct) + "%";
         } else {
            String s = String.format("%.2f", pct);
            if (s.endsWith(".00")) s = s.substring(0, s.length() - 3);
            else if (s.endsWith("0")) s = s.substring(0, s.length() - 1);
            return (pct >= 0 ? "+" : "") + s + "%";
         }
      } else {
         if (Math.abs(amount - Math.round(amount)) < 1e-6) {
            return (amount >= 0 ? "+" : "") + Math.round(amount);
         } else {
            String s = String.format("%.2f", amount);
            if (s.endsWith(".00")) s = s.substring(0, s.length() - 3);
            else if (s.endsWith("0")) s = s.substring(0, s.length() - 1);
            return (amount >= 0 ? "+" : "") + s;
         }
      }
   }

   private static void appendCanDestroyAndPlace(NBTTagCompound tag, int hideFlags, List<String> tooltip) {
      if ((hideFlags & 8) != 0 && tag.hasKey("CanDestroy", 9)) {
         NBTTagList list = tag.getTagList("CanDestroy", 8);
         tooltip.add(TextFormatting.GREEN + "Có thể phá hủy: " + TextFormatting.GRAY + list.tagCount() + " khối");
      }
      if ((hideFlags & 16) != 0 && tag.hasKey("CanPlaceOn", 9)) {
         NBTTagList list = tag.getTagList("CanPlaceOn", 8);
         tooltip.add(TextFormatting.GREEN + "Có thể đặt lên: " + TextFormatting.GRAY + list.tagCount() + " khối");
      }
   }
}
