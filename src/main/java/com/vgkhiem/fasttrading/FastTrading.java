package com.vgkhiem.fasttrading;

import com.vgkhiem.fasttrading.config.ConfigLoader;
import com.vgkhiem.fasttrading.eventhandler.FastTradingEventHandler;
import com.vgkhiem.fasttrading.util.KeyLoader;
import java.io.IOException;
import net.minecraft.entity.NpcMerchant;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
   modid = "fasttrading",
   version = "2.0",
   name = "FastTrading",
   acceptedMinecraftVersions = "[1.12, )",
   clientSideOnly = true
)
public class FastTrading {
   public static final String MODID = "fasttrading";
   public static final String NAME = "FastTrading";
   public static final String VERSION = "2.0";
   public static ConfigLoader configLoader = null;
   public static Logger logger;

   @EventHandler
   public void init(FMLInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new FastTradingEventHandler());
      MinecraftForge.EVENT_BUS.register(this);
      new KeyLoader();
      Class c = NpcMerchant.class;
   }

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) throws IOException {
      configLoader = new ConfigLoader(event);
      logger = event.getModLog();
   }
}
