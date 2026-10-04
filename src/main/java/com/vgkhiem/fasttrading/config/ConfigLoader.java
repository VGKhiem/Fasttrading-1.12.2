package com.vgkhiem.fasttrading.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ConfigLoader {
   public ConfigJson config;
   public ArrayList<ConfigJson.SimpleRecipe> recipeList = new ArrayList();
   private File configFile;
   private Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
   private ConfigJson oldConfig;

   public ConfigLoader(FMLPreInitializationEvent event) throws IOException {
      File configDir = new File(event.getModConfigurationDirectory(), "khyxultilities");
      if (!configDir.exists()) {
         configDir.mkdirs();
      }
      this.configFile = new File(configDir, "fasttrading.json");
      File oldFile = new File(event.getModConfigurationDirectory(), "fasttrading.json");
      if (!this.configFile.exists()) {
         if (oldFile.exists()) {
            oldFile.renameTo(this.configFile);
         } else {
            this.configFile.createNewFile();
            this.fileInit(this.configFile);
         }
      }

      JsonReader j = new JsonReader(new FileReader(this.configFile));
      this.config = (ConfigJson)this.gson.fromJson(j, ConfigJson.class);
      j.close();
      if (null == this.config || null == this.config.recipeList) {
         this.config = new ConfigJson(true, new ConfigJson.SimpleRecipe[0]);
      }

      Collections.addAll(this.recipeList, this.config.recipeList);
      this.oldConfig = (ConfigJson)this.config.clone();
   }

   public void save() {
      this.startToSave(this.config.isAuto, this.recipeList);
   }

   private void startToSave(boolean isAuto, List<ConfigJson.SimpleRecipe> list) {
      ConfigJson configJson = new ConfigJson(isAuto, (ConfigJson.SimpleRecipe[])list.toArray(new ConfigJson.SimpleRecipe[0]));
      if (!this.oldConfig.equals(configJson)) {
         this.save0(configJson);
         this.oldConfig = (ConfigJson)configJson.clone();
      }

   }

   private void save0(ConfigJson configJson) {
      String s = this.gson.toJson(configJson);
      BufferedOutputStream buffered = null;

      try {
         buffered = new BufferedOutputStream(new FileOutputStream(this.configFile));
         buffered.write(s.getBytes());
         buffered.flush();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (null != buffered) {
            try {
               buffered.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }

      }

   }

   private void fileInit(File configFile) throws IOException {
      BufferedOutputStream buff = new BufferedOutputStream(new FileOutputStream(configFile));
      buff.write("{}".getBytes());
      buff.flush();
      buff.close();
   }
}
