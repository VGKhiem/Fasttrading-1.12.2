package com.vgkhiem.fasttrading.client.gui;

import com.vgkhiem.fasttrading.FastTrading;
import com.vgkhiem.fasttrading.config.ConfigJson;
import com.vgkhiem.fasttrading.util.VGKhiemUtils;
import com.vgkhiem.fasttrading.util.TradingHelper;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

public class GuiMerchantOverride extends GuiMerchant {
   public TradingHelper helper = new TradingHelper(this);
   public MerchantRecipeList merchantRecipeList;
   private IMerchant iMerchant;
   private int lastClickTime;
   private GuiButton lastClickButton;
   private ArrayList<GuiRecipeButton> recipeButtonList = new ArrayList();
   private GuiIconButton onButton;
   private GuiIconButton offButton;
   private GuiIconButton plusButton;
   private GuiIconButton subtractButton;
   private GuiIconButton lockButton;
   private GuiIconButton unlockButton;

   public GuiMerchantOverride(InventoryPlayer inventoryPlayer, IMerchant iMerchant, World worldIn) {
      super(inventoryPlayer, iMerchant, worldIn);
      this.iMerchant = iMerchant;
   }

   public void initGui() {
      super.initGui();
      this.recipeButtonList.clear();
      this.addMerchantButton(this.merchantRecipeList);
      this.addFunctionButton();
   }

   public void click(Slot slot, int mouseButton, ClickType type) {
      this.handleMouseClick(slot, slot.slotNumber, mouseButton, type);
   }

   public void drawScreen(int mouseX, int mouseY, float p) {
      super.drawScreen(mouseX, mouseY, p);
      GuiButton button0 = this.getFirstButton();
      if (null != button0 && button0 instanceof GuiRecipeButton) {
         ((GuiRecipeButton)button0).tryProminent(this.mc, mouseX, mouseY, p, true);
         ((GuiRecipeButton)button0).tryRenderItemTooltip(mouseX, mouseY);
      }

      for(GuiButton button : this.buttonList) {
         if (button instanceof GuiButtonPlus) {
            ((GuiButtonPlus)button).drawTooltip(this.mc, mouseX, mouseY);
         }

         if (button != button0 && button instanceof GuiRecipeButton) {
            ((GuiRecipeButton)button).tryProminent(this.mc, mouseX, mouseY, p, false);
         }
      }

   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      if (isShiftKeyDown()) {
         Slot slot = this.getSlotAtPosition(mouseX, mouseY);
         if (null != slot && slot.getHasStack() && slot.slotNumber != 0 && slot.slotNumber != 1 && slot.slotNumber != 2) {
            if (!this.inventorySlots.getSlot(0).getHasStack()) {
               this.moveItem(slot, this.inventorySlots.getSlot(0));
            } else if (!this.inventorySlots.getSlot(1).getHasStack()) {
               this.moveItem(slot, this.inventorySlots.getSlot(1));
            }
         }
      }

      super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   private void moveItem(Slot from, Slot to) {
      this.click(from, 0, ClickType.PICKUP);
      this.click(to, 0, ClickType.PICKUP);
      if (!this.mc.player.inventory.getItemStack().isEmpty()) {
         this.click(from, 0, ClickType.PICKUP);
      }

   }

   private Slot getSlotAtPosition(int x, int y) {
      for(int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
         Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i);
         if (super.isPointInRegion(slot.xPos, slot.yPos, 16, 16, x, y) && slot.isEnabled()) {
            return slot;
         }
      }

      return null;
   }

   private GuiButton getFirstButton() {
      ArrayList<GuiButton> list = new ArrayList();

      for(GuiButton button : this.buttonList) {
         if (button.isMouseOver()) {
            list.add(button);
         }
      }

      for(GuiButton button1 : list) {
         if (button1 instanceof GuiRecipeButton && ((GuiRecipeButton)button1).hasBeenMove) {
            return button1;
         }
      }

      if (!list.isEmpty()) {
         return (GuiButton)list.get(0);
      } else {
         return null;
      }
   }

