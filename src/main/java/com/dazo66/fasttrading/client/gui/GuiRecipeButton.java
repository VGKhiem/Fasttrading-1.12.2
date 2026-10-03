package com.dazo66.fasttrading.client.gui;

import com.dazo66.fasttrading.client.audio.FakeSubtitleSound;
import com.dazo66.fasttrading.config.ConfigJson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;

public class GuiRecipeButton extends GuiButton {
   private static final ResourceLocation MERCHANT_GUI_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/villager.png");
   public boolean hasBeenMove = false;
   private GuiMerchantOverride gui;
   private MerchantRecipe recipe;

   public GuiRecipeButton(int buttonId, int x, int y, GuiMerchantOverride gui, MerchantRecipe recipe) {
      super(buttonId, x, y, 89, 25, "");
      this.recipe = recipe;
      this.gui = gui;
   }

   public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
         this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
         GlStateManager.popMatrix();
         this.renderBackground(mc);
         this.renderItem(mc);
         GlStateManager.pushMatrix();
      }

   }

   private void renderBackground(Minecraft mc) {
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      GlStateManager.enableAlpha();
      ConfigJson.SimpleRecipe simpleRecipe = (ConfigJson.SimpleRecipe)this.gui.helper.map.get(this.recipe);
      if (null != simpleRecipe) {
         if (simpleRecipe.lockPrice) {
            if (ConfigJson.isSamePrice(this.recipe, simpleRecipe)) {
               GlStateManager.color(1.0F, 0.8F, 0.8F, 1.0F);
            }
         } else {
            GlStateManager.color(1.0F, 0.9F, 0.9F, 1.0F);
         }
      } else {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }

      mc.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
      this.drawTexturedModalRect(this.x, this.y, 0, 0, this.width - 5, this.height - 5);
      this.drawTexturedModalRect(this.x, this.y + this.height - 5, 0, 161, this.width / 2 + 1, 5);
      this.drawTexturedModalRect(this.x + this.width - 5, this.y, 171, 0, 5, this.height - 5);
      this.drawTexturedModalRect(this.x + this.width / 2 + 1, this.y + this.height - 5, 176 - this.width / 2, 161, this.width / 2, 5);
      if (this.recipe.isRecipeDisabled()) {
         this.drawTexturedModalRect(this.x + 36 + 15 - 11, this.y + 4, 212, 3, 28, 15);
      } else {
         this.drawTexturedModalRect(this.x + 36 + 15 - 11, this.y + 4, 83, 24, 28, 15);
      }

   }

   private void renderItem(Minecraft mc) {
      GlStateManager.enableDepth();
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableColorMaterial();
      GlStateManager.disableLighting();
      String s0 = this.recipe.getItemToBuy().getCount() == 1 ? "" : String.valueOf(this.recipe.getItemToBuy().getCount());
      String s1 = this.recipe.getSecondItemToBuy().getCount() == 1 ? "" : String.valueOf(this.recipe.getSecondItemToBuy().getCount());
      String s2 = this.recipe.getItemToSell().getCount() == 1 ? "" : String.valueOf(this.recipe.getItemToSell().getCount());
      RenderItem renderItem = mc.getRenderItem();
      renderItem.renderItemAndEffectIntoGUI(this.recipe.getItemToBuy(), this.x + 5, this.y + 4);
      renderItem.renderItemAndEffectIntoGUI(this.recipe.getSecondItemToBuy(), this.x + 18 + 6 + 1, this.y + 4);
      renderItem.renderItemAndEffectIntoGUI(this.recipe.getItemToSell(), this.x + 36 + 12 + 28 + 2 - 10, this.y + 4);
      renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, this.recipe.getItemToBuy(), this.x + 5, this.y + 4, s0);
      renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, this.recipe.getSecondItemToBuy(), this.x + 18 + 6 + 1, this.y + 4, s1);
      renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, this.recipe.getItemToSell(), this.x + 36 + 12 + 28 + 2 - 10, this.y + 4, s2);
      GlStateManager.disableDepth();
      GlStateManager.disableLighting();
   }

   public boolean isMouseOver() {
      return this.hovered && this.visible && this.enabled;
   }

   private void renderItemTooltip(int mouseX, int mouseY, ItemStack itemStack) {
      if (!itemStack.isEmpty()) {
         this.gui.drawHoveringText(this.gui.getItemToolTip(itemStack), mouseX, mouseY);
      }
   }

   public void tryRenderItemTooltip(int mouseX, int mouseY) {
      if (this.visible) {
         GlStateManager.disableDepth();
         if (mouseX >= this.x + 5 && mouseX <= this.x + 23 && mouseY >= this.y + 4 && mouseY <= this.y + 22) {
            this.renderItemTooltip(mouseX, mouseY, this.recipe.getItemToBuy());
         } else if (mouseX >= this.x + 18 + 6 + 1 && mouseX <= this.x + 18 + 6 + 1 + 18 && mouseY >= this.y + 4 && mouseY <= this.y + 22) {
            this.renderItemTooltip(mouseX, mouseY, this.recipe.getSecondItemToBuy());
         } else if (mouseX >= this.x + 36 + 12 + 28 + 2 - 10 && mouseX <= this.x + 36 + 12 + 28 + 2 - 10 + 18 && mouseY >= this.y + 4 && mouseY <= this.y + 22) {
            this.renderItemTooltip(mouseX, mouseY, this.recipe.getItemToSell());
         }
      }

   }

   public void tryProminent(Minecraft mc, int mouseX, int mouseY, float p, boolean shouldDraw) {
      if (this.visible) {
         if (this.isMouseOver() && !this.hasBeenMove && shouldDraw) {
            --this.x;
            --this.y;
            ++this.height;
            this.hasBeenMove = true;
            mc.getSoundHandler().playSound(FakeSubtitleSound.getRecord(SoundEvents.ENTITY_ITEM_PICKUP, 0.5F, 0.05F, "fasttrading.subtitles.buttonswitching"));
         } else if (!shouldDraw && this.hasBeenMove) {
            ++this.x;
            ++this.y;
            --this.height;
            this.hasBeenMove = false;
         }

         if (shouldDraw) {
            mc.getRenderItem().zLevel = 120.0F;
            this.drawButton(mc, mouseX, mouseY, p);
            mc.getRenderItem().zLevel = 100.0F;
         }
      }

   }
}
