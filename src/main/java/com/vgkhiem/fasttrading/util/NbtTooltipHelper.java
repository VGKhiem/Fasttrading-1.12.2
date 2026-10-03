package com.vgkhiem.fasttrading.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
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

      NBTTagCompound tag = stack.getTagCompound();
      boolean hasNbt = tag != null && !tag.isEmpty();

      if (!isAltKeyDown()) {
         if (hasNbt) {
            int hideFlags = tag.getInteger("HideFlags");
            if (hideFlags != 0) {
               tooltip.add("§8[Giữ Alt: Hiện " + tag.getKeySet().size() + " NBT & các thẻ bị ẩn]");
            } else {
               tooltip.add("§8[Giữ Alt: Hiện " + tag.getKeySet().size() + " thẻ NBT]");
            }
         }
         return;
      }

      // Alt is held!
      if (!hasNbt) {
         tooltip.add("§8§o[Alt: Vật phẩm này không có dữ liệu NBT]");
         return;
      }

      int hideFlags = tag.getInteger("HideFlags");

      // 1. Hidden & detailed Enchantments
      appendEnchantments(stack, tag, hideFlags, tooltip);

      // 2. Hidden Attributes
      appendAttributes(tag, hideFlags, tooltip);

      // 3. Hidden Unbreakable
      appendUnbreakable(tag, hideFlags, tooltip);

      // 4. Hidden CanDestroy / CanPlaceOn
      appendCanDestroyAndPlace(tag, hideFlags, tooltip);

      // 5. Full NBT Tag Details
      appendNbtDetails(tag, hideFlags, tooltip);
   }

   private static List<String> getHideFlagsDescriptions(int flags) {
      List<String> list = new ArrayList<String>();
      if ((flags & 1) != 0) list.add("Enchantment");
      if ((flags & 2) != 0) list.add("Thuộc tính (Modifiers)");
      if ((flags & 4) != 0) list.add("Không vỡ (Unbreakable)");
      if ((flags & 8) != 0) list.add("CanDestroy");
      if ((flags & 16) != 0) list.add("CanPlaceOn");
      if ((flags & 32) != 0) list.add("Khác/Potion/StoredEnch");
      if ((flags & 64) != 0) list.add("Màu nhuộm (Dye)");
      return list;
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
      if ((hideFlags & 2) != 0) {
         if (tag.hasKey("AttributeModifiers", 9)) {
            NBTTagList attrList = tag.getTagList("AttributeModifiers", 10);
            if (!attrList.isEmpty()) {
               tooltip.add("§6§l[Thuộc tính bị ẩn (AttributeModifiers)]:");
               for (int i = 0; i < attrList.tagCount(); ++i) {
                  NBTTagCompound attr = attrList.getCompoundTagAt(i);
                  String name = attr.getString("AttributeName");
                  double amount = attr.getDouble("Amount");
                  String slot = attr.getString("Slot");
                  String formattedAmt = (amount >= 0 ? "+" : "") + String.format("%.2f", amount);
                  tooltip.add("  §c[Ẩn] §e" + name + ": §a" + formattedAmt + (slot.isEmpty() ? "" : " §8(Slot: " + slot + ")"));
               }
            }
         } else {
            tooltip.add("§6§l[Thuộc tính]: §c(Bị ẩn toàn bộ bởi HideFlags)");
         }
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

   private static void appendNbtDetails(NBTTagCompound tag, int hideFlags, List<String> tooltip) {
      tooltip.add("§6§l=== CHI TIẾT NBT (Alt) ===");
      if (hideFlags != 0) {
         List<String> hiddenNames = getHideFlagsDescriptions(hideFlags);
         tooltip.add("  §eHideFlags: §f" + hideFlags + " §8(0b" + Integer.toBinaryString(hideFlags) + ") " + (hiddenNames.isEmpty() ? "" : "§c[Ẩn: " + String.join(", ", hiddenNames) + "]"));
      }

      List<String> keys = new ArrayList<String>(tag.getKeySet());
      Collections.sort(keys);

      int linesAdded = 0;
      final int MAX_LINES = 25;

      for (String key : keys) {
         if ("HideFlags".equals(key)) {
            continue;
         }
         NBTBase nbt = tag.getTag(key);
         if (nbt == null) continue;

         if (linesAdded >= MAX_LINES) {
            int remaining = keys.size() - keys.indexOf(key);
            tooltip.add("  §8... và " + remaining + " thẻ NBT khác");
            break;
         }

         linesAdded += formatAndAddTag(key, nbt, "  ", tooltip, linesAdded, MAX_LINES);
      }
   }

   private static int formatAndAddTag(String key, NBTBase tag, String indent, List<String> tooltip, int currentLines, int maxLines) {
      if (currentLines >= maxLines) {
         return 0;
      }
      int lines = 1;
      byte type = tag.getId();
      switch (type) {
         case 10: // Compound
            NBTTagCompound comp = (NBTTagCompound)tag;
            tooltip.add(indent + "§e" + key + ": §6{§7" + comp.getKeySet().size() + " mục§6}");
            List<String> subKeys = new ArrayList<String>(comp.getKeySet());
            Collections.sort(subKeys);
            int subCount = 0;
            for (String subKey : subKeys) {
               if (currentLines + lines >= maxLines) {
                  tooltip.add(indent + "  §8... (" + (subKeys.size() - subCount) + " mục con khác)");
                  lines++;
                  break;
               }
               NBTBase subTag = comp.getTag(subKey);
               if (subTag != null) {
                  if (subTag.getId() == 10) {
                     tooltip.add(indent + "  §e" + subKey + ": §6{...}");
                     lines++;
                  } else {
                     lines += formatAndAddTag(subKey, subTag, indent + "  ", tooltip, currentLines + lines, maxLines);
                  }
               }
               subCount++;
            }
            break;
         case 9: // List
            NBTTagList list = (NBTTagList)tag;
            int count = list.tagCount();
            tooltip.add(indent + "§e" + key + ": §9[§7" + count + " mục§9]");
            if (count > 0 && count <= 3) {
               for (int i = 0; i < count; i++) {
                  if (currentLines + lines >= maxLines) break;
                  NBTBase elem = list.get(i);
                  if (elem.getId() == 10) {
                     tooltip.add(indent + "  §8[§f" + i + "§8] §6{...}");
                  } else {
                     tooltip.add(indent + "  §8[§f" + i + "§8] §7" + truncate(elem.toString(), 40));
                  }
                  lines++;
               }
            }
            break;
         case 8: // String
            String str = ((NBTTagString)tag).getString();
            tooltip.add(indent + "§e" + key + ": §a\"" + truncate(str, 50) + "\"");
            break;
         case 1: // Byte
            byte b = ((NBTTagByte)tag).getByte();
            if (b == 0 || b == 1) {
               tooltip.add(indent + "§e" + key + ": §b" + b + " §8(bool: " + (b == 1) + ")");
            } else {
               tooltip.add(indent + "§e" + key + ": §b" + b + "b");
            }
            break;
         case 2: // Short
            tooltip.add(indent + "§e" + key + ": §b" + ((NBTTagShort)tag).getShort() + "s");
            break;
         case 3: // Int
            tooltip.add(indent + "§e" + key + ": §b" + ((NBTTagInt)tag).getInt());
            break;
         case 4: // Long
            tooltip.add(indent + "§e" + key + ": §b" + ((NBTTagLong)tag).getLong() + "L");
            break;
         case 5: // Float
            tooltip.add(indent + "§e" + key + ": §b" + ((NBTTagFloat)tag).getFloat() + "f");
            break;
         case 6: // Double
            tooltip.add(indent + "§e" + key + ": §b" + ((NBTTagDouble)tag).getDouble() + "d");
            break;
         default:
            tooltip.add(indent + "§e" + key + ": §7" + truncate(tag.toString(), 40));
            break;
      }
      return lines;
   }

   private static String truncate(String text, int maxLength) {
      if (text == null) return "";
      if (text.length() <= maxLength) {
         return text;
      }
      return text.substring(0, maxLength - 3) + "...";
   }
}
