package com.vgkhiem.khyxultilities.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ProxyConfig {
   public boolean enabled = false;
   public int activeIndex = 0;
   public List<ProxyEntry> proxies = new ArrayList<ProxyEntry>();

   public String host = "";
   public int port = 0;
   public String proxyType = "SOCKS5";
   public String username = "";
   public String password = "";
   public String buttonPosition = "TOP_RIGHT";

   public enum ButtonPosition {
      TOP_RIGHT("Top-Right"),
      TOP_LEFT("Top-Left"),
      BOTTOM_LEFT("Bottom-Left"),
      BOTTOM_RIGHT("Bottom-Right");

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
         return ButtonPosition.TOP_RIGHT;
      }
   }

   public void setButtonPosition(ButtonPosition pos) {
      this.buttonPosition = pos != null ? pos.name() : ButtonPosition.TOP_RIGHT.name();
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public ProxyEntry getActiveProxy() {
      if (this.proxies == null || this.proxies.isEmpty()) {
         return null;
      }
      if (this.activeIndex < 0 || this.activeIndex >= this.proxies.size()) {
         this.activeIndex = 0;
      }
      return this.proxies.get(this.activeIndex);
   }

   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static File configFile;
   private static ProxyConfig instance;

   public static ProxyConfig get() {
      if (instance == null) {
         instance = new ProxyConfig();
      }
      if (instance.proxies == null) {
         instance.proxies = new ArrayList<ProxyEntry>();
      }
      return instance;
   }

   public static ProxyConfig load(File folder) {
      if (!folder.exists()) {
         folder.mkdirs();
      }
      configFile = new File(folder, "proxy.json");
      if (!configFile.exists()) {
         instance = new ProxyConfig();
         save(instance);
         return instance;
      }
      try (JsonReader reader = new JsonReader(new FileReader(configFile))) {
         instance = GSON.fromJson(reader, ProxyConfig.class);
         if (instance == null) {
            instance = new ProxyConfig();
         }
         if (instance.proxies == null) {
            instance.proxies = new ArrayList<ProxyEntry>();
         }
         if (instance.proxies.isEmpty() && instance.host != null && !instance.host.trim().isEmpty()) {
            instance.proxies.add(new ProxyEntry("Default", instance.proxyType, instance.host, instance.port, instance.username, instance.password));
         }
         save(instance);
         return instance;
      } catch (Exception e) {
         instance = new ProxyConfig();
         instance.proxies = new ArrayList<ProxyEntry>();
         save(instance);
         return instance;
      }
   }

   public static void save() {
      if (instance != null) {
         save(instance);
      }
   }

   public static void save(ProxyConfig cfg) {
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

   public static String pingTest(String host, int port, int timeoutMs) {
      Socket socket = null;
      try {
         long start = System.currentTimeMillis();
         socket = new Socket();
         socket.connect(new InetSocketAddress(host, port), timeoutMs);
         long latency = System.currentTimeMillis() - start;
         return "OK:" + latency;
      } catch (Exception e) {
         String msg = e.getMessage();
         if (msg == null || msg.trim().isEmpty()) {
            msg = e.getClass().getSimpleName();
         }
         return "ERR:" + msg;
      } finally {
         if (socket != null) {
            try {
               socket.close();
            } catch (Exception ignored) {
            }
         }
      }
   }
}
