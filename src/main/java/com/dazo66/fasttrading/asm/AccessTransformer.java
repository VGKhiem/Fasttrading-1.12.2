package com.dazo66.fasttrading.asm;

import java.io.IOException;

public class AccessTransformer extends net.minecraftforge.fml.common.asm.transformers.AccessTransformer {
   public AccessTransformer() throws IOException {
      super("fasttrading_at.cfg");
   }
}
