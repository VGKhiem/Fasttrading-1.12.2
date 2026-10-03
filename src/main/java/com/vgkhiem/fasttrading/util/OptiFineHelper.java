package com.vgkhiem.fasttrading.util;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public class OptiFineHelper {
   private static Field ofFastRenderField = null;
   private static boolean initialized = false;

   private static void init() {
      if (!initialized) {
         initialized = true;
         try {
            ofFastRenderField = GameSettings.class.getDeclaredField("ofFastRender");
            ofFastRenderField.setAccessible(true);
         } catch (Exception ignored) {
         }
      }
   }

   public static boolean isFastRender() {
      init();
      if (ofFastRenderField != null) {
         try {
            return ofFastRenderField.getBoolean(Minecraft.getMinecraft().gameSettings);
         } catch (Exception ignored) {
         }
      }
      return false;
   }

   public static void setFastRender(boolean value) {
      init();
      if (ofFastRenderField != null) {
         try {
            ofFastRenderField.setBoolean(Minecraft.getMinecraft().gameSettings, value);
         } catch (Exception ignored) {
         }
      }
   }
}
