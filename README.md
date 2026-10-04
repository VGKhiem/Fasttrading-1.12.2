# KhyxUltilities 1.12.2

A Minecraft Forge 1.12.2 client-side utility mod featuring Fast Villager Trading, Alt NBT Tooltip Inspector, Custom GUI Slot Auto-Clicker, and Slot ID Visualizer.

**Author**: VGKhiem  
**Minecraft Version**: 1.12.2  
**Forge Version**: 14.23.5.2847+  

---

## Features

### 1. Fast Villager Trading
- **Fast Trading Sidebar**: Integrated alongside the villager trading GUI for quick trade selection and execution.
- **One-Click & Shift-Click Trading**: Trade instantly with a single click or Shift-click to bulk trade until resources run out.
- **Price Locking & Auto-Trading**: Lock desired trade recipes and automate repeated trades.
- **Sound Feedback**: Custom audio cues for successful trades (villager yes / anvil) and failure (villager no).
- **Crash & Freeze Fixes**:
  - Resolved infinite loop client freeze/crash when a trade requires 2 items of the same type.
  - Accurate NBT matching for custom items using `ItemStack.areItemStackTagsEqual`.
  - Resolved OptiFine Fast Render conflict causing invisible GUI (OpenGL matrix stack underflow error 1284 and dirty GL state).

### 2. Alt NBT Tooltip Inspector
- **Hold `Alt`** while hovering over any item in inventories, trading windows, or custom menus to inspect hidden item properties:
  - Hidden enchantments and active enchantment levels (filters out dummy level 0 / glint lore tags).
  - Hidden attribute modifiers with calculated percentages (`+X%` / `-X%`).
  - Slot requirement indicator: `(Slot: all / mainhand / offhand / head / chest / legs / feet)`.
  - Unbreakable tags and raw NBT compound structures.

### 3. Custom GUI Slot Auto-Clicker (Spam Slot)
- **Control Panel**: Stacks cleanly on the right side of custom container GUIs (chests, custom menus).
- **Resource Pack Compatible**: Built using standard Minecraft `GuiButton` textures (`widgets.png`), fully supporting custom GUI resource packs.
- **Controls**:
  - `Spam Slot: ON / OFF`: Toggle automated clicking.
  - `Mode: <Drop | Drop All | Left | Right | Shift Left | Shift Right>`:
    - **Drop**: Drops single item from slot (vanilla drop key behavior).
    - **Drop All**: Drops entire stack from slot (Ctrl+Drop behavior).
    - **Left**: Normal left click.
    - **Right**: Right click (split / single item placement).
    - **Shift Left**: Shift + Left click (quick transfer).
    - **Shift Right**: Shift + Right click.
  - `Slot ID: ON / OFF`: Visual overlay displaying slot index numbers directly on the top-left of each slot square in yellow.
  - **Slot Input Box**: Type the target slot number directly (or click any slot when Slot ID is enabled to auto-fill) for hands-free background clicking without hovering the mouse cursor.

### 4. Configuration & In-Game GUI
- Unified configuration directory: `.minecraft/config/khyxultilities/`
  - `fasttrading.json`: Fast trading settings and trade locks.
  - `cooldown.json`: Click cooldowns (`fastTradeCooldown: 50ms`, `guiClickCooldown: 100ms`).
- **In-Game Mod Options**: Fully configurable in-game via `Mod Options -> KhyxUltilities -> Config` with live saving without restarting Minecraft.

---

## Building from Source

### Requirements
- JDK 8 (Java 8)

### Build Command
```bash
./gradlew build
```

The compiled mod JAR will be located at:
```
build/libs/KhyxUltilities-1.12.2-2.0.jar
```

