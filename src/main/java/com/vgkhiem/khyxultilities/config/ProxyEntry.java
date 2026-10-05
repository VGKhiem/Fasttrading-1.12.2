package com.vgkhiem.khyxultilities.config;

public class ProxyEntry {
   public String name = "Proxy";
   public String proxyType = "SOCKS5";
   public String host = "";
   public int port = 0;
   public String username = "";
   public String password = "";

   public enum Type {
      SOCKS5("SOCKS5"),
      SOCKS4("SOCKS4"),
      HTTP("HTTP");

      private final String displayName;

      Type(String displayName) {
         this.displayName = displayName;
      }

      public String getDisplayName() {
         return this.displayName;
      }

      public Type next() {
         Type[] vals = values();
         return vals[(this.ordinal() + 1) % vals.length];
      }
   }

   public ProxyEntry() {
   }

   public ProxyEntry(String name, String proxyType, String host, int port, String username, String password) {
      this.name = name != null ? name : "Proxy";
      this.proxyType = proxyType != null ? proxyType : "SOCKS5";
      this.host = host != null ? host : "";
      this.port = port;
      this.username = username != null ? username : "";
      this.password = password != null ? password : "";
   }

   public Type getType() {
      try {
         return Type.valueOf(this.proxyType.toUpperCase());
      } catch (Exception e) {
         return Type.SOCKS5;
      }
   }

   public void setType(Type type) {
      this.proxyType = type.name();
   }

   public boolean hasAuth() {
      return this.username != null && !this.username.trim().isEmpty();
   }

   public boolean isValid() {
      return this.host != null && !this.host.trim().isEmpty() && this.port > 0 && this.port <= 65535;
   }

   public ProxyEntry copy() {
      return new ProxyEntry(this.name, this.proxyType, this.host, this.port, this.username, this.password);
   }
}