   public void onGuiClosed() {
      super.onGuiClosed();
      FastTrading.configLoader.save();
   }

   public void updateScreen() {
      super.updateScreen();
      this.functionButtonUpdate();
   }

   protected void actionPerformed(GuiButton button) throws IOException {
      int currentTime = (int)System.currentTimeMillis() % Integer.MAX_VALUE;
      super.actionPerformed(button);
      if (this.merchantRecipeList == null) {
         this.merchantRecipeList = this.iMerchant.getRecipes(this.mc.player);
      }

      GuiButton button1 = this.getFirstButton();
      if (button == button1) {
         MerchantRecipe recipe = (MerchantRecipe)this.merchantRecipeList.get(this.selectedMerchantRecipe);
         ConfigJson.SimpleRecipe currentRecipe = (ConfigJson.SimpleRecipe)this.helper.map.get(recipe);
         if (!button.enabled || !button.visible) {
            return;
         }

         if (this.recipeButtonList.contains(button)) {
            int recipeIndex = button.id - 300;
            this.setCurrentRecipe(recipeIndex);
            if (this.lastClickTime > currentTime - 500 && this.lastClickButton == button) {
               if (isShiftKeyDown()) {
                  this.helper.trading((MerchantRecipe)this.merchantRecipeList.get(recipeIndex), recipeIndex);
               } else {
                  this.helper.tradingOnce((MerchantRecipe)this.merchantRecipeList.get(recipeIndex), recipeIndex);
               }

               this.lastClickButton = null;
               this.lastClickTime = 0;
            } else {
               this.lastClickButton = button;
               this.lastClickTime = currentTime;
            }
         } else if (button.id == 250) {
            FastTrading.configLoader.config.setAuto(true);
         } else if (button.id == 251) {
            FastTrading.configLoader.config.setAuto(false);
         } else if (button.id == 252) {
            ConfigJson.SimpleRecipe recipe1 = new ConfigJson.SimpleRecipe(false, recipe);
            FastTrading.configLoader.recipeList.add(recipe1);
            this.helper.map.put(recipe, recipe1);
         } else if (button.id == 253) {
            FastTrading.configLoader.recipeList.remove(currentRecipe);
            this.helper.map.remove(recipe);
         } else if (button.id == 254) {
            ConfigJson.SimpleRecipe recipe1 = new ConfigJson.SimpleRecipe(true, recipe);
            FastTrading.configLoader.recipeList.remove(currentRecipe);
            FastTrading.configLoader.recipeList.add(recipe1);
            this.helper.map.put(recipe, recipe1);
         } else if (button.id == 255 && null != currentRecipe) {
            currentRecipe.setLockPrice(false);
         }
      }

   }

   private void addFunctionButton() {
      this.onButton = null;
      this.offButton = null;
      this.plusButton = null;
      this.subtractButton = null;
      this.lockButton = null;
      this.unlockButton = null;
      ResourceLocation ON = new ResourceLocation("fasttrading", "textures/gui/on.png");
      ResourceLocation OFF = new ResourceLocation("fasttrading", "textures/gui/off.png");
      ResourceLocation PLUS = new ResourceLocation("fasttrading", "textures/gui/plus.png");
      ResourceLocation SUBTRACT = new ResourceLocation("fasttrading", "textures/gui/subtract.png");
      ResourceLocation LOCK = new ResourceLocation("fasttrading", "textures/gui/lock.png");
      ResourceLocation UNLOCK = new ResourceLocation("fasttrading", "textures/gui/unlock.png");
      this.onButton = new GuiIconButton(250, this.guiLeft + 3, this.guiTop + 3, 10, 10, VGKhiemUtils.tooltipI18n("fasttrading.tooltip.enablebutton"), ON);
      this.offButton = new GuiIconButton(251, this.guiLeft + 3, this.guiTop + 3, 10, 10, VGKhiemUtils.tooltipI18n("fasttrading.tooltip.disablebutton"), OFF);
      this.plusButton = new GuiIconButton(252, this.guiLeft + 14, this.guiTop + 3, 10, 10, VGKhiemUtils.tooltipI18n("fasttrading.tooltip.addbutton"), PLUS);
      this.subtractButton = new GuiIconButton(253, this.guiLeft + 14, this.guiTop + 3, 10, 10, VGKhiemUtils.tooltipI18n("fasttrading.tooltip.removebutton"), SUBTRACT);
      this.lockButton = new GuiIconButton(254, this.guiLeft + 25, this.guiTop + 3, 10, 10, VGKhiemUtils.tooltipI18n("fasttrading.tooltip.lockbutton"), LOCK);
      this.unlockButton = new GuiIconButton(255, this.guiLeft + 25, this.guiTop + 3, 10, 10, VGKhiemUtils.tooltipI18n("fasttrading.tooltip.unlockbutton"), UNLOCK);
      this.addButton(this.onButton);
      this.addButton(this.offButton);
      this.addButton(this.plusButton);
      this.addButton(this.subtractButton);
      this.addButton(this.lockButton);
      this.addButton(this.unlockButton);
      this.functionButtonUpdate();
   }

