package com.vgkhiem.khyxultilities.event;

import javax.annotation.Nullable;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class SetMerchantListEvent extends Event {
   public MerchantRecipeList list;

   public SetMerchantListEvent(@Nullable MerchantRecipeList list) {
      this.list = list;
   }

   public static void post(@Nullable MerchantRecipeList list) {
      MinecraftForge.EVENT_BUS.post(new SetMerchantListEvent(list));
   }
}
