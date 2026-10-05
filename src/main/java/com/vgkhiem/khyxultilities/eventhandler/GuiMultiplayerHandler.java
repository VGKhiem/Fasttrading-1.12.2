package com.vgkhiem.khyxultilities.eventhandler;

import com.vgkhiem.khyxultilities.client.gui.GuiProxyConfig;
import com.vgkhiem.khyxultilities.config.ProxyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiMultiplayerHandler {
   private static final int PROXY_BUTTON_ID = 9981;

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
      if (event.getGui() instanceof GuiMultiplayer) {
         ProxyConfig cfg = ProxyConfig.get();
         String label = "Proxy: " + (cfg.isEnabled() ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF");
         event.getButtonList().add(new GuiButton(PROXY_BUTTON_ID, event.getGui().width - 85, 5, 80, 20, label));
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
      if (event.getGui() instanceof GuiMultiplayer && event.getButton().id == PROXY_BUTTON_ID) {
         Minecraft.getMinecraft().displayGuiScreen(new GuiProxyConfig(event.getGui()));
      }
   }
}