   private void functionButtonUpdate() {
      if (FastTrading.configLoader.config.isAuto) {
         this.onButton.visible = false;
         this.offButton.visible = true;
      } else {
         this.onButton.visible = true;
         this.offButton.visible = false;
      }

      if (null != this.merchantRecipeList && !this.merchantRecipeList.isEmpty()) {
         MerchantRecipe recipe = (MerchantRecipe)this.merchantRecipeList.get(this.selectedMerchantRecipe);
         ConfigJson.SimpleRecipe simpleRecipe = (ConfigJson.SimpleRecipe)this.helper.map.get(recipe);
         if (null == simpleRecipe) {
            this.plusButton.visible = true;
            this.subtractButton.visible = false;
            this.lockButton.visible = false;
            this.unlockButton.visible = false;
         } else {
            this.plusButton.visible = false;
            this.subtractButton.visible = true;
            this.lockButton.visible = true;
            this.unlockButton.visible = false;
            if (simpleRecipe.lockPrice) {
               if (ConfigJson.isSamePrice(recipe, simpleRecipe)) {
                  this.unlockButton.visible = true;
                  this.lockButton.visible = false;
               } else {
                  this.unlockButton.visible = false;
                  this.lockButton.visible = true;
               }
            }
         }

      }
   }

   private void addMerchantButton(MerchantRecipeList list) {
      if (list != null && !list.isEmpty()) {
         int i = 0;
         int spacing = 25;
         int top = this.guiTop;
         if (list.size() * 25 > 166 && list.size() * 15 < 166) {
            spacing = 166 / list.size();
         } else if (list.size() * 15 >= 166) {
            spacing = 15;
            top -= (list.size() * 15 - 166) / 2;
         }

         for(MerchantRecipe merchantRecipe : list) {
            GuiRecipeButton button = new GuiRecipeButton(300 + i++, this.guiLeft - 89 - 1, top - spacing + i * spacing, this, merchantRecipe);

            for(GuiButton button1 : this.buttonList) {
               if (button1.id == button.id) {
                  return;
               }
            }

            this.buttonList.add(button);
            this.recipeButtonList.add(button);
         }

      } else {
         list = this.iMerchant.getRecipes(this.mc.player);
         if (list != null) {
            this.addMerchantButton(list);
         }

      }
   }

   public void setCurrentRecipe(int index) {
      if (index != this.selectedMerchantRecipe) {
         this.selectedMerchantRecipe = index;
         ((ContainerMerchant)this.inventorySlots).setCurrentRecipeIndex(this.selectedMerchantRecipe);
         PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
         packetbuffer.writeInt(this.selectedMerchantRecipe);
         this.mc.getConnection().sendPacket(new CPacketCustomPayload("MC|TrSel", packetbuffer));
      }

   }

   public void setMerchantRecipeList(MerchantRecipeList list) {
      this.merchantRecipeList = list;
      this.addMerchantButton(this.merchantRecipeList);
      this.helper.init(this.merchantRecipeList);
   }
}
