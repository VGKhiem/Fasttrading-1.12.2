package com.vgkhiem.khyxultilities.util;

import java.util.Iterator;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;

public class VGKhiemUtils {
   public static String[] tooltipI18n(String s, Object... args) {
      return I18n.format(s, args).replace("\\n", "##&&").split("##&&");
   }

   public static boolean areItemEqualIgnoreCount(ItemStack stackA, ItemStack stackB) {
      if (stackA == null && stackB == null) {
         return true;
      } else if (stackA == null || stackB == null) {
         return false;
      } else if (stackA.isEmpty() && stackB.isEmpty()) {
         return true;
      } else if (stackA.isEmpty() || stackB.isEmpty()) {
         return false;
      } else if (stackA.getItem() != stackB.getItem()) {
         return false;
      } else if (stackA.getItemDamage() != stackB.getItemDamage() && stackA.getItemDamage() != 32767 && stackB.getItemDamage() != 32767) {
         return false;
      }

      if (ItemStack.areItemStackTagsEqual(stackA, stackB)) {
         return true;
      }
      boolean emptyA = !stackA.hasTagCompound() || stackA.getTagCompound().isEmpty();
      boolean emptyB = !stackB.hasTagCompound() || stackB.getTagCompound().isEmpty();

      if (emptyA && emptyB) {
         return true;
      }
      if (emptyB) {
         return true;
      }
      if (emptyA) {
         return false;
      }
      return NBTUtil.areNBTEquals(stackB.getTagCompound(), stackA.getTagCompound(), false)
          || NBTUtil.areNBTEquals(stackA.getTagCompound(), stackB.getTagCompound(), false);
   }

   public static boolean areNbtEqual(NBTBase nbtA, NBTBase nbtB) {
      if (nbtA == null && nbtB == null) {
         return true;
      } else if (nbtA != null && nbtB != null) {
         if (nbtA.getClass() != nbtB.getClass()) {
            return false;
         } else {
            if (nbtA instanceof NBTTagCompound) {
               NBTTagCompound nbtCompoundA = (NBTTagCompound)nbtA;
               NBTTagCompound nbtCompoundB = (NBTTagCompound)nbtB;
               if (nbtCompoundA.getKeySet().size() != nbtCompoundB.getKeySet().size()) {
                  return false;
               }

               for(String key : nbtCompoundA.getKeySet()) {
                  NBTBase nbtBaseA = nbtCompoundA.getTag(key);
                  NBTBase nbtBaseB = nbtCompoundB.getTag(key);
                  if (!areNbtEqual(nbtBaseA, nbtBaseB)) {
                     return false;
                  }
               }
            } else {
               if (nbtA instanceof NBTTagList) {
                  Iterator<NBTBase> nbtListA = ((NBTTagList)nbtA).iterator();
                  Iterator<NBTBase> nbtListB = ((NBTTagList)nbtB).iterator();

                  while(nbtListA.hasNext()) {
                     if (!nbtListB.hasNext()) {
                        return false;
                     }

                     if (!areNbtEqual((NBTBase)nbtListA.next(), (NBTBase)nbtListB.next())) {
                        return false;
                     }
                  }

                  return !nbtListB.hasNext();
               }

               if (nbtA instanceof NBTPrimitive) {
                  return nbtA.equals(nbtB);
               }

               if (nbtA instanceof NBTTagString) {
                  NBTTagString stringA = (NBTTagString)nbtA;
                  NBTTagString stringB = (NBTTagString)nbtB;
                  return stringA.getString().equals(stringB.getString());
               }

               if (nbtA instanceof NBTTagByteArray) {
                  NBTTagByteArray byteArrayA = (NBTTagByteArray)nbtA;
                  NBTTagByteArray byteArrayB = (NBTTagByteArray)nbtB;
                  return byteArrayA.equals(byteArrayB);
               }

               if (nbtA instanceof NBTTagIntArray) {
                  NBTTagIntArray intArrayA = (NBTTagIntArray)nbtA;
                  NBTTagIntArray intArrayB = (NBTTagIntArray)nbtB;
                  return intArrayA.equals(intArrayB);
               }

               if (nbtA instanceof NBTTagLongArray) {
                  NBTTagLongArray longArrayA = (NBTTagLongArray)nbtA;
                  NBTTagLongArray longArrayB = (NBTTagLongArray)nbtB;
                  return longArrayA.equals(longArrayB);
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }
}
