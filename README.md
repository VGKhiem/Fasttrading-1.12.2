# FastTrading 1.12.2

A Minecraft Forge 1.12.2 client-side mod that optimizes and accelerates villager trading transactions.

**Author**: VGKhiem

## Features
- **Fast Trading Interface**: Custom villager trading GUI sidebar for quick navigation and one-click/double-click trading.
- **Price Locking & Auto-Trading**: Lock desired trade prices and automate repeated trades.
- **Bug Fixes & Compatibility (Fork/Patch)**:
  - Fixed infinite loop client freeze/crash when a trade requires 2 items of the same type (both vanilla and custom NBT items).
  - Accurate NBT matching for custom items using `ItemStack.areItemStackTagsEqual`.
  - Fixed OptiFine Fast Render conflict causing invisible GUI (resolved OpenGL matrix stack underflow error 1284 and dirty GL states).
- **Advanced Inspection (Hold Alt)**:
  - Hold `Alt` while hovering over any item (in trading boxes, inventory, or any custom GUI) to inspect hidden enchantments, hidden attribute modifiers, unbreakable tags, and full detailed NBT structure.

## Building from Source

Requirements:
- JDK 8

Run the following command to build the mod JAR:
```bash
./gradlew build
```

The compiled mod will be generated in `build/libs/FastTrading-1.12.2-2.0.jar`.
