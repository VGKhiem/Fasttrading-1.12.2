package com.vgkhiem.fasttrading.eventhandler;

import com.vgkhiem.fasttrading.FastTrading;
import com.vgkhiem.fasttrading.client.gui.GuiMerchantOverride;
import com.vgkhiem.fasttrading.event.SetMerchantListEvent;
import com.vgkhiem.fasttrading.util.KeyLoader;
import com.vgkhiem.fasttrading.util.NbtTooltipHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.entity.IMerchant;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FastTradingEventHandler {
   private Minecraft mc = Minecraft.getMinecraft();

   @SubscribeEvent
   public void onSetMerchantList(SetMerchantListEvent event) {
      if (this.mc.currentScreen instanceof GuiMerchantOverride) {
         GuiMerchantOverride gui = (GuiMerchantOverride)this.mc.currentScreen;
         gui.setMerchantRecipeList(event.list);
      }

   }

   @SubscribeEvent
   public void guiOpenEvent(GuiOpenEvent event0) {
      if (event0.getGui() instanceof GuiMerchant) {
         IMerchant iMerchant = ((GuiMerchant)event0.getGui()).getMerchant();
         GuiMerchantOverride guiMerchantOverride = new GuiMerchantOverride(this.mc.player.inventory, iMerchant, this.mc.player.world);
         event0.setGui(guiMerchantOverride);
      }

   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onKeyInput(InputEvent.KeyInputEvent event) {
      if (KeyLoader.key_F4.isPressed()) {
         if (FastTrading.configLoader.config.isAuto) {
            FastTrading.configLoader.config.setAuto(false);
         } else {
            FastTrading.configLoader.config.setAuto(true);
         }

         FastTrading.configLoader.save();
      }

   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onItemTooltip(ItemTooltipEvent event) {
      if (event.getItemStack() != null) {
         NbtTooltipHelper.handleTooltip(event.getItemStack(), event.getToolTip());
      }
   }
}
