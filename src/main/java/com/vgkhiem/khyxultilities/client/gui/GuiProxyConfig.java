package com.vgkhiem.khyxultilities.client.gui;

import com.vgkhiem.khyxultilities.config.ProxyConfig;
import com.vgkhiem.khyxultilities.config.ProxyEntry;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public class GuiProxyConfig extends GuiScreen {
   private final GuiScreen parentScreen;
   private ProxyListSlot slotList;
   private int selectedSlot = -1;

   private GuiButton toggleButton;
   private GuiButton useButton;
   private GuiButton testButton;
   private GuiButton addButton;
   private GuiButton editButton;
   private GuiButton deleteButton;
   private GuiButton doneButton;

   private final Map<Integer, String> pingResults = new ConcurrentHashMap<Integer, String>();
   private volatile boolean isTestingAll = false;

   public GuiProxyConfig(GuiScreen parentScreen) {
      this.parentScreen = parentScreen;
      ProxyConfig cfg = ProxyConfig.get();
      if (!cfg.proxies.isEmpty()) {
         this.selectedSlot = Math.min(cfg.activeIndex, cfg.proxies.size() - 1);
      }
   }

   @Override
   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();

      int centerX = this.width / 2;

      this.slotList = new ProxyListSlot(this.mc, this.width, this.height, 46, this.height - 56, 32);

      this.toggleButton = new GuiButton(1001, centerX - 100, 20, 200, 20, getToggleText());
      this.buttonList.add(this.toggleButton);

      int startX = centerX - 154;
      int row1Y = this.height - 52;
      this.useButton = new GuiButton(1002, startX, row1Y, 100, 20, "Use as Active");
      this.testButton = new GuiButton(1003, startX + 104, row1Y, 100, 20, "Test Connection");
      this.addButton = new GuiButton(1004, startX + 208, row1Y, 100, 20, "Add Proxy");

      this.buttonList.add(this.useButton);
      this.buttonList.add(this.testButton);
      this.buttonList.add(this.addButton);

      int row2Y = this.height - 28;
      this.editButton = new GuiButton(1005, startX, row2Y, 100, 20, "Edit");
      this.deleteButton = new GuiButton(1006, startX + 104, row2Y, 100, 20, "Delete");
      this.doneButton = new GuiButton(1007, startX + 208, row2Y, 100, 20, "Done");

      this.buttonList.add(this.editButton);
      this.buttonList.add(this.deleteButton);
      this.buttonList.add(this.doneButton);

      updateButtonStates();
   }

   private String getToggleText() {
      boolean enabled = ProxyConfig.get().isEnabled();
      return "Proxy: " + (enabled ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF");
   }

   private void updateButtonStates() {
      ProxyConfig cfg = ProxyConfig.get();
      boolean hasSelection = this.selectedSlot >= 0 && this.selectedSlot < cfg.proxies.size();
      this.useButton.enabled = hasSelection && this.selectedSlot != cfg.activeIndex;
      this.testButton.enabled = !cfg.proxies.isEmpty() && !this.isTestingAll;
      this.editButton.enabled = hasSelection;
      this.deleteButton.enabled = hasSelection;
   }

   @Override
   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   @Override
   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      if (this.slotList != null) {
         this.slotList.handleMouseInput();
      }
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (keyCode == Keyboard.KEY_ESCAPE) {
         ProxyConfig.save();
         this.mc.displayGuiScreen(this.parentScreen);
         return;
      }

      ProxyConfig cfg = ProxyConfig.get();
      if (keyCode == Keyboard.KEY_UP && this.selectedSlot > 0) {
         this.selectedSlot--;
         updateButtonStates();
         return;
      }
      if (keyCode == Keyboard.KEY_DOWN && this.selectedSlot < cfg.proxies.size() - 1) {
         this.selectedSlot++;
         updateButtonStates();
         return;
      }
      if (keyCode == Keyboard.KEY_DELETE && this.selectedSlot >= 0 && this.selectedSlot < cfg.proxies.size()) {
         actionPerformed(this.deleteButton);
         return;
      }

      super.keyTyped(typedChar, keyCode);
   }

   @Override
   protected void actionPerformed(GuiButton button) throws IOException {
      if (!button.enabled) return;

      ProxyConfig cfg = ProxyConfig.get();

      if (button.id == 1001) {
         cfg.setEnabled(!cfg.isEnabled());
         ProxyConfig.save();
         this.toggleButton.displayString = getToggleText();
      } else if (button.id == 1002) {
         if (this.selectedSlot >= 0 && this.selectedSlot < cfg.proxies.size()) {
            cfg.activeIndex = this.selectedSlot;
            ProxyConfig.save();
            updateButtonStates();
         }
      } else if (button.id == 1003) {
         if (!cfg.proxies.isEmpty() && !this.isTestingAll) {
            this.isTestingAll = true;
            this.testButton.enabled = false;
            this.testButton.displayString = "Testing...";
            final java.util.concurrent.atomic.AtomicInteger remaining = new java.util.concurrent.atomic.AtomicInteger(cfg.proxies.size());
            for (int i = 0; i < cfg.proxies.size(); i++) {
               final int slotIndex = i;
               final ProxyEntry entry = cfg.proxies.get(i);
               if (entry != null && entry.isValid()) {
                  pingResults.put(slotIndex, TextFormatting.YELLOW + "Testing...");
                  new Thread(new Runnable() {
                     @Override
                     public void run() {
                        try {
                           String res = ProxyConfig.pingTest(entry.host, entry.port, 3000);
                           if (res.startsWith("OK:")) {
                              pingResults.put(slotIndex, TextFormatting.GREEN + res.substring(3) + "ms");
                           } else {
                              pingResults.put(slotIndex, TextFormatting.RED + "Failed");
                           }
                        } catch (Exception ignored) {
                           pingResults.put(slotIndex, TextFormatting.RED + "Error");
                        } finally {
                           if (remaining.decrementAndGet() == 0) {
                              isTestingAll = false;
                              testButton.displayString = "Test Connection";
                              updateButtonStates();
                           }
                        }
                     }
                  }).start();
               } else {
                  pingResults.put(slotIndex, TextFormatting.RED + "Invalid");
                  if (remaining.decrementAndGet() == 0) {
                     this.isTestingAll = false;
                     this.testButton.displayString = "Test Connection";
                     updateButtonStates();
                  }
               }
            }
         }
      } else if (button.id == 1004) {
         this.mc.displayGuiScreen(new GuiEditProxy(this, null, -1));
      } else if (button.id == 1005) {
         if (this.selectedSlot >= 0 && this.selectedSlot < cfg.proxies.size()) {
            ProxyEntry entry = cfg.proxies.get(this.selectedSlot);
            this.mc.displayGuiScreen(new GuiEditProxy(this, entry, this.selectedSlot));
         }
      } else if (button.id == 1006) {
         if (this.selectedSlot >= 0 && this.selectedSlot < cfg.proxies.size()) {
            cfg.proxies.remove(this.selectedSlot);
            pingResults.remove(this.selectedSlot);
            if (this.selectedSlot >= cfg.proxies.size()) {
               this.selectedSlot = cfg.proxies.size() - 1;
            }
            if (cfg.activeIndex >= cfg.proxies.size()) {
               cfg.activeIndex = Math.max(0, cfg.proxies.size() - 1);
            }
            ProxyConfig.save();
            updateButtonStates();
         }
      } else if (button.id == 1007) {
         ProxyConfig.save();
         this.mc.displayGuiScreen(this.parentScreen);
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      drawDefaultBackground();

      if (this.slotList != null) {
         this.slotList.drawScreen(mouseX, mouseY, partialTicks);
      }

      int centerX = this.width / 2;
      drawCenteredString(this.fontRenderer, TextFormatting.BOLD + "Multiplayer Proxy Manager", centerX, 7, 0xFFFFFF);

      ProxyConfig cfg = ProxyConfig.get();
      if (cfg.proxies.isEmpty()) {
         drawCenteredString(this.fontRenderer, TextFormatting.GRAY + "No proxies added. Click 'Add Proxy' below.", centerX, this.height / 2 - 10, 0xAAAAAA);
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private class ProxyListSlot extends GuiSlot {
      public ProxyListSlot(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
         super(mc, width, height, top, bottom, slotHeight);
      }

      @Override
      protected int getSize() {
         return ProxyConfig.get().proxies.size();
      }

      @Override
      protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
         selectedSlot = slotIndex;
         ProxyConfig cfg = ProxyConfig.get();
         if (isDoubleClick) {
            cfg.activeIndex = slotIndex;
            ProxyConfig.save();
         }
         updateButtonStates();
      }

      @Override
      protected boolean isSelected(int slotIndex) {
         return slotIndex == selectedSlot;
      }

      @Override
      protected void drawBackground() {
      }

      @Override
      public int getListWidth() {
         return 240;
      }

      @Override
      protected int getScrollBarX() {
         return this.width / 2 + 120 + 6;
      }

      @Override
      protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks) {
         ProxyConfig cfg = ProxyConfig.get();
         if (entryID < 0 || entryID >= cfg.proxies.size()) return;

         ProxyEntry entry = cfg.proxies.get(entryID);
         boolean isActive = (entryID == cfg.activeIndex);

         String prefix = isActive ? TextFormatting.GREEN + "[ACTIVE] " : TextFormatting.DARK_GRAY + "[     ] ";
         String nameDisplay = prefix + (isActive ? TextFormatting.WHITE : TextFormatting.GRAY) + entry.name;
         fontRenderer.drawString(nameDisplay, insideLeft + 2, yPos + 3, 0xFFFFFF);

         String ipPort = entry.host + (entry.port > 0 ? ":" + entry.port : "");
         int ipWidth = fontRenderer.getStringWidth(ipPort);
         fontRenderer.drawString(TextFormatting.YELLOW + ipPort, insideLeft + 236 - ipWidth, yPos + 3, 0xFFFFFF);

         String subInfo = TextFormatting.DARK_GRAY + entry.getType().getDisplayName() + (entry.hasAuth() ? " " + TextFormatting.GOLD + "(Auth)" : "");
         fontRenderer.drawString(subInfo, insideLeft + 16, yPos + 16, 0x888888);

         String ping = pingResults.get(entryID);
         if (ping != null) {
            int pingW = fontRenderer.getStringWidth(ping);
            fontRenderer.drawString(ping, insideLeft + 236 - pingW, yPos + 16, 0xFFFFFF);
         }
      }
   }
}
