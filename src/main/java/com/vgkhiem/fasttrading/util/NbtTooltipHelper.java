package com.vgkhiem.fasttrading.util;

import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

      // Không hiển thị ghi chú khi không giữ Alt
      if (!isAltKeyDown()) {
         return;
      }

      NBTTagCompound tag = stack.getTagCompound();
      if (tag == null || tag.isEmpty()) {
         return;
      }

      int hideFlags = tag.getInteger("HideFlags");

      // 1. Enchantments
      appendEnchantments(stack, tag, hideFlags, tooltip);

      // 2. Attribute Modifiers (hỗ trợ % cho Operation 1, 2 và Knockback Resistance)
      appendAttributes(tag, hideFlags, tooltip);

      // 3. Unbreakable
      appendUnbreakable(tag, hideFlags, tooltip);

      // 4. CanDestroy / CanPlaceOn
      appendCanDestroyAndPlace(tag, hideFlags, tooltip);
   }

   private static void appendEnchantments(ItemStack stack, NBTTagCompound tag, int hideFlags, List<String> tooltip) {
      NBTTagList enchList = null;
      boolean isStored = false;

      if (stack.getItem() == Items.ENCHANTED_BOOK) {
         enchList = ItemEnchantedBook.getEnchantments(stack);
         isStored = true;
      }
      if (enchList == null || enchList.isEmpty()) {
         if (tag.hasKey("ench", 9)) {
            enchList = tag.getTagList("ench", 10);
            isStored = false;
         } else if (tag.hasKey("StoredEnchantments", 9)) {
            enchList = tag.getTagList("StoredEnchantments", 10);
            isStored = true;
         }
      }

      if (enchList != null && !enchList.isEmpty()) {
         boolean isHidden = isStored ? ((hideFlags & 32) != 0 || (hideFlags & 1) != 0) : ((hideFlags & 1) != 0);
         tooltip.add("§dEnchantments:");
         for (int i = 0; i < enchList.tagCount(); ++i) {
            NBTTagCompound enchTag = enchList.getCompoundTagAt(i);
            short id = enchTag.getShort("id");
            short lvl = enchTag.getShort("lvl");
            Enchantment ench = Enchantment.getEnchantmentByID(id);
            if (ench != null) {
               String translated = ench.getTranslatedName(lvl);
               String regName = ench.getRegistryName() != null ? ench.getRegistryName().toString() : ("id:" + id);
               tooltip.add("  " + (isHidden ? "§c[Ẩn] " : "§7- ") + "§b" + translated + " §8[" + regName + " | id:" + id + ", lvl:" + lvl + "]");
            } else {
               tooltip.add("  " + (isHidden ? "§c[Ẩn] " : "§7- ") + "§c[Enchant #" + id + "] Cấp: " + lvl);
            }
         }
      }
   }

   private static void appendAttributes(NBTTagCompound tag, int hideFlags, List<String> tooltip) {
      if (tag.hasKey("AttributeModifiers", 9)) {
         NBTTagList attrList = tag.getTagList("AttributeModifiers", 10);
         if (!attrList.isEmpty()) {
            boolean isHidden = (hideFlags & 2) != 0;
            tooltip.add("§6Attribute Modifiers:");
            for (int i = 0; i < attrList.tagCount(); ++i) {
               NBTTagCompound attr = attrList.getCompoundTagAt(i);
               String name = attr.getString("AttributeName");
               double amount = attr.getDouble("Amount");
               int operation = attr.getInteger("Operation");
               String slot = attr.getString("Slot");
               String formattedAmt = formatAmount(name, amount, operation);
               tooltip.add("  " + (isHidden ? "§c[Ẩn] " : "§7- ") + "§e" + name + ": §a" + formattedAmt + (slot.isEmpty() ? "" : " §8(Slot: " + slot + ")"));
            }
         }
      } else if ((hideFlags & 2) != 0) {
         tooltip.add("§6Attribute Modifiers: §c(Bị ẩn toàn bộ bởi HideFlags)");
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

   private static void appendUnbreakable(NBTTagCompound tag, int hideFlags, List<String> tooltip) {
      if (tag.getBoolean("Unbreakable")) {
         if ((hideFlags & 4) != 0) {
            tooltip.add("§b[Ẩn] Không thể phá hủy (Unbreakable: true)");
         } else {
            tooltip.add("§bKhông thể phá hủy (Unbreakable: true)");
         }
      }
   }

   private static void appendCanDestroyAndPlace(NBTTagCompound tag, int hideFlags, List<String> tooltip) {
      if ((hideFlags & 8) != 0 && tag.hasKey("CanDestroy", 9)) {
         NBTTagList list = tag.getTagList("CanDestroy", 8);
         tooltip.add("§a[Ẩn] Có thể phá hủy: §7" + list.tagCount() + " khối");
      }
      if ((hideFlags & 16) != 0 && tag.hasKey("CanPlaceOn", 9)) {
         NBTTagList list = tag.getTagList("CanPlaceOn", 8);
         tooltip.add("§a[Ẩn] Có thể đặt lên: §7" + list.tagCount() + " khối");
      }
   }
}
