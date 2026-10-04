package com.vgkhiem.fasttrading.client.gui;

import net.minecraft.inventory.ClickType;

public enum GuiClickMode {
   DROP("Drop", 0, ClickType.THROW),
   DROP_ALL("Drop All", 1, ClickType.THROW),
   LEFT("Left", 0, ClickType.PICKUP),
   RIGHT("Right", 1, ClickType.PICKUP),
   SHIFT_LEFT("Shift Left", 0, ClickType.QUICK_MOVE),
   SHIFT_RIGHT("Shift Right", 1, ClickType.QUICK_MOVE);

   private final String displayName;
   private final int mouseButton;
   private final ClickType clickType;

   GuiClickMode(String displayName, int mouseButton, ClickType clickType) {
      this.displayName = displayName;
      this.mouseButton = mouseButton;
      this.clickType = clickType;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public int getMouseButton() {
      return this.mouseButton;
   }

   public ClickType getClickType() {
      return this.clickType;
   }

   public GuiClickMode next() {
      GuiClickMode[] values = values();
      return values[(this.ordinal() + 1) % values.length];
   }
}
