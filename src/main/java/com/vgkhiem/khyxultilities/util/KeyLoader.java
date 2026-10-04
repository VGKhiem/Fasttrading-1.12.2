package com.vgkhiem.khyxultilities.util;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class KeyLoader {
   public static KeyBinding key_F4;

   public KeyLoader() {
      key_F4 = new KeyBinding("FastTrading ON-OFF", 62, "FastTrading");
      ClientRegistry.registerKeyBinding(key_F4);
   }
}
