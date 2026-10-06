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

public class FastCraftConfig {
   public boolean fastCraftEnabled = true;
   public String fastCraftPosition = "RIGHT";
   public boolean autoCraftEnabled = true;
   public List<String> autoCraftRecipes = new ArrayList<>();

   public enum ButtonPosition {
      RIGHT("Right"),
      LEFT("Left");

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
   private static FastCraftConfig instance;

   public static FastCraftConfig get() {
      if (instance == null) {
         instance = new FastCraftConfig();
      }
      if (instance.autoCraftRecipes == null) {
         instance.autoCraftRecipes = new ArrayList<>();
      }
      return instance;
   }

   public static FastCraftConfig load(File folder) {
      if (!folder.exists()) {
         folder.mkdirs();
      }
      configFile = new File(folder, "fastcraft.json");
      if (!configFile.exists()) {
         instance = new FastCraftConfig();
         save(instance);
         return instance;
      }
      try (JsonReader reader = new JsonReader(new FileReader(configFile))) {
         instance = GSON.fromJson(reader, FastCraftConfig.class);
         if (instance == null) {
            instance = new FastCraftConfig();
         }
         if (instance.autoCraftRecipes == null) {
            instance.autoCraftRecipes = new ArrayList<>();
         }
         return instance;
      } catch (Exception e) {
         instance = new FastCraftConfig();
         instance.autoCraftRecipes = new ArrayList<>();
         save(instance);
         return instance;
      }
   }

   public static void save() {
      if (instance != null) {
         save(instance);
      }
   }

   public static void save(FastCraftConfig cfg) {
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
