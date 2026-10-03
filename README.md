# FastTrading 1.12.2

A Minecraft Forge 1.12.2 client-side mod that optimizes and accelerates villager trading transactions.

## Features
- **Fast Trading Interface**: Custom villager trading GUI sidebar for quick navigation and one-click/double-click trading.
- **Price Locking & Auto-Trading**: Lock desired trade prices and automate repeated trades.
- **Bug Fixes (Fork/Patch)**:
  - Fixed infinite loop client freeze/crash when a trade requires 2 items of the same type (both vanilla and custom NBT items).
  - Accurate NBT matching for custom items using `ItemStack.areItemStackTagsEqual`.

## Building from Source

Requirements:
- JDK 8

Run the following command to build the mod JAR:
```bash
./gradlew build
```

The compiled mod will be generated in `build/libs/FastTrading-1.12.2-2.0.jar`.
