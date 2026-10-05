package com.vgkhiem.khyxultilities.client.gui;

import com.vgkhiem.khyxultilities.config.ProxyConfig;
import com.vgkhiem.khyxultilities.config.ProxyEntry;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public class GuiEditProxy extends GuiScreen {
   private final GuiScreen parentScreen;
   private final ProxyEntry targetEntry;
   private final int editIndex;

   private GuiTextField quickImportField;
   private GuiTextField nameField;
   private GuiTextField hostField;
   private GuiTextField portField;
   private GuiTextField usernameField;
   private GuiTextField passwordField;

   private GuiButton parseButton;
   private GuiButton typeButton;
   private GuiButton testButton;
   private GuiButton saveButton;
   private GuiButton cancelButton;

   private ProxyEntry.Type currentType;
   private String testResult = "";
   private volatile boolean isTesting = false;

   public GuiEditProxy(GuiScreen parentScreen, ProxyEntry entry, int editIndex) {
      this.parentScreen = parentScreen;
      this.targetEntry = entry != null ? entry.copy() : new ProxyEntry("Proxy", "SOCKS5", "", 0, "", "");
      this.editIndex = editIndex;
      this.currentType = this.targetEntry.getType();
   }

   @Override
   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();

      int centerX = this.width / 2;

      this.quickImportField = new GuiTextField(100, this.fontRenderer, centerX - 110, 34, 150, 20);
      this.quickImportField.setMaxStringLength(256);

      this.parseButton = new GuiButton(1001, centerX + 45, 34, 65, 20, "Import");
      this.buttonList.add(this.parseButton);

      this.nameField = new GuiTextField(101, this.fontRenderer, centerX - 110, 72, 135, 20);
      this.nameField.setMaxStringLength(32);
      this.nameField.setText(this.targetEntry.name != null ? this.targetEntry.name : "Proxy");

      this.typeButton = new GuiButton(1002, centerX + 30, 72, 80, 20, getTypeButtonText());
      this.buttonList.add(this.typeButton);

      this.hostField = new GuiTextField(102, this.fontRenderer, centerX - 110, 108, 150, 20);
      this.hostField.setMaxStringLength(128);
      this.hostField.setText(this.targetEntry.host != null ? this.targetEntry.host : "");

      this.portField = new GuiTextField(103, this.fontRenderer, centerX + 50, 108, 60, 20);
      this.portField.setMaxStringLength(5);
      this.portField.setText(this.targetEntry.port > 0 ? String.valueOf(this.targetEntry.port) : "");

      this.usernameField = new GuiTextField(104, this.fontRenderer, centerX - 110, 144, 220, 20);
      this.usernameField.setMaxStringLength(64);
      this.usernameField.setText(this.targetEntry.username != null ? this.targetEntry.username : "");

      this.passwordField = new GuiTextField(105, this.fontRenderer, centerX - 110, 180, 220, 20);
      this.passwordField.setMaxStringLength(64);
      this.passwordField.setText(this.targetEntry.password != null ? this.targetEntry.password : "");

      this.testButton = new GuiButton(1003, centerX - 110, 206, 105, 20, "Test Connection");
      this.buttonList.add(this.testButton);

      int bottomY = this.height - 28;
      this.saveButton = new GuiButton(1004, centerX - 110, bottomY, 105, 20, "Save");
      this.cancelButton = new GuiButton(1005, centerX + 5, bottomY, 105, 20, "Cancel");
      this.buttonList.add(this.saveButton);
      this.buttonList.add(this.cancelButton);
   }

   private String getTypeButtonText() {
      return TextFormatting.YELLOW + this.currentType.getDisplayName();
   }

   @Override
   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
      if (this.quickImportField != null) this.quickImportField.updateCursorCounter();
      if (this.nameField != null) this.nameField.updateCursorCounter();
      if (this.hostField != null) this.hostField.updateCursorCounter();
      if (this.portField != null) this.portField.updateCursorCounter();
      if (this.usernameField != null) this.usernameField.updateCursorCounter();
      if (this.passwordField != null) this.passwordField.updateCursorCounter();
   }

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.quickImportField != null) this.quickImportField.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.nameField != null) this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.hostField != null) this.hostField.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.portField != null) this.portField.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.usernameField != null) this.usernameField.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.passwordField != null) this.passwordField.mouseClicked(mouseX, mouseY, mouseButton);
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (keyCode == Keyboard.KEY_ESCAPE) {
         this.mc.displayGuiScreen(this.parentScreen);
         return;
      }

      if (keyCode == Keyboard.KEY_TAB) {
         if (this.quickImportField.isFocused()) {
            this.quickImportField.setFocused(false);
            this.nameField.setFocused(true);
         } else if (this.nameField.isFocused()) {
            this.nameField.setFocused(false);
            this.hostField.setFocused(true);
         } else if (this.hostField.isFocused()) {
            this.hostField.setFocused(false);
            this.portField.setFocused(true);
         } else if (this.portField.isFocused()) {
            this.portField.setFocused(false);
            this.usernameField.setFocused(true);
         } else if (this.usernameField.isFocused()) {
            this.usernameField.setFocused(false);
            this.passwordField.setFocused(true);
         } else if (this.passwordField.isFocused()) {
            this.passwordField.setFocused(false);
            this.nameField.setFocused(true);
         } else {
            this.nameField.setFocused(true);
         }
         return;
      }

      if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
         if (this.quickImportField.isFocused()) {
            actionPerformed(this.parseButton);
         } else {
            actionPerformed(this.saveButton);
         }
         return;
      }

      if (this.portField != null && this.portField.isFocused()) {
         if (Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT) {
            this.portField.textboxKeyTyped(typedChar, keyCode);
         }
         return;
      }

      if (this.quickImportField != null && this.quickImportField.textboxKeyTyped(typedChar, keyCode)) return;
      if (this.nameField != null && this.nameField.textboxKeyTyped(typedChar, keyCode)) return;
      if (this.hostField != null && this.hostField.textboxKeyTyped(typedChar, keyCode)) return;
      if (this.usernameField != null && this.usernameField.textboxKeyTyped(typedChar, keyCode)) return;
      if (this.passwordField != null && this.passwordField.textboxKeyTyped(typedChar, keyCode)) return;

      super.keyTyped(typedChar, keyCode);
   }

   @Override
   protected void actionPerformed(GuiButton button) throws IOException {
      if (!button.enabled) return;

      if (button.id == 1001) {
         String text = this.quickImportField.getText().trim();
         if (!text.isEmpty()) {
            String[] parts = text.split(":");
            if (parts.length >= 2) {
               this.hostField.setText(parts[0].trim());
               this.portField.setText(parts[1].trim());
               if (parts.length >= 4) {
                  this.usernameField.setText(parts[2].trim());
                  this.passwordField.setText(parts[3].trim());
               } else if (parts.length == 3) {
                  this.usernameField.setText(parts[2].trim());
               }
               this.quickImportField.setText("");
               this.testResult = TextFormatting.GREEN + "Imported!";
            } else {
               this.testResult = TextFormatting.RED + "Format: ip:port:user:pass";
            }
         }
      } else if (button.id == 1002) {
         this.currentType = this.currentType.next();
         this.typeButton.displayString = getTypeButtonText();
      } else if (button.id == 1003) {
         int testP = 0;
         try {
            String pStr = this.portField.getText().trim();
            if (!pStr.isEmpty()) testP = Integer.parseInt(pStr);
         } catch (Exception ignored) {
         }

         String testH = this.hostField.getText().trim();
         if (testH.isEmpty() || testP <= 0 || testP > 65535) {
            this.testResult = TextFormatting.RED + "Invalid IP/Port";
            return;
         }

         this.testResult = TextFormatting.YELLOW + "Testing...";
         this.isTesting = true;
         this.testButton.enabled = false;
         final int finalP = testP;
         final String finalH = testH;
         new Thread(new Runnable() {
            @Override
            public void run() {
               String res = ProxyConfig.pingTest(finalH, finalP, 3000);
               isTesting = false;
               if (res.startsWith("OK:")) {
                  testResult = TextFormatting.GREEN + "Online (" + res.substring(3) + "ms)";
               } else {
                  testResult = TextFormatting.RED + "Failed (" + res.substring(4) + ")";
               }
               testButton.enabled = true;
            }
         }).start();
      } else if (button.id == 1004) {
         int saveP = 0;
         try {
            String pStr = this.portField.getText().trim();
            if (!pStr.isEmpty()) saveP = Integer.parseInt(pStr);
         } catch (Exception ignored) {
         }

         String name = this.nameField.getText().trim();
         if (name.isEmpty()) name = "Proxy";
         String host = this.hostField.getText().trim();
         String user = this.usernameField.getText().trim();
         String pass = this.passwordField.getText();

         ProxyConfig cfg = ProxyConfig.get();
         if (this.editIndex >= 0 && this.editIndex < cfg.proxies.size()) {
            ProxyEntry e = cfg.proxies.get(this.editIndex);
            e.name = name;
            e.setType(this.currentType);
            e.host = host;
            e.port = saveP;
            e.username = user;
            e.password = pass;
         } else {
            ProxyEntry e = new ProxyEntry(name, this.currentType.name(), host, saveP, user, pass);
            cfg.proxies.add(e);
            cfg.activeIndex = cfg.proxies.size() - 1;
         }
         ProxyConfig.save();

         this.mc.displayGuiScreen(this.parentScreen);
      } else if (button.id == 1005) {
         this.mc.displayGuiScreen(this.parentScreen);
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      drawDefaultBackground();

      int centerX = this.width / 2;

      drawCenteredString(this.fontRenderer, TextFormatting.BOLD + (this.editIndex >= 0 ? "Edit Proxy" : "Add New Proxy"), centerX, 10, 0xFFFFFF);

      drawString(this.fontRenderer, "Quick Paste (ip:port:user:pass):", centerX - 110, 24, 0x888888);
      drawString(this.fontRenderer, "Name:", centerX - 110, 61, 0xAAAAAA);
      drawString(this.fontRenderer, "Type:", centerX + 30, 61, 0xAAAAAA);
      drawString(this.fontRenderer, "IP:", centerX - 110, 97, 0xAAAAAA);
      drawString(this.fontRenderer, "Port:", centerX + 50, 97, 0xAAAAAA);
      drawString(this.fontRenderer, "Username (Optional):", centerX - 110, 133, 0xAAAAAA);
      drawString(this.fontRenderer, "Password (Optional):", centerX - 110, 169, 0xAAAAAA);

      if (this.testResult != null && !this.testResult.isEmpty()) {
         drawString(this.fontRenderer, this.testResult, centerX + 5, 212, 0xFFFFFF);
      }

      super.drawScreen(mouseX, mouseY, partialTicks);

      if (this.quickImportField != null) this.quickImportField.drawTextBox();
      if (this.nameField != null) this.nameField.drawTextBox();
      if (this.hostField != null) this.hostField.drawTextBox();
      if (this.portField != null) this.portField.drawTextBox();
      if (this.usernameField != null) this.usernameField.drawTextBox();
      if (this.passwordField != null) this.passwordField.drawTextBox();
   }
}
