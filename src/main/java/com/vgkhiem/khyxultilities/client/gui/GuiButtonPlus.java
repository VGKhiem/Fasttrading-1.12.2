package com.vgkhiem.khyxultilities.client.gui;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

public class GuiButtonPlus extends GuiButton {
   private static ResourceLocation resourceButtonDefault = new ResourceLocation("textures/gui/widgets.png");
   private int hoverTime = 0;
   private String[] tooltipText;

   public GuiButtonPlus(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String[] tooltipText) {
      super(buttonId, x, y, widthIn, heightIn, buttonText);
      this.tooltipText = tooltipText;
   }

   public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
         this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
         if (this.isMouseOver()) {
            ++this.hoverTime;
         } else {
            this.hoverTime = 0;
         }

         int k = this.getHoverState(this.isMouseOver());
         mc.getTextureManager().bindTexture(resourceButtonDefault);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
         this.drawTexturedModalRect(this.x, this.y, 1, 46 + k * 20 + 1, this.width / 2, this.height / 2);
         this.drawTexturedModalRect(this.x, this.y + this.height / 2, 1, 46 + k * 20 + 20 - this.height / 2 - 1, this.width / 2, this.height / 2);
         this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2 - 1, 46 + k * 20 + 1, this.width / 2, this.height / 2);
         this.drawTexturedModalRect(this.x + this.width / 2, this.y + this.height / 2, 200 - this.width / 2 - 1, 46 + k * 20 + 19 - this.height / 2, this.width / 2, this.height / 2);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }

   }

   public boolean isMouseOver() {
      return this.hovered && this.visible && this.enabled;
   }

   public void drawTooltip(Minecraft mc, int mouseX, int mouseY) {
      if (this.hoverTime > 15 && this.visible) {
         FontRenderer font = mc.fontRenderer;
         GuiUtils.drawHoveringText(ItemStack.EMPTY, Arrays.asList(this.tooltipText), mouseX, mouseY, mc.currentScreen != null ? mc.currentScreen.width : 1000, mc.currentScreen != null ? mc.currentScreen.height : 1000, -1, font);
      }

   }
}
