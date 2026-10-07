package com.vgkhiem.khyxultilities.eventhandler;

import com.vgkhiem.khyxultilities.FastTrading;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ScoreboardHandler {
   private final Minecraft mc = Minecraft.getMinecraft();

   @SubscribeEvent
   public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
      if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
         boolean show = FastTrading.cooldownConfig == null || FastTrading.cooldownConfig.showRedNumbers;
         GuiIngameForge.renderObjective = show;
      }
   }

   @SubscribeEvent
   public void onRenderOverlayPost(RenderGameOverlayEvent.Post event) {
      if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
         return;
      }
      if (FastTrading.cooldownConfig != null && FastTrading.cooldownConfig.showRedNumbers) {
         return;
      }
      if (this.mc.world == null || this.mc.player == null) {
         return;
      }

      Scoreboard scoreboard = this.mc.world.getScoreboard();
      if (scoreboard == null) {
         return;
      }

      ScoreObjective objective = null;
      ScorePlayerTeam playerTeam = scoreboard.getPlayersTeam(this.mc.player.getName());
      if (playerTeam != null && playerTeam.getColor() != null) {
         int slot = playerTeam.getColor().getColorIndex();
         if (slot >= 0) {
            objective = scoreboard.getObjectiveInDisplaySlot(3 + slot);
         }
      }

      ScoreObjective sidebar = objective != null ? objective : scoreboard.getObjectiveInDisplaySlot(1);
      if (sidebar != null) {
         renderScoreboardWithoutScores(sidebar, event.getResolution());
      }
   }

   private void renderScoreboardWithoutScores(ScoreObjective objective, ScaledResolution resolution) {
      Scoreboard scoreboard = objective.getScoreboard();
      if (scoreboard == null) {
         return;
      }

      Collection<Score> collection = scoreboard.getSortedScores(objective);
      List<Score> list = new ArrayList<>();

      for (Score score : collection) {
         if (score != null && score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
            list.add(score);
         }
      }

      if (list.size() > 15) {
         collection = list.subList(list.size() - 15, list.size());
      } else {
         collection = list;
      }

      FontRenderer fontRenderer = this.mc.fontRenderer;
      int maxWidth = fontRenderer.getStringWidth(objective.getDisplayName());

      for (Score score : collection) {
         ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
         String line = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
         maxWidth = Math.max(maxWidth, fontRenderer.getStringWidth(line));
      }

      int totalHeight = collection.size() * fontRenderer.FONT_HEIGHT;
      int startY = resolution.getScaledHeight() / 2 + totalHeight / 3;
      int right = resolution.getScaledWidth() - 3;
      int left = right - maxWidth;
      int index = 0;

      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

      for (Score score : collection) {
         index++;
         ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
         String line = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
         int y = startY - index * fontRenderer.FONT_HEIGHT;

         // Background
         Gui.drawRect(left - 2, y, right + 2, y + fontRenderer.FONT_HEIGHT, 1342177280);
         // Text
         fontRenderer.drawString(line, left, y, 553648127);

         if (index == collection.size()) {
            String title = objective.getDisplayName();
            Gui.drawRect(left - 2, y - fontRenderer.FONT_HEIGHT - 1, right + 2, y - 1, 1610612736);
            Gui.drawRect(left - 2, y - 1, right + 2, y, 1342177280);
            fontRenderer.drawString(title, left + maxWidth / 2 - fontRenderer.getStringWidth(title) / 2, y - fontRenderer.FONT_HEIGHT, 553648127);
         }
      }

      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }
}
