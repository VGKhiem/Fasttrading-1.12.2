package com.vgkhiem.khyxultilities.network;

import com.vgkhiem.khyxultilities.config.ProxyConfig;
import io.netty.channel.Channel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ProxyHook {
   public static void injectProxy(Channel ch) {
      try {
         ProxyConfig config = ProxyConfig.get();
         if (config == null || !config.isEnabled()) {
            return;
         }

         com.vgkhiem.khyxultilities.config.ProxyEntry proxy = config.getActiveProxy();
         if (proxy == null || !proxy.isValid()) {
            return;
         }

         SocketAddress proxyAddr = new InetSocketAddress(proxy.host, proxy.port);
         ProxyHandler proxyHandler = null;

         com.vgkhiem.khyxultilities.config.ProxyEntry.Type type = proxy.getType();
         if (type == com.vgkhiem.khyxultilities.config.ProxyEntry.Type.SOCKS5) {
            if (proxy.hasAuth()) {
               proxyHandler = new Socks5ProxyHandler(proxyAddr, proxy.username, proxy.password);
            } else {
               proxyHandler = new Socks5ProxyHandler(proxyAddr);
            }
         } else if (type == com.vgkhiem.khyxultilities.config.ProxyEntry.Type.SOCKS4) {
            if (proxy.hasAuth()) {
               proxyHandler = new Socks4ProxyHandler(proxyAddr, proxy.username);
            } else {
               proxyHandler = new Socks4ProxyHandler(proxyAddr);
            }
         } else if (type == com.vgkhiem.khyxultilities.config.ProxyEntry.Type.HTTP) {
            if (proxy.hasAuth()) {
               proxyHandler = new HttpProxyHandler(proxyAddr, proxy.username, proxy.password);
            } else {
               proxyHandler = new HttpProxyHandler(proxyAddr);
            }
         }

         if (proxyHandler != null) {
            ch.pipeline().addFirst("khyx_proxy", proxyHandler);
         }
      } catch (Throwable t) {
         t.printStackTrace();
      }
   }
}
