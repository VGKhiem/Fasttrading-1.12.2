package com.vgkhiem.khyxultilities.client.gui;

import com.vgkhiem.khyxultilities.FastTrading;
import com.vgkhiem.khyxultilities.config.CooldownConfig;
import com.vgkhiem.khyxultilities.config.FastCraftConfig;
import com.vgkhiem.khyxultilities.config.ProxyConfig;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public class FastTradingConfigGui extends GuiScreen {
   private final GuiScreen parentScreen;
   private GuiTextField fastTradeField;
   private GuiTextField guiClickField;
   private GuiButton autoTradeButton;
   private GuiButton positionButton;
   private GuiButton proxyPositionButton;
   private GuiButton fastCraftButton;
   private GuiButton fastCraftPosButton;
   private GuiButton autoCraftButton;
   private GuiButton doneButton;
   private GuiButton resetButton;
   private GuiButton cancelButton;
   private boolean currentAuto;
   private boolean currentFastCraft;
   private boolean currentAutoCraft;
   private CooldownConfig.ButtonPosition currentPos;
   private FastCraftConfig.ButtonPosition currentFastCraftPos;
   private ProxyConfig.ButtonPosition currentProxyPos;

   public FastTradingConfigGui(GuiScreen parentScreen) {
      this.parentScreen = parentScreen;
      this.currentAuto = FastTrading.configLoader != null && FastTrading.configLoader.config != null && FastTrading.configLoader.config.isAuto;
      this.currentPos = FastTrading.cooldownConfig != null ? FastTrading.cooldownConfig.getButtonPosition() : CooldownConfig.ButtonPosition.RIGHT;
      this.currentFastCraft = FastCraftConfig.get().isFastCraftEnabled();
      this.currentFastCraftPos = FastCraftConfig.get().getFastCraftPosition();
      this.currentAutoCraft = FastCraftConfig.get().isAutoCraftEnabled();
      this.currentProxyPos = ProxyConfig.get().getButtonPosition();
   }

   @Override
   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();

      int centerX = this.width / 2;
      int col1X = centerX - 155;
      int col2X = centerX + 10;
      int colW = 145;

      this.fastTradeField = new GuiTextField(101, this.fontRenderer, col1X, 44, colW, 20);
      this.fastTradeField.setMaxStringLength(6);
      int ftCd = FastTrading.cooldownConfig != null ? FastTrading.cooldownConfig.fastTradeCooldown : 50;
      this.fastTradeField.setText(String.valueOf(ftCd));

      this.guiClickField = new GuiTextField(102, this.fontRenderer, col1X, 84, colW, 20);
      this.guiClickField.setMaxStringLength(6);
      int gcCd = FastTrading.cooldownConfig != null ? FastTrading.cooldownConfig.guiClickCooldown : 100;
      this.guiClickField.setText(String.valueOf(gcCd));

      this.autoTradeButton = new GuiButton(1001, col1X, 124, colW, 20, getAutoTradeText());
      this.buttonList.add(this.autoTradeButton);

      this.fastCraftButton = new GuiButton(1007, col1X, 164, colW, 20, getFastCraftText());
      this.buttonList.add(this.fastCraftButton);

      this.positionButton = new GuiButton(1005, col2X, 44, colW, 20, getPositionText());
      this.buttonList.add(this.positionButton);

      this.proxyPositionButton = new GuiButton(1006, col2X, 84, colW, 20, getProxyPositionText());
      this.buttonList.add(this.proxyPositionButton);

      this.fastCraftPosButton = new GuiButton(1008, col2X, 124, colW, 20, getFastCraftPosText());
      this.buttonList.add(this.fastCraftPosButton);

      this.autoCraftButton = new GuiButton(1009, col2X, 164, colW, 20, getAutoCraftText());
      this.buttonList.add(this.autoCraftButton);

      int bottomY = this.height - 35;
      this.doneButton = new GuiButton(1002, centerX - 155, bottomY, 100, 20, "Save & Close");
      this.resetButton = new GuiButton(1003, centerX - 50, bottomY, 100, 20, "Reset Default");
      this.cancelButton = new GuiButton(1004, centerX + 55, bottomY, 100, 20, "Cancel");

      this.buttonList.add(this.doneButton);
      this.buttonList.add(this.resetButton);
      this.buttonList.add(this.cancelButton);
   }

   private String getAutoTradeText() {
      return "Auto Trade: " + (this.currentAuto ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF");
   }

   private String getPositionText() {
      return "Spam Pos: " + TextFormatting.YELLOW + this.currentPos.getDisplayName();
   }

   private String getProxyPositionText() {
      return "Proxy Pos: " + TextFormatting.YELLOW + this.currentProxyPos.getDisplayName();
   }

   private String getFastCraftText() {
      return "Fast Craft: " + (this.currentFastCraft ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF");
   }

   private String getAutoCraftText() {
      return "Auto Craft: " + (this.currentAutoCraft ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF");
   }

   private String getFastCraftPosText() {
      return "Craft Pos: " + TextFormatting.YELLOW + this.currentFastCraftPos.getDisplayName();
   }

   @Override
   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
      if (this.fastTradeField != null) {
         this.fastTradeField.updateCursorCounter();
      }
      if (this.guiClickField != null) {
         this.guiClickField.updateCursorCounter();
      }
   }

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.fastTradeField != null) {
         this.fastTradeField.mouseClicked(mouseX, mouseY, mouseButton);
      }
      if (this.guiClickField != null) {
         this.guiClickField.mouseClicked(mouseX, mouseY, mouseButton);
      }
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (keyCode == Keyboard.KEY_ESCAPE) {
         this.mc.displayGuiScreen(this.parentScreen);
         return;
      }

      if (this.fastTradeField != null && this.fastTradeField.isFocused()) {
         if (Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE ||
             keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT || keyCode == Keyboard.KEY_HOME || keyCode == Keyboard.KEY_END ||
             isKeyComboCtrlA(keyCode) || isKeyComboCtrlC(keyCode) || isKeyComboCtrlV(keyCode) || isKeyComboCtrlX(keyCode)) {
            this.fastTradeField.textboxKeyTyped(typedChar, keyCode);
            String text = this.fastTradeField.getText();
            String filtered = text.replaceAll("[^0-9]", "");
            if (!text.equals(filtered)) {
               this.fastTradeField.setText(filtered);
            }
         }
         return;
      }

      if (this.guiClickField != null && this.guiClickField.isFocused()) {
         if (Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE ||
             keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT || keyCode == Keyboard.KEY_HOME || keyCode == Keyboard.KEY_END ||
             isKeyComboCtrlA(keyCode) || isKeyComboCtrlC(keyCode) || isKeyComboCtrlV(keyCode) || isKeyComboCtrlX(keyCode)) {
            this.guiClickField.textboxKeyTyped(typedChar, keyCode);
            String text = this.guiClickField.getText();
            String filtered = text.replaceAll("[^0-9]", "");
            if (!text.equals(filtered)) {
               this.guiClickField.setText(filtered);
            }
         }
         return;
      }

      super.keyTyped(typedChar, keyCode);
   }

   @Override
   protected void actionPerformed(GuiButton button) {
      if (button.id == 1001) {
         this.currentAuto = !this.currentAuto;
         this.autoTradeButton.displayString = getAutoTradeText();
      } else if (button.id == 1005) {
         this.currentPos = this.currentPos.next();
         this.positionButton.displayString = getPositionText();
      } else if (button.id == 1006) {
         this.currentProxyPos = this.currentProxyPos.next();
         this.proxyPositionButton.displayString = getProxyPositionText();
      } else if (button.id == 1007) {
         this.currentFastCraft = !this.currentFastCraft;
         this.fastCraftButton.displayString = getFastCraftText();
      } else if (button.id == 1008) {
         this.currentFastCraftPos = (this.currentFastCraftPos == FastCraftConfig.ButtonPosition.RIGHT) ?
            FastCraftConfig.ButtonPosition.LEFT : FastCraftConfig.ButtonPosition.RIGHT;
         this.fastCraftPosButton.displayString = getFastCraftPosText();
      } else if (button.id == 1009) {
         this.currentAutoCraft = !this.currentAutoCraft;
         this.autoCraftButton.displayString = getAutoCraftText();
      } else if (button.id == 1002) {
         try {
            int ft = Integer.parseInt(this.fastTradeField.getText().trim());
            if (ft >= 0 && FastTrading.cooldownConfig != null) {
               FastTrading.cooldownConfig.fastTradeCooldown = ft;
            }
         } catch (NumberFormatException ignored) {
         }

         try {
            int gc = Integer.parseInt(this.guiClickField.getText().trim());
            if (gc >= 0 && FastTrading.cooldownConfig != null) {
               FastTrading.cooldownConfig.guiClickCooldown = gc;
            }
         } catch (NumberFormatException ignored) {
         }

         if (FastTrading.cooldownConfig != null) {
            FastTrading.cooldownConfig.setButtonPosition(this.currentPos);
            CooldownConfig.save(FastTrading.cooldownConfig);
         }

         FastCraftConfig fcc = FastCraftConfig.get();
         fcc.setFastCraftEnabled(this.currentFastCraft);
         fcc.setFastCraftPosition(this.currentFastCraftPos);
         fcc.setAutoCraftEnabled(this.currentAutoCraft);
         FastCraftConfig.save(fcc);

         if (FastTrading.configLoader != null && FastTrading.configLoader.config != null) {
            FastTrading.configLoader.config.isAuto = this.currentAuto;
            FastTrading.configLoader.save();
         }

         ProxyConfig.get().setButtonPosition(this.currentProxyPos);
         ProxyConfig.save();

         this.mc.displayGuiScreen(this.parentScreen);
      } else if (button.id == 1003) {
         this.fastTradeField.setText("50");
         this.guiClickField.setText("100");
         this.currentAuto = true;
         this.currentPos = CooldownConfig.ButtonPosition.RIGHT;
         this.currentProxyPos = ProxyConfig.ButtonPosition.TOP_RIGHT;
         this.currentFastCraft = true;
         this.currentFastCraftPos = FastCraftConfig.ButtonPosition.RIGHT;
         this.autoTradeButton.displayString = getAutoTradeText();
         this.positionButton.displayString = getPositionText();
         this.proxyPositionButton.displayString = getProxyPositionText();
         this.fastCraftButton.displayString = getFastCraftText();
         this.fastCraftPosButton.displayString = getFastCraftPosText();
         this.currentAutoCraft = true;
         this.autoCraftButton.displayString = getAutoCraftText();
      } else if (button.id == 1004) {
         this.mc.displayGuiScreen(this.parentScreen);
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      int centerX = this.width / 2;
      int col1X = centerX - 155;
      int col2X = centerX + 10;

      this.drawString(this.fontRenderer, "Fast Trade Cooldown (ms):", col1X, 32, 0xDDDDDD);
      this.drawString(this.fontRenderer, "GUI Click Cooldown (ms):", col1X, 72, 0xDDDDDD);

      this.drawString(this.fontRenderer, "Spam Clicker Position:", col2X, 32, 0xDDDDDD);
      this.drawString(this.fontRenderer, "Multiplayer Proxy Position:", col2X, 72, 0xDDDDDD);
      this.drawString(this.fontRenderer, "Fast Craft Position:", col2X, 112, 0xDDDDDD);

      if (this.fastTradeField != null) {
         this.fastTradeField.drawTextBox();
      }
      if (this.guiClickField != null) {
         this.guiClickField.drawTextBox();
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }
}
