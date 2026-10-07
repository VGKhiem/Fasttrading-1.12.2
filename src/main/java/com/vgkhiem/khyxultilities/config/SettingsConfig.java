package com.vgkhiem.khyxultilities.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class SettingsConfig {
   public int fastTradeCooldown = 50;
   public int guiClickCooldown = 100;
   public String buttonPosition = "RIGHT";
   public boolean betterScoreboard = true;

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

   public static SettingsConfig load(File folder) {
      if (!folder.exists()) {
         folder.mkdirs();
      }
      configFile = new File(folder, "settings.json");
      if (!configFile.exists()) {
         SettingsConfig cfg = new SettingsConfig();
         save(cfg);
         return cfg;
      }
      try (JsonReader reader = new JsonReader(new FileReader(configFile))) {
         SettingsConfig cfg = GSON.fromJson(reader, SettingsConfig.class);
         if (cfg == null) {
            cfg = new SettingsConfig();
            save(cfg);
         }
         return cfg;
      } catch (Exception e) {
         SettingsConfig cfg = new SettingsConfig();
         save(cfg);
         return cfg;
      }
   }

   public static void save(SettingsConfig cfg) {
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
