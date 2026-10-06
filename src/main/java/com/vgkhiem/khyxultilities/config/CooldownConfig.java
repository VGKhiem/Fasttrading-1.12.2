package com.vgkhiem.khyxultilities.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CooldownConfig {
   public int fastTradeCooldown = 50;
   public int guiClickCooldown = 100;
   public String buttonPosition = "RIGHT";
   public boolean fastCraftEnabled = true;
   public String fastCraftPosition = "RIGHT";
   public boolean autoCraftEnabled = true;
   public List<String> autoCraftRecipes = new ArrayList<>();

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

   public boolean isFastCraftEnabled() {
      return this.fastCraftEnabled;
   }

   public void setFastCraftEnabled(boolean enabled) {
      this.fastCraftEnabled = enabled;
   }

   public ButtonPosition getFastCraftPosition() {
      try {
         ButtonPosition pos = ButtonPosition.valueOf(this.fastCraftPosition.toUpperCase());
         return (pos == ButtonPosition.LEFT) ? ButtonPosition.LEFT : ButtonPosition.RIGHT;
      } catch (Exception e) {
         return ButtonPosition.RIGHT;
      }
   }

   public void setFastCraftPosition(ButtonPosition pos) {
      this.fastCraftPosition = (pos == ButtonPosition.LEFT) ? "LEFT" : "RIGHT";
   }

   public boolean isAutoCraftEnabled() {
      return this.autoCraftEnabled;
   }

   public void setAutoCraftEnabled(boolean enabled) {
      this.autoCraftEnabled = enabled;
   }

   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static File configFile;

   public static CooldownConfig load(File folder) {
      if (!folder.exists()) {
         folder.mkdirs();
      }
      configFile = new File(folder, "cooldown.json");
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
         if (cfg.autoCraftRecipes == null) {
            cfg.autoCraftRecipes = new ArrayList<>();
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
