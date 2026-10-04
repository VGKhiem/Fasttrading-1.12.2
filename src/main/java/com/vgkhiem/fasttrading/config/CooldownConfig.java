package com.vgkhiem.fasttrading.config;

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
