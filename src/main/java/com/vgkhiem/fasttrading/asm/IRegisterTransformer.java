package com.vgkhiem.fasttrading.asm;

import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;

public interface IRegisterTransformer extends IClassTransformer {
   String getMcVersion();

   List<String> getClassName();
}
