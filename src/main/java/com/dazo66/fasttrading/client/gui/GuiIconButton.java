package com.dazo66.fasttrading.client.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

public class GuiIconButton extends GuiButtonPlus {
   private ResourceLocation icon;
   private int iconX = 0;
   private int iconY = 0;
   private int textureWidth = -1;
   private int textureHeight = -1;

   public GuiIconButton(int buttonId, int x, int y, int widthIn, int heightIn, String[] tooltipText, ResourceLocation icon) {
      super(buttonId, x, y, widthIn, heightIn, "", tooltipText);
      this.icon = icon;
   }

   public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
         super.drawButton(mc, mouseX, mouseY, partialTicks);
         GlStateManager.disableLighting();
         GlStateManager.disableDepth();
         mc.getTextureManager().bindTexture(this.icon);
         if (this.textureHeight == -1 || this.textureWidth == -1) {
            this.loadWidthAndHeight(mc.getResourceManager(), this.icon);
         }

         Gui.drawScaledCustomSizeModalRect(this.x, this.y, 0.0F, 0.0F, this.textureWidth, this.textureHeight, this.width, this.height, (float)this.textureWidth, (float)this.textureHeight);
         GlStateManager.enableDepth();
      }

   }

   public boolean isMouseOver() {
      return this.hovered && this.visible && this.enabled;
   }

   public void drawCustomSizedTexture(int x, int y, float u, float v, int width, int height, ResourceLocation texture) {
      Minecraft mc = Minecraft.getMinecraft();
      mc.getTextureManager().bindTexture(texture);
      mc.getTextureManager().getTexture(texture).getGlTextureId();
      if (this.textureHeight == -1 || this.textureWidth == -1) {
         this.loadWidthAndHeight(mc.getResourceManager(), texture);
      }

      float f = 1.0F / (float)this.textureWidth;
      float f1 = 1.0F / (float)this.textureHeight;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
      bufferbuilder.pos((double)x, (double)(y + height), (double)0.0F).tex((double)(u * f), (double)((v + (float)height) * f1)).endVertex();
      bufferbuilder.pos((double)(x + width), (double)(y + height), (double)0.0F).tex((double)((u + (float)width) * f), (double)((v + (float)height) * f1)).endVertex();
      bufferbuilder.pos((double)(x + width), (double)y, (double)0.0F).tex((double)((u + (float)width) * f), (double)(v * f1)).endVertex();
      bufferbuilder.pos((double)x, (double)y, (double)0.0F).tex((double)(u * f), (double)(v * f1)).endVertex();
      tessellator.draw();
   }

   public void loadWidthAndHeight(IResourceManager resourceManager, ResourceLocation resourceLocation) {
      IResource iresource = null;

      try {
         iresource = resourceManager.getResource(resourceLocation);
         BufferedImage bufferedimage = TextureUtil.readBufferedImage(iresource.getInputStream());
         this.textureWidth = bufferedimage.getWidth();
         this.textureHeight = bufferedimage.getHeight();
      } catch (IOException e) {
         System.out.println(e.toString());
      } finally {
         IOUtils.closeQuietly(iresource);
      }

   }

   public void setIconX(int iconX) {
      this.iconX = iconX;
   }

   public void setIconY(int iconY) {
      this.iconY = iconY;
   }

   public void setIcon(ResourceLocation icon) {
      this.icon = icon;
      this.textureWidth = -1;
      this.textureHeight = -1;
   }
}
