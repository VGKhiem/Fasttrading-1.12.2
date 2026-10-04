package com.vgkhiem.khyxultilities.asm;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MainTransformer implements IClassTransformer {
   private HashMap<String, IRegisterTransformer> map = new HashMap();
   private String MCVERSION = this.getMCVERSION();

   public MainTransformer() {
      (new RegisterTransformer(this)).register();
   }

   public static byte[] clearMethod(String name, String transformedName, byte[] basicClass, List<String> methodInfo) {
      ClassReader classReader = new ClassReader(basicClass);
      ClassNode classNode = new ClassNode();
      classReader.accept(classNode, 0);

      for(MethodNode method : classNode.methods) {
         if (methodInfo.contains(method.name) && methodInfo.contains(method.desc)) {
            method.instructions.clear();
            method.instructions.add(new InsnNode(177));
         }
      }

      ClassWriter classWriter = new ClassWriter(2);
      classNode.accept(classWriter);
      return classWriter.toByteArray();
   }

   public void register(IRegisterTransformer iRegisterTransformer) {
      List<String> name = iRegisterTransformer.getClassName();
      if (this.isVersionAllow(iRegisterTransformer.getMcVersion())) {
         for(String s : name) {
            this.map.put(s, iRegisterTransformer);
         }

         FMLLog.log.info("{} Register SUCCESS", iRegisterTransformer.getClass().getSimpleName());
      } else {
         FMLLog.log.warn("This MCVersion is {} but Transformer {} accept MCVersion is {} that ignore this Transformer", this.MCVERSION, iRegisterTransformer.getClass().getSimpleName(), iRegisterTransformer.getMcVersion());
      }

   }

   public byte[] transform(String name, String transformedName, byte[] basicClass) {
      IRegisterTransformer irtf = null;
      if (this.map.containsKey(transformedName)) {
         irtf = (IRegisterTransformer)this.map.get(transformedName);
         FMLLog.log.info("CLASS: " + irtf.getClass().getSimpleName() + " Transformer SUCCESS");
         return irtf.transform(name, transformedName, basicClass);
      } else if (this.map.containsKey(name)) {
         irtf = (IRegisterTransformer)this.map.get(name);
         FMLLog.log.info("CLASS: " + irtf.getClass().getSimpleName() + " Transformer SUCCESS");
         return irtf.transform(name, transformedName, basicClass);
      } else {
         return basicClass;
      }
   }

   private boolean isVersionAllow(String transMcVersion) {
      String mcVersionString = transMcVersion;
      if ("[1.12]".equals(transMcVersion)) {
         mcVersionString = "[1.12,1.12.2]";
      }

      if ("[1.12.1]".equals(mcVersionString) || "[1.12,1.12.1]".equals(mcVersionString)) {
         mcVersionString = "[1.12,1.12.2]";
      }

      VersionRange range;
      if (!Strings.isNullOrEmpty(mcVersionString)) {
         range = VersionParser.parseRange(mcVersionString);
      } else {
         range = Loader.instance().getMinecraftModContainer().getStaticVersionRange();
      }

      return range.containsVersion(new DefaultArtifactVersion(this.MCVERSION));
   }

   private String getMCVERSION() {
      try {
         return (String)ForgeVersion.class.getField("mcVersion").get("");
      } catch (Exception var2) {
         return "";
      }
   }
}
