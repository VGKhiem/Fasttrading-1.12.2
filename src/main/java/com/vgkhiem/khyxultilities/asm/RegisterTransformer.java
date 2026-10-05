package com.vgkhiem.khyxultilities.asm;

import com.vgkhiem.khyxultilities.event.SetMerchantListEvent;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class RegisterTransformer {
   private MainTransformer mainT;

   public RegisterTransformer(MainTransformer mainTIn) {
      this.mainT = mainTIn;
   }

   public void register() {
      this.mainT.register(new HookSetRecipeListEvent());
      this.mainT.register(new HookNetworkManagerChannel());
   }

   private class HookSetRecipeListEvent implements IRegisterTransformer {
      private HookSetRecipeListEvent() {
      }

      public String getMcVersion() {
         return "[1.8,1.12.2]";
      }

      public List<String> getClassName() {
         return Arrays.asList("net.minecraft.entity.NpcMerchant");
      }

      public byte[] transform(String name, String transformedName, byte[] basicClass) {
         ClassReader classReader = new ClassReader(basicClass);
         ClassNode classNode = new ClassNode();
         classReader.accept(classNode, 0);
         MethodNode method = (MethodNode)classNode.methods.get(4);
         InsnList insnList = method.instructions;
         insnList.remove(insnList.getLast());
         insnList.remove(insnList.getLast());
         insnList.add(new VarInsnNode(25, 1));
         MethodInsnNode m = new MethodInsnNode(184, SetMerchantListEvent.class.getName().replace(".", "/"), "post", "(Lnet/minecraft/village/MerchantRecipeList;)V", false);
         insnList.add(m);
         insnList.add(new InsnNode(177));
         insnList.add(new LabelNode());
         ClassWriter classWriter = new ClassWriter(2);
         classNode.accept(classWriter);
         return classWriter.toByteArray();
      }
   }

   private class HookNetworkManagerChannel implements IRegisterTransformer {
      private HookNetworkManagerChannel() {
      }

      public String getMcVersion() {
         return "[1.8,1.12.2]";
      }

      public List<String> getClassName() {
         return Arrays.asList("net.minecraft.network.NetworkManager$5", "gw$5");
      }

      public byte[] transform(String name, String transformedName, byte[] basicClass) {
         ClassReader classReader = new ClassReader(basicClass);
         ClassNode classNode = new ClassNode();
         classReader.accept(classNode, 0);

         for (MethodNode method : classNode.methods) {
            if ("initChannel".equals(method.name) && "(Lio/netty/channel/Channel;)V".equals(method.desc)) {
               InsnList toInject = new InsnList();
               toInject.add(new VarInsnNode(25, 1));
               toInject.add(new MethodInsnNode(184, "com/vgkhiem/khyxultilities/network/ProxyHook", "injectProxy", "(Lio/netty/channel/Channel;)V", false));
               method.instructions.insert(toInject);
               break;
            }
         }

         ClassWriter classWriter = new ClassWriter(1);
         classNode.accept(classWriter);
         return classWriter.toByteArray();
      }
   }
}
