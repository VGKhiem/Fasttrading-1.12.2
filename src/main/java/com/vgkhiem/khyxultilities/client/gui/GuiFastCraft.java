package com.vgkhiem.khyxultilities.client.gui;

import com.vgkhiem.khyxultilities.config.FastCraftConfig;
import com.vgkhiem.khyxultilities.util.FastCraftHelper;
import com.vgkhiem.khyxultilities.util.FastCraftHelper.CraftableRecipe;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiFastCraft {
   public static final int PANEL_WIDTH = 76;

   private static Field guiLeftField;
   private static Field guiTopField;
   private static Field xSizeField;
   private static Field ySizeField;

   private Minecraft mc = Minecraft.getMinecraft();
   private GuiTextField searchField;
   private List<CraftableRecipe> allRecipes = new ArrayList<>();
   private List<CraftableRecipe> filteredRecipes = new ArrayList<>();
   private long lastScanTime = 0;
   private int scrollRow = 0;
   private ItemStack hoveredStack = ItemStack.EMPTY;
   private CraftableRecipe hoveredRecipe = null;
   private String lastSearchText = "";
   private boolean autoCraftTriggeredForCurrentGui = false;

   static {
      try {
         guiLeftField = ReflectionHelper.findField(GuiContainer.class, "guiLeft", "field_147003_i");
         guiTopField = ReflectionHelper.findField(GuiContainer.class, "guiTop", "field_147009_r");
         xSizeField = ReflectionHelper.findField(GuiContainer.class, "xSize", "field_146999_f");
         ySizeField = ReflectionHelper.findField(GuiContainer.class, "ySize", "field_147000_g");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static boolean isApplicable(GuiContainer gui) {
      return FastCraftHelper.isApplicable(gui);
   }

   public static int getPanelX(GuiContainer gui) {
      int guiLeft = (gui.width - 176) / 2;
      int xSize = 176;
      try {
         if (guiLeftField != null) guiLeft = guiLeftField.getInt(gui);
         if (xSizeField != null) xSize = xSizeField.getInt(gui);
      } catch (Exception ignored) {
      }

      FastCraftConfig.ButtonPosition pos = FastCraftConfig.get().getFastCraftPosition();

      int guiActualLeft = guiLeft;
      if (guiLeft > (gui.width - xSize) / 2 + 10) {
         guiActualLeft = guiLeft - 147;
      }

      if (pos == FastCraftConfig.ButtonPosition.LEFT) {
         int x = guiActualLeft - PANEL_WIDTH - 4;
         if (x >= 2) {
            return x;
         }
         return guiLeft + xSize + 4;
      } else {
         int x = guiLeft + xSize + 4;
         if (x + PANEL_WIDTH <= gui.width - 2) {
            return x;
         }
         if (guiActualLeft - PANEL_WIDTH - 4 >= 2) {
            return guiActualLeft - PANEL_WIDTH - 4;
         }
         return x;
      }
   }

   public static int getPanelY(GuiContainer gui) {
      try {
         if (guiTopField != null) return guiTopField.getInt(gui);
      } catch (Exception ignored) {
      }
      return (gui.height - 166) / 2;
   }

   public static int getPanelHeight(GuiContainer gui) {
      try {
         if (ySizeField != null) return ySizeField.getInt(gui);
      } catch (Exception ignored) {
      }
      return 166;
   }

   @SubscribeEvent
   public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
      if (!(event.getGui() instanceof GuiContainer)) {
         return;
      }
      GuiContainer gui = (GuiContainer) event.getGui();
      if (!isApplicable(gui)) {
         return;
      }

      int panelX = getPanelX(gui);
      int panelY = getPanelY(gui);

      this.searchField = new GuiTextField(8801, this.mc.fontRenderer, panelX + 4, panelY + 4, PANEL_WIDTH - 8, 14);
      this.searchField.setMaxStringLength(32);
      this.searchField.setText(lastSearchText);
      this.searchField.setEnableBackgroundDrawing(true);

      refreshRecipes(gui);
      this.scrollRow = 0;
   }

   @SubscribeEvent
   public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
      if (!(event.getGui() instanceof GuiContainer)) {
         return;
      }
      GuiContainer gui = (GuiContainer) event.getGui();
      if (!isApplicable(gui) || !isFastCraftEnabled()) {
         return;
      }

      if (!this.autoCraftTriggeredForCurrentGui) {
         this.autoCraftTriggeredForCurrentGui = true;
         FastCraftHelper.performAutoCraft(gui);
         this.lastScanTime = 0;
         refreshRecipes(gui);
      }

      long now = System.currentTimeMillis();
      if (now - this.lastScanTime > 300) {
         this.lastScanTime = now;
         refreshRecipes(gui);
      }

      int panelX = getPanelX(gui);
      int panelY = getPanelY(gui);
      if (this.searchField != null) {
         this.searchField.x = panelX + 4;
         this.searchField.y = panelY + 4;
      }
   }

   @SubscribeEvent
   public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
      if (!(event.getGui() instanceof GuiContainer)) {
         return;
      }
      GuiContainer gui = (GuiContainer) event.getGui();
      if (!isApplicable(gui) || !isFastCraftEnabled()) {
         return;
      }

      int mouseX = event.getMouseX();
      int mouseY = event.getMouseY();

      int panelX = getPanelX(gui);
      int panelY = getPanelY(gui);
      int panelHeight = getPanelHeight(gui);

      if (this.searchField != null) {
         this.searchField.drawTextBox();
         if (this.searchField.getText().isEmpty() && !this.searchField.isFocused()) {
            this.mc.fontRenderer.drawString("Search...", this.searchField.x + 4, this.searchField.y + 3, 0x777777);
         }
      }

      int startY = panelY + 22;
      int gridHeight = panelHeight - 26;
      int visibleRows = Math.max(1, gridHeight / 20);
      int totalRows = (filteredRecipes.size() + 2) / 3;
      int maxScroll = Math.max(0, totalRows - visibleRows);

      if (this.scrollRow > maxScroll) {
         this.scrollRow = maxScroll;
      }
      if (this.scrollRow < 0) {
         this.scrollRow = 0;
      }

      CraftableRecipe hovered = getRecipeAt(gui, mouseX, mouseY);
      this.hoveredRecipe = hovered;
      this.hoveredStack = hovered != null ? hovered.output : ItemStack.EMPTY;

      for (int r = 0; r < visibleRows; r++) {
         for (int c = 0; c < 3; c++) {
            int index = (this.scrollRow + r) * 3 + c;
            if (index >= filteredRecipes.size()) {
               continue;
            }

            CraftableRecipe cr = filteredRecipes.get(index);
            int slotX = panelX + 4 + c * 21;
            int slotY = startY + r * 20;

            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            boolean isAuto = FastCraftHelper.isAutoCraft(cr.recipe);
            if (isAuto) {
               Gui.drawRect(slotX, slotY, slotX + 19, slotY + 19, 0x60FFEE55);
            }

            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            this.mc.getRenderItem().renderItemAndEffectIntoGUI(cr.output, slotX + 1, slotY + 1);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();

            GlStateManager.disableLighting();

            if (cr.craftableCount <= 0) {
               Gui.drawRect(slotX, slotY, slotX + 19, slotY + 19, 0x44000000);
            }

            if (cr == hovered) {
               Gui.drawRect(slotX, slotY, slotX + 19, slotY + 19, 0x55FFFFFF);
            }

            if (cr.totalOutputCount > 1) {
               String countStr = cr.totalOutputCount > 999 ? "999+" : String.valueOf(cr.totalOutputCount);
               GlStateManager.pushMatrix();
               GlStateManager.disableLighting();
               GlStateManager.disableDepth();
               GlStateManager.scale(0.65F, 0.65F, 1.0F);
               int strW = (int) (this.mc.fontRenderer.getStringWidth(countStr) * 0.65F);
               int txtX = (int) ((slotX + 19 - strW) / 0.65F);
               int txtY = (int) ((slotY + 12) / 0.65F);
               this.mc.fontRenderer.drawStringWithShadow(countStr, txtX, txtY, 0xFFFF55);
               GlStateManager.enableDepth();
               GlStateManager.popMatrix();
            }
         }
      }

      if (maxScroll > 0) {
         int trackX = panelX + PANEL_WIDTH - 6;
         int trackY = startY;
         int trackHeight = visibleRows * 20;

         Gui.drawRect(trackX, trackY, trackX + 3, trackY + trackHeight, 0x44000000);

         int thumbHeight = Math.max(12, trackHeight * visibleRows / totalRows);
         int thumbY = trackY + (trackHeight - thumbHeight) * this.scrollRow / maxScroll;
         Gui.drawRect(trackX, thumbY, trackX + 3, thumbY + thumbHeight, 0xFF888888);
      }

      if (this.hoveredStack != null && !this.hoveredStack.isEmpty()) {
         List<String> tip = this.hoveredStack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
         if (this.hoveredRecipe != null) {
            boolean isAuto = FastCraftHelper.isAutoCraft(this.hoveredRecipe.recipe);
            if (isAuto) {
               tip.add(TextFormatting.GREEN + "✔ Auto-Craft on Open: ON");
               tip.add(TextFormatting.RED + "Right-Click: Remove from Auto-Craft");
            } else {
               tip.add(TextFormatting.GOLD + "Right-Click: Auto Craft");
            }
            if (this.hoveredRecipe.craftableCount > 0) {
               tip.add(TextFormatting.GRAY + "Left-Click: Craft " + TextFormatting.YELLOW + "1");
               tip.add(TextFormatting.GRAY + "Shift-Left Click: " + TextFormatting.YELLOW + "Craft All");
            } else {
               tip.add(TextFormatting.RED + "Missing materials to craft");
            }
         }
         GuiUtils.drawHoveringText(this.hoveredStack, tip, mouseX, mouseY, gui.width, gui.height, -1, this.mc.fontRenderer);
      }
   }

   @SubscribeEvent
   public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
      if (!(event.getGui() instanceof GuiContainer)) {
         return;
      }
      GuiContainer gui = (GuiContainer) event.getGui();
      if (!isApplicable(gui) || !isFastCraftEnabled()) {
         return;
      }

      int mouseX = Mouse.getEventX() * gui.width / this.mc.displayWidth;
      int mouseY = gui.height - Mouse.getEventY() * gui.height / this.mc.displayHeight - 1;

      int panelX = getPanelX(gui);
      int panelY = getPanelY(gui);
      int panelHeight = getPanelHeight(gui);

      int wheel = Mouse.getEventDWheel();
      if (wheel != 0) {
         if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + panelHeight) {
            int gridHeight = panelHeight - 26;
            int visibleRows = Math.max(1, gridHeight / 20);
            int totalRows = (filteredRecipes.size() + 2) / 3;
            int maxScroll = Math.max(0, totalRows - visibleRows);

            if (wheel > 0) {
               this.scrollRow = Math.max(0, this.scrollRow - 1);
            } else {
               this.scrollRow = Math.min(maxScroll, this.scrollRow + 1);
            }
            event.setCanceled(true);
            return;
         }
      }

      int button = Mouse.getEventButton();
      boolean buttonState = Mouse.getEventButtonState();

      if (buttonState) {
         if (button == 0) {
            if (this.searchField != null) {
               this.searchField.mouseClicked(mouseX, mouseY, button);
            }

            CraftableRecipe target = getRecipeAt(gui, mouseX, mouseY);
            if (target != null && target.craftableCount > 0) {
               boolean craftAll = GuiScreen.isShiftKeyDown();
               FastCraftHelper.craftRecipe(gui, target, craftAll);
               this.hoveredRecipe = null;
               this.hoveredStack = ItemStack.EMPTY;
               this.lastScanTime = 0;
               refreshRecipes(gui);
               event.setCanceled(true);
               return;
            }

            if (this.searchField != null && mouseX >= this.searchField.x && mouseX <= this.searchField.x + this.searchField.width &&
                mouseY >= this.searchField.y && mouseY <= this.searchField.y + this.searchField.height) {
               event.setCanceled(true);
            }
         } else if (button == 1) {
            CraftableRecipe target = getRecipeAt(gui, mouseX, mouseY);
            if (target != null) {
               boolean added = FastCraftHelper.toggleAutoCraft(target.recipe);
               this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               if (this.mc.player != null) {
                  String msg = added ?
                     TextFormatting.GREEN + "[FastCraft] Added " + target.displayName + " to Auto-Craft list." :
                     TextFormatting.RED + "[FastCraft] Removed " + target.displayName + " from Auto-Craft list.";
                  this.mc.player.sendMessage(new TextComponentString(msg));
               }
               refreshRecipes(gui);
               event.setCanceled(true);
               return;
            }
         }
      }
   }

   @SubscribeEvent
   public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
      if (!(event.getGui() instanceof GuiContainer)) {
         return;
      }
      GuiContainer gui = (GuiContainer) event.getGui();
      if (!isApplicable(gui) || !isFastCraftEnabled()) {
         return;
      }

      if (this.searchField != null && this.searchField.isFocused()) {
         if (Keyboard.getEventKeyState()) {
            char c = Keyboard.getEventCharacter();
            int key = Keyboard.getEventKey();

            if (key == Keyboard.KEY_ESCAPE) {
               this.searchField.setFocused(false);
               event.setCanceled(true);
               return;
            }

            this.searchField.textboxKeyTyped(c, key);
            this.lastSearchText = this.searchField.getText();
            filterRecipes();
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public void onGuiOpen(GuiOpenEvent event) {
      this.autoCraftTriggeredForCurrentGui = false;
      if (event.getGui() == null) {
         this.searchField = null;
         this.hoveredStack = ItemStack.EMPTY;
         this.hoveredRecipe = null;
      }
   }

   private void refreshRecipes(GuiContainer gui) {
      this.allRecipes = FastCraftHelper.findAllDisplayRecipes(gui);
      filterRecipes();
   }

   private void filterRecipes() {
      String query = this.searchField != null ? this.searchField.getText().trim().toLowerCase() : "";
      if (query.isEmpty()) {
         this.filteredRecipes = new ArrayList<>(this.allRecipes);
      } else {
         this.filteredRecipes = new ArrayList<>();
         for (CraftableRecipe cr : this.allRecipes) {
            if (cr.displayName.toLowerCase().contains(query)) {
               this.filteredRecipes.add(cr);
            }
         }
      }
   }

   private CraftableRecipe getRecipeAt(GuiContainer gui, int mouseX, int mouseY) {
      if (!isApplicable(gui) || !isFastCraftEnabled()) {
         return null;
      }
      int panelX = getPanelX(gui);
      int panelY = getPanelY(gui);
      int panelHeight = getPanelHeight(gui);
      int startY = panelY + 22;
      int gridHeight = panelHeight - 26;
      int visibleRows = Math.max(1, gridHeight / 20);

      for (int r = 0; r < visibleRows; r++) {
         int slotY = startY + r * 20;
         if (mouseY >= slotY && mouseY < slotY + 20) {
            for (int c = 0; c < 3; c++) {
               int slotX = panelX + 4 + c * 21;
               if (mouseX >= slotX && mouseX < slotX + 21) {
                  int index = (this.scrollRow + r) * 3 + c;
                  if (index >= 0 && index < this.filteredRecipes.size()) {
                     return this.filteredRecipes.get(index);
                  }
               }
            }
         }
      }
      return null;
   }

   private boolean isFastCraftEnabled() {
      return FastCraftConfig.get().isFastCraftEnabled();
   }
}
