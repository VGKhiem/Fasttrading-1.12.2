package com.vgkhiem.khyxultilities;

import com.vgkhiem.khyxultilities.client.gui.GuiSpamClicker;
import com.vgkhiem.khyxultilities.config.ConfigLoader;
import com.vgkhiem.khyxultilities.config.FastCraftConfig;
import com.vgkhiem.khyxultilities.config.ProxyConfig;
import com.vgkhiem.khyxultilities.config.SettingsConfig;
import com.vgkhiem.khyxultilities.eventhandler.FastTradingEventHandler;
import com.vgkhiem.khyxultilities.eventhandler.GuiMultiplayerHandler;
import com.vgkhiem.khyxultilities.util.KeyLoader;
import java.io.File;
import java.io.IOException;
import net.minecraft.entity.NpcMerchant;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
   modid = FastTrading.MODID,
   name = FastTrading.NAME,
   useMetadata = true,
   acceptedMinecraftVersions = "[1.12, )",
   clientSideOnly = true,
   guiFactory = "com.vgkhiem.khyxultilities.client.gui.FastTradingGuiFactory"
)
public class FastTrading {
   public static final String MODID = "khyxultilities";
   public static final String NAME = "KhyxUltilities";
   public static String VERSION = "2.0";
   public static ConfigLoader configLoader = null;
   public static SettingsConfig settingsConfig = null;
   public static FastCraftConfig fastCraftConfig = null;
   public static Logger logger;

   @EventHandler
   public void init(FMLInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new FastTradingEventHandler());
      MinecraftForge.EVENT_BUS.register(new GuiSpamClicker());
      MinecraftForge.EVENT_BUS.register(new com.vgkhiem.khyxultilities.client.gui.GuiFastCraft());
      MinecraftForge.EVENT_BUS.register(new GuiMultiplayerHandler());
      MinecraftForge.EVENT_BUS.register(new com.vgkhiem.khyxultilities.eventhandler.ScoreboardHandler());
      MinecraftForge.EVENT_BUS.register(this);
      new KeyLoader();
      NpcMerchant.class.getName();
   }

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) throws IOException {
      if (event.getModMetadata() != null && event.getModMetadata().version != null) {
         VERSION = event.getModMetadata().version;
      }
      configLoader = new ConfigLoader(event);
      File configDir = new File(event.getModConfigurationDirectory(), "khyxultilities");
      settingsConfig = SettingsConfig.load(configDir);
      fastCraftConfig = FastCraftConfig.load(configDir);
      ProxyConfig.load(configDir);
      logger = event.getModLog();
   }
}
