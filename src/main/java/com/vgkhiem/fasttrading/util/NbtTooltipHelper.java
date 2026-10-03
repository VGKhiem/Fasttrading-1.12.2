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

      // 1. Không hiện dòng ghi chú khi không giữ Alt
      if (!isAltKeyDown()) {
         return;
      }

      NBTTagCompound tag = stack.getTagCompound();
      if (tag == null || tag.isEmpty()) {
         return;
      }

      int hideFlags = tag.getInteger("HideFlags");

      // 2. Chỉ hiện Enchant ẩn / chi tiết
      appendEnchantments(stack, tag, hideFlags, tooltip);

      // 3. Chỉ hiện Thuộc tính ẩn (AttributeModifiers)
      appendAttributes(tag, hideFlags, tooltip);

      // 4. Chỉ hiện Không thể phá hủy ẩn (Unbreakable)
      appendUnbreakable(tag, hideFlags, tooltip);

      // 5. Chỉ hiện CanDestroy / CanPlaceOn nếu bị ẩn
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
         tooltip.add("§d§l[Enchantment] " + (isHidden ? "§c§l(BỊ ẨN BỞI HIDEFLAGS):" : "§7(Chi tiết):"));
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
            tooltip.add(isHidden ? "§6§l[Thuộc tính bị ẩn (AttributeModifiers)]:" : "§6§l[Thuộc tính (AttributeModifiers)]:");
            for (int i = 0; i < attrList.tagCount(); ++i) {
               NBTTagCompound attr = attrList.getCompoundTagAt(i);
               String name = attr.getString("AttributeName");
               double amount = attr.getDouble("Amount");
               String slot = attr.getString("Slot");
               String formattedAmt = (amount >= 0 ? "+" : "") + String.format("%.2f", amount);
               tooltip.add("  " + (isHidden ? "§c[Ẩn] " : "§7- ") + "§e" + name + ": §a" + formattedAmt + (slot.isEmpty() ? "" : " §8(Slot: " + slot + ")"));
            }
         }
      } else if ((hideFlags & 2) != 0) {
         tooltip.add("§6§l[Thuộc tính]: §c(Bị ẩn toàn bộ bởi HideFlags)");
      }
   }

   private static void appendUnbreakable(NBTTagCompound tag, int hideFlags, List<String> tooltip) {
      if (tag.getBoolean("Unbreakable")) {
         if ((hideFlags & 4) != 0) {
            tooltip.add("§b§l[Ẩn] §bKhông thể phá hủy (Unbreakable: true)");
         } else {
            tooltip.add("§bKhông thể phá hủy (Unbreakable: true)");
         }
      }
   }

   private static void appendCanDestroyAndPlace(NBTTagCompound tag, int hideFlags, List<String> tooltip) {
      if ((hideFlags & 8) != 0 && tag.hasKey("CanDestroy", 9)) {
         NBTTagList list = tag.getTagList("CanDestroy", 8);
         tooltip.add("§a§l[Ẩn] Có thể phá hủy: §7" + list.tagCount() + " khối");
      }
      if ((hideFlags & 16) != 0 && tag.hasKey("CanPlaceOn", 9)) {
         NBTTagList list = tag.getTagList("CanPlaceOn", 8);
         tooltip.add("§a§l[Ẩn] Có thể đặt lên: §7" + list.tagCount() + " khối");
      }
   }
}
