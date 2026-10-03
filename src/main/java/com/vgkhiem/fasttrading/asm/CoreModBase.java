package com.vgkhiem.fasttrading.asm;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion("")
public class CoreModBase implements IFMLLoadingPlugin {
   public String[] getASMTransformerClass() {
      return new String[]{MainTransformer.class.getName()};
   }

   public String getModContainerClass() {
      return null;
   }

   @Nullable
   public String getSetupClass() {
      return null;
   }

   public void injectData(Map<String, Object> data) {
   }

   public String getAccessTransformerClass() {
      return AccessTransformer.class.getName();
   }
}
