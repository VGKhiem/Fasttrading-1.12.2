package com.dazo66.fasttrading.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class FakeSubtitleSound extends PositionedSound {
   private String subtitle;

   protected FakeSubtitleSound(SoundEvent soundIn, SoundCategory categoryIn, String subtitle) {
      super(soundIn, categoryIn);
      this.subtitle = subtitle;
   }

   protected FakeSubtitleSound(ResourceLocation soundId, SoundCategory categoryIn, String subtitle) {
      super(soundId, categoryIn);
      this.subtitle = subtitle;
   }

   public FakeSubtitleSound(ResourceLocation soundId, SoundCategory categoryIn, float volumeIn, float pitchIn, boolean repeatIn, int repeatDelayIn, ISound.AttenuationType attenuationTypeIn, float xIn, float yIn, float zIn, String subtitle) {
      this(soundId, categoryIn, subtitle);
      this.volume = volumeIn;
      this.pitch = pitchIn;
      this.xPosF = xIn;
      this.yPosF = yIn;
      this.zPosF = zIn;
      this.repeat = repeatIn;
      this.repeatDelay = repeatDelayIn;
      this.attenuationType = attenuationTypeIn;
   }

   public static FakeSubtitleSound getRecord(SoundEvent soundIn, float pitchIn, float volumeIn, String subtitle) {
      return new FakeSubtitleSound(soundIn.getSoundName(), SoundCategory.MASTER, volumeIn, pitchIn, false, 0, AttenuationType.NONE, 0.0F, 0.0F, 0.0F, subtitle);
   }

   public SoundEventAccessor createAccessor(SoundHandler handler) {
      return new SoundEventAccessor(super.createAccessor(handler).getLocation(), this.subtitle);
   }
}
