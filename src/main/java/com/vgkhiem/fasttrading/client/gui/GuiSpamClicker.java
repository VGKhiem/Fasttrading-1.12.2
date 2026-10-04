package com.vgkhiem.fasttrading.client.gui;

import com.vgkhiem.fasttrading.FastTrading;
import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

public class GuiSpamClicker {
   private static final int BTN_SPAM_ID = 7701;
   private static final int BTN_MODE_ID = 7702;

   private static Field guiLeftField;
   private static Field guiTopField;
   private static Field xSizeField;
   private static Field hoveredSlotField;

   private static boolean spamEnabled = false;
   private static GuiClickMode currentMode = GuiClickMode.SHIFT_LEFT;

   private Minecraft mc = Minecraft.getMinecraft();
   private GuiButton spamButton;
   private GuiButton modeButton;
   private long lastClickTime = 0;

   static {
      try {
         guiLeftField = ReflectionHelper.findField(GuiContainer.class, "guiLeft", "field_147003_i");
         guiTopField = ReflectionHelper.findField(GuiContainer.class, "guiTop", "field_147009_r");
         xSizeField = ReflectionHelper.findField(GuiContainer.class, "xSize", "field_146999_f");
         hoveredSlotField = ReflectionHelper.findField(GuiContainer.class, "hoveredSlot", "field_147006_u");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private String getSpamText() {
      return spamEnabled ? "§aSpam Slot: ON" : "Spam Slot: OFF";
   }

   private String getModeText() {
      return "Mode: " + currentMode.getDisplayName();
   }

   private boolean isTargetGui(Object gui) {
      return gui instanceof GuiContainer && !(gui instanceof GuiInventory) && !(gui instanceof GuiMerchantOverride);
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

         int btnWidth = 85;
         int btnHeight = 20;
         int btnY = Math.max(2, guiTop - 22);
         int btnX1 = guiLeft + (xSize / 2) - btnWidth - 2;
         int btnX2 = guiLeft + (xSize / 2) + 2;

         this.spamButton = new GuiButton(BTN_SPAM_ID, btnX1, btnY, btnWidth, btnHeight, getSpamText());
         this.modeButton = new GuiButton(BTN_MODE_ID, btnX2, btnY, btnWidth, btnHeight, getModeText());

         event.getButtonList().add(this.spamButton);
         event.getButtonList().add(this.modeButton);
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
      }
   }

   @SubscribeEvent
   public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
      if (!spamEnabled || !isTargetGui(event.getGui())) {
         return;
      }

      int button = Mouse.getEventButton();
      boolean buttonState = Mouse.getEventButtonState();

      if (button == 0 && buttonState) {
         GuiContainer gui = (GuiContainer) event.getGui();
         int mouseX = Mouse.getEventX() * gui.width / this.mc.displayWidth;
         int mouseY = gui.height - Mouse.getEventY() * gui.height / this.mc.displayHeight - 1;

         if (this.spamButton != null && this.spamButton.mousePressed(this.mc, mouseX, mouseY)) {
            return;
         }
         if (this.modeButton != null && this.modeButton.mousePressed(this.mc, mouseX, mouseY)) {
            return;
         }

         Slot slot = getSlotUnderMouse(gui, mouseX, mouseY);
         if (slot != null) {
            event.setCanceled(true);
            executeClick(gui, slot);
            this.lastClickTime = System.currentTimeMillis();
         }
      }
   }

   @SubscribeEvent
   public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
      if (!spamEnabled || !isTargetGui(event.getGui())) {
         return;
      }

      if (Mouse.isButtonDown(0)) {
         int mouseX = event.getMouseX();
         int mouseY = event.getMouseY();

         if (this.spamButton != null && this.spamButton.isMouseOver()) {
            return;
         }
         if (this.modeButton != null && this.modeButton.isMouseOver()) {
            return;
         }

         GuiContainer gui = (GuiContainer) event.getGui();
         Slot slot = getSlotUnderMouse(gui, mouseX, mouseY);
         if (slot != null) {
            long now = System.currentTimeMillis();
            long cooldown = FastTrading.cooldownConfig != null ? FastTrading.cooldownConfig.guiClickCooldown : 100;
            if (now - this.lastClickTime >= cooldown) {
               this.lastClickTime = now;
               executeClick(gui, slot);
            }
         }
      }
   }

   @SubscribeEvent
   public void onGuiOpen(GuiOpenEvent event) {
      this.lastClickTime = 0;
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
