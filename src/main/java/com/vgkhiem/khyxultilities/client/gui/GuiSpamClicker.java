package com.vgkhiem.khyxultilities.client.gui;

import com.vgkhiem.khyxultilities.FastTrading;
import com.vgkhiem.khyxultilities.config.CooldownConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiSpamClicker {
   private static final int BTN_SPAM_ID = 7701;
   private static final int BTN_MODE_ID = 7702;
   private static final int BTN_SLOT_ID = 7703;

   private static Field guiLeftField;
   private static Field guiTopField;
   private static Field xSizeField;
   private static Field ySizeField;
   private static Field hoveredSlotField;
   private static Method renderHoveredToolTipMethod;

   private static boolean spamEnabled = false;
   private static boolean showSlotIds = false;
   private static GuiClickMode currentMode = GuiClickMode.SHIFT_LEFT;
   private static String targetSlotText = "";

   private Minecraft mc = Minecraft.getMinecraft();
   private GuiButton spamButton;
   private GuiButton modeButton;
   private GuiButton slotIdButton;
   private GuiSlotInputRow slotInputRow;
   private GuiTextField slotInputField;
   private long lastClickTime = 0;

   static {
      try {
         guiLeftField = ReflectionHelper.findField(GuiContainer.class, "guiLeft", "field_147003_i");
         guiTopField = ReflectionHelper.findField(GuiContainer.class, "guiTop", "field_147009_r");
         xSizeField = ReflectionHelper.findField(GuiContainer.class, "xSize", "field_146999_f");
         ySizeField = ReflectionHelper.findField(GuiContainer.class, "ySize", "field_147000_g");
         hoveredSlotField = ReflectionHelper.findField(GuiContainer.class, "hoveredSlot", "field_147006_u");
         renderHoveredToolTipMethod = ReflectionHelper.findMethod(GuiContainer.class, "renderHoveredToolTip", "func_191948_b", int.class, int.class);
         renderHoveredToolTipMethod.setAccessible(true);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private class GuiSlotInputRow extends GuiButton {
      private long lastCursorTick = 0;

      public GuiSlotInputRow(int buttonId, int x, int y, int widthIn, int heightIn) {
         super(buttonId, x, y, widthIn, heightIn, "");
      }

      @Override
      public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
         if (!this.visible) {
            return;
         }
         mc.fontRenderer.drawStringWithShadow("Slot:", this.x + 2, this.y + 6, 0xFFFFFF);
         if (slotInputField != null) {
            long now = System.currentTimeMillis();
            if (lastCursorTick == 0 || now - lastCursorTick > 500) {
               lastCursorTick = now;
            }
            while (now - lastCursorTick >= 50) {
               slotInputField.updateCursorCounter();
               lastCursorTick += 50;
            }
            slotInputField.drawTextBox();
         }
      }

      @Override
      public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
         return false;
      }
   }

   private String getSpamText() {
      return "Spam Slot: " + (spamEnabled ? "§aON" : "§cOFF");
   }

   private String getModeText() {
      return "Mode: " + currentMode.getDisplayName();
   }

   private String getSlotIdText() {
      return "Slot ID: " + (showSlotIds ? "§aON" : "§cOFF");
   }

   private boolean isTargetGui(Object gui) {
      return gui instanceof GuiContainer && !(gui instanceof GuiMerchantOverride);
   }

   @SubscribeEvent
   public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
      if (!isTargetGui(event.getGui())) {
         return;
      }

      GuiContainer gui = (GuiContainer) event.getGui();
      try {
         int guiLeft = guiLeftField != null ? guiLeftField.getInt(gui) : (gui.width - 176) / 2;
         int guiTop = guiTopField != null ? guiTopField.getInt(gui) : (gui.height - 166) / 2;
         int xSize = xSizeField != null ? xSizeField.getInt(gui) : 176;

         CooldownConfig.ButtonPosition pos = FastTrading.cooldownConfig != null ?
            FastTrading.cooldownConfig.getButtonPosition() : CooldownConfig.ButtonPosition.RIGHT;

         int btnWidth = 85;
         int btnHeight = 20;

         int spamX, spamY;
         int modeX, modeY;
         int slotIdX, slotIdY;
         int inputX, inputY;

         if (pos == CooldownConfig.ButtonPosition.LEFT) {
            int btnX = guiLeft - btnWidth - 4;
            if (btnX < 2) {
               btnX = 2;
            }
            spamX = modeX = slotIdX = inputX = btnX;
            spamY = guiTop + 4;
            modeY = guiTop + 26;
            slotIdY = guiTop + 48;
            inputY = guiTop + 70;
         } else if (pos == CooldownConfig.ButtonPosition.TOP) {
            int startX = guiLeft + (xSize - (btnWidth * 2 + 4)) / 2;
            if (startX < 2) {
               startX = 2;
            }
            int startY = guiTop - (btnHeight * 2 + 4);
            if (startY < 2) {
               startY = 2;
            }
            spamX = startX;
            spamY = startY;
            modeX = startX + btnWidth + 4;
            modeY = startY;

            slotIdX = startX;
            slotIdY = startY + btnHeight + 2;
            inputX = startX + btnWidth + 4;
            inputY = startY + btnHeight + 2;
         } else if (pos == CooldownConfig.ButtonPosition.BOTTOM) {
            int startX = guiLeft + (xSize - (btnWidth * 2 + 4)) / 2;
            if (startX < 2) {
               startX = 2;
            }
            int ySize = ySizeField != null ? ySizeField.getInt(gui) : 166;
            int startY = guiTop + ySize + 4;
            if (startY + btnHeight * 2 + 2 > gui.height - 2) {
               startY = Math.max(2, gui.height - btnHeight * 2 - 4);
            }
            spamX = startX;
            spamY = startY;
            modeX = startX + btnWidth + 4;
            modeY = startY;

            slotIdX = startX;
            slotIdY = startY + btnHeight + 2;
            inputX = startX + btnWidth + 4;
            inputY = startY + btnHeight + 2;
         } else {
            int btnX = guiLeft + xSize + 4;
            if (btnX + btnWidth > gui.width - 2) {
               if (guiLeft - btnWidth - 4 >= 2) {
                  btnX = guiLeft - btnWidth - 4;
               } else {
                  btnX = Math.max(2, gui.width - btnWidth - 2);
               }
            }
            spamX = modeX = slotIdX = inputX = btnX;
            spamY = guiTop + 4;
            modeY = guiTop + 26;
            slotIdY = guiTop + 48;
            inputY = guiTop + 70;
         }

         this.spamButton = new GuiButton(BTN_SPAM_ID, spamX, spamY, btnWidth, btnHeight, getSpamText());
         this.modeButton = new GuiButton(BTN_MODE_ID, modeX, modeY, btnWidth, btnHeight, getModeText());
         this.slotIdButton = new GuiButton(BTN_SLOT_ID, slotIdX, slotIdY, btnWidth, btnHeight, getSlotIdText());

         this.slotInputField = new GuiTextField(7704, this.mc.fontRenderer, inputX + 32, inputY + 2, 50, 16);
         this.slotInputField.setMaxStringLength(4);
         this.slotInputField.setText(targetSlotText);

         this.slotInputRow = new GuiSlotInputRow(7705, inputX, inputY, btnWidth, btnHeight);

         event.getButtonList().add(this.spamButton);
         event.getButtonList().add(this.modeButton);
         event.getButtonList().add(this.slotIdButton);
         event.getButtonList().add(this.slotInputRow);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @SubscribeEvent
   public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
      if (event.getButton().id == BTN_SPAM_ID) {
         spamEnabled = !spamEnabled;
         event.getButton().displayString = getSpamText();
         event.setCanceled(true);
      } else if (event.getButton().id == BTN_MODE_ID) {
         currentMode = currentMode.next();
         event.getButton().displayString = getModeText();
         event.setCanceled(true);
      } else if (event.getButton().id == BTN_SLOT_ID) {
         showSlotIds = !showSlotIds;
         event.getButton().displayString = getSlotIdText();
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
      if (!isTargetGui(event.getGui())) {
         return;
      }

      if (this.slotInputField != null && this.slotInputField.isFocused()) {
         if (Keyboard.getEventKeyState()) {
            char c = Keyboard.getEventCharacter();
            int key = Keyboard.getEventKey();

            if (key == Keyboard.KEY_ESCAPE) {
               this.slotInputField.setFocused(false);
               event.setCanceled(true);
               return;
            }

            if (GuiScreen.isKeyComboCtrlA(key) || GuiScreen.isKeyComboCtrlC(key) ||
                GuiScreen.isKeyComboCtrlV(key) || GuiScreen.isKeyComboCtrlX(key) ||
                Character.isDigit(c) || key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE ||
                key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT || key == Keyboard.KEY_HOME || key == Keyboard.KEY_END) {
               this.slotInputField.textboxKeyTyped(c, key);
               String text = this.slotInputField.getText();
               String filtered = text.replaceAll("[^0-9]", "");
               if (!text.equals(filtered)) {
                  this.slotInputField.setText(filtered);
               }
               targetSlotText = this.slotInputField.getText();
            }
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
      if (!isTargetGui(event.getGui())) {
         return;
      }

      int button = Mouse.getEventButton();
      boolean buttonState = Mouse.getEventButtonState();

      if (button == 0 && buttonState) {
         GuiContainer gui = (GuiContainer) event.getGui();
         int mouseX = Mouse.getEventX() * gui.width / this.mc.displayWidth;
         int mouseY = gui.height - Mouse.getEventY() * gui.height / this.mc.displayHeight - 1;

         if (this.slotInputField != null) {
            this.slotInputField.mouseClicked(mouseX, mouseY, button);
         }

         if (this.spamButton != null && this.spamButton.mousePressed(this.mc, mouseX, mouseY)) {
            return;
         }
         if (this.modeButton != null && this.modeButton.mousePressed(this.mc, mouseX, mouseY)) {
            return;
         }
         if (this.slotIdButton != null && this.slotIdButton.mousePressed(this.mc, mouseX, mouseY)) {
            return;
         }
         if (this.slotInputRow != null && mouseX >= this.slotInputRow.x && mouseX <= this.slotInputRow.x + this.slotInputRow.width &&
             mouseY >= this.slotInputRow.y && mouseY <= this.slotInputRow.y + this.slotInputRow.height) {
            return;
         }

         Slot slot = getSlotUnderMouse(gui, mouseX, mouseY);
         if (slot != null && spamEnabled && (this.slotInputField == null || this.slotInputField.getText().trim().isEmpty())) {
            event.setCanceled(true);
            executeClick(gui, slot);
            this.lastClickTime = System.currentTimeMillis();
         }
      }
   }

   @SubscribeEvent
   public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
      if (!isTargetGui(event.getGui())) {
         return;
      }

      GuiContainer gui = (GuiContainer) event.getGui();

      if (showSlotIds) {
         renderSlotIds(gui);
         if (renderHoveredToolTipMethod != null) {
            try {
               renderHoveredToolTipMethod.invoke(gui, event.getMouseX(), event.getMouseY());
            } catch (Exception ignored) {
            }
         }
      }

      if (!spamEnabled) {
         return;
      }

      long now = System.currentTimeMillis();
      long cooldown = FastTrading.cooldownConfig != null ? FastTrading.cooldownConfig.guiClickCooldown : 100;
      if (now - this.lastClickTime < cooldown) {
         return;
      }

      String targetText = this.slotInputField != null ? this.slotInputField.getText().trim() : "";
      if (!targetText.isEmpty()) {
         try {
            int targetNum = Integer.parseInt(targetText);
            Slot targetSlot = findSlotByNumber(gui, targetNum);
            if (targetSlot != null) {
               this.lastClickTime = now;
               executeClick(gui, targetSlot);
            }
         } catch (NumberFormatException ignored) {
         }
      } else if (Mouse.isButtonDown(0)) {
         int mouseX = event.getMouseX();
         int mouseY = event.getMouseY();

         if (this.spamButton != null && this.spamButton.isMouseOver()) {
            return;
         }
         if (this.modeButton != null && this.modeButton.isMouseOver()) {
            return;
         }
         if (this.slotIdButton != null && this.slotIdButton.isMouseOver()) {
            return;
         }
         if (this.slotInputRow != null && mouseX >= this.slotInputRow.x && mouseX <= this.slotInputRow.x + this.slotInputRow.width &&
             mouseY >= this.slotInputRow.y && mouseY <= this.slotInputRow.y + this.slotInputRow.height) {
            return;
         }

         Slot slot = getSlotUnderMouse(gui, mouseX, mouseY);
         if (slot != null) {
            this.lastClickTime = now;
            executeClick(gui, slot);
         }
      }
   }

   @SubscribeEvent
   public void onGuiOpen(GuiOpenEvent event) {
      this.lastClickTime = 0;
      if (event.getGui() == null) {
         this.slotInputField = null;
         this.spamButton = null;
         this.modeButton = null;
         this.slotIdButton = null;
         this.slotInputRow = null;
      }
   }

   private void renderSlotIds(GuiContainer gui) {
      try {
         int guiLeft = guiLeftField != null ? guiLeftField.getInt(gui) : (gui.width - 176) / 2;
         int guiTop = guiTopField != null ? guiTopField.getInt(gui) : (gui.height - 166) / 2;

         GlStateManager.pushMatrix();
         GlStateManager.disableLighting();
         GlStateManager.disableDepth();
         GlStateManager.enableBlend();

         for (Slot slot : gui.inventorySlots.inventorySlots) {
            int slotX = guiLeft + slot.xPos;
            int slotY = guiTop + slot.yPos;
            String text = String.valueOf(slot.slotNumber);

            GlStateManager.pushMatrix();
            GlStateManager.translate(slotX + 1.0F, slotY + 1.0F, 300.0F);
            GlStateManager.scale(0.7F, 0.7F, 1.0F);
            this.mc.fontRenderer.drawStringWithShadow(text, 0, 0, 0xFFFF55);
            GlStateManager.popMatrix();
         }

         GlStateManager.enableDepth();
         GlStateManager.enableLighting();
         GlStateManager.popMatrix();
      } catch (Exception ignored) {
      }
   }

   private Slot findSlotByNumber(GuiContainer gui, int slotNumber) {
      for (Slot slot : gui.inventorySlots.inventorySlots) {
         if (slot.slotNumber == slotNumber) {
            return slot;
         }
      }
      return null;
   }

   private Slot getSlotUnderMouse(GuiContainer gui, int mouseX, int mouseY) {
      if (hoveredSlotField != null) {
         try {
            Slot slot = (Slot) hoveredSlotField.get(gui);
            if (slot != null) {
               return slot;
            }
         } catch (Exception ignored) {
         }
      }
      if (guiLeftField != null && guiTopField != null) {
         try {
            int guiLeft = guiLeftField.getInt(gui);
            int guiTop = guiTopField.getInt(gui);
            for (Slot slot : gui.inventorySlots.inventorySlots) {
               if (mouseX >= guiLeft + slot.xPos && mouseX < guiLeft + slot.xPos + 16 &&
                   mouseY >= guiTop + slot.yPos && mouseY < guiTop + slot.yPos + 16) {
                  return slot;
               }
            }
         } catch (Exception ignored) {
         }
      }
      return null;
   }

   private void executeClick(GuiContainer gui, Slot slot) {
      if (this.mc.playerController != null && this.mc.player != null) {
         this.mc.playerController.windowClick(
            gui.inventorySlots.windowId,
            slot.slotNumber,
            currentMode.getMouseButton(),
            currentMode.getClickType(),
            this.mc.player
         );
      }
   }
}
