package com.vgkhiem.khyxultilities.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class CooldownConfig {
   public int fastTradeCooldown = 50;
   public int guiClickCooldown = 100;
   public String buttonPosition = "RIGHT";
   public boolean showRedNumbers = false;

   public enum ButtonPosition {
      RIGHT("Right"),
      LEFT("Left"),
      TOP("Top"),
      BOTTOM("Bottom");

      private final String displayName;

      ButtonPosition(String displayName) {
         this.displayName = displayName;
      }

      public String getDisplayName() {
         return this.displayName;
      }

      public ButtonPosition next() {
         ButtonPosition[] vals = values();
         return vals[(this.ordinal() + 1) % vals.length];
      }
   }

   public ButtonPosition getButtonPosition() {
      try {
         return ButtonPosition.valueOf(this.buttonPosition.toUpperCase());
      } catch (Exception e) {
         return ButtonPosition.RIGHT;
      }
   }

   public void setButtonPosition(ButtonPosition pos) {
      this.buttonPosition = pos.name();
   }

   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static File configFile;

   public static CooldownConfig load(File folder) {
      if (!folder.exists()) {
         folder.mkdirs();
      }
      configFile = new File(folder, "settings.json");
      File oldConfigFile = new File(folder, "cooldown.json");
      if (!configFile.exists() && oldConfigFile.exists()) {
         try {
            oldConfigFile.renameTo(configFile);
         } catch (Exception ignored) {
         }
      }
      if (!configFile.exists()) {
         CooldownConfig cfg = new CooldownConfig();
         save(cfg);
         return cfg;
      }
      try (JsonReader reader = new JsonReader(new FileReader(configFile))) {
         CooldownConfig cfg = GSON.fromJson(reader, CooldownConfig.class);
         if (cfg == null) {
            cfg = new CooldownConfig();
            save(cfg);
         }
         return cfg;
      } catch (Exception e) {
         CooldownConfig cfg = new CooldownConfig();
         save(cfg);
         return cfg;
      }
   }

   public static void save(CooldownConfig cfg) {
      if (configFile == null) {
         return;
      }
      try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(configFile))) {
         out.write(GSON.toJson(cfg).getBytes());
         out.flush();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
