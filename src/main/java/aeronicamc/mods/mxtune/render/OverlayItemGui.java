package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.items.MusicVenueToolItem;
import aeronicamc.mods.mxtune.util.IInstrument;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;


public class OverlayItemGui extends AbstractGui implements IItemOverlayPosition {
   private final Minecraft minecraft;
   private final OverlayInstance<?>[] visible = { null, null };
   private int lastSlot = -1;

   public OverlayItemGui(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void render(MatrixStack pPoseStack) {
      if (!this.minecraft.options.hideGui) {
         OverlayInstance<?> overlayInstance;
         for(int instIndex = 0; instIndex < this.visible.length; ++instIndex) {
            overlayInstance = null;
            if (this.visible[instIndex] == null && getMinecraft().player != null && lastSlot != RenderHelper.getSelectedSlot()) {
               lastSlot = RenderHelper.getSelectedSlot();
               this.visible[instIndex] = getOverLayInstance();
            }

            if (this.visible[instIndex] != null)
               overlayInstance = this.visible[instIndex];
            if (overlayInstance != null && overlayInstance.render(this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight(), instIndex, pPoseStack)) {
               this.visible[instIndex] = null;
            }
         }
      }
   }

   @Nullable
   public OverlayInstance<?> getOverLayInstance() {
      ItemStack stack = getPlayer().inventory.getSelected();
      if (stack.getItem() instanceof IInstrument)
         return new OverlayInstance<>(this, new InstrumentOverlay(stack));
      else if (stack.getItem() instanceof MusicVenueToolItem)
         return new OverlayInstance<>(this, new VenueToolOverlay(stack));
      return null;
   }

   @Override
   public IOverlayItem.Position getPosition(IOverlayItem overlayItem) {
      synchronized (MXTuneConfig.SYNC) {
         if (overlayItem.getItemStack().getItem() instanceof IInstrument)
            return MXTuneConfig.getInstrumentOverlayPosition();
         else if (overlayItem.getItemStack().getItem() instanceof MusicVenueToolItem)
            return MXTuneConfig.getVenueToolOverlayPosition();
         else
            return IOverlayItem.Position.LEFT;
      }
   }

   @Override
   public float getPercent(IOverlayItem overlayItem) {
      synchronized (MXTuneConfig.SYNC) {
         if (overlayItem.getItemStack().getItem() instanceof IInstrument)
            return MXTuneConfig.getInstrumentOverlayPercent();
         else if (overlayItem.getItemStack().getItem() instanceof MusicVenueToolItem)
            return MXTuneConfig.getVenueToolOverlayPercent();
         else
            return 0F;
      }
   }

   @SuppressWarnings("unchecked")
   @Nullable
   public <T extends IOverlayItem> T getOverlay(Class<? extends T> pIOverlayItem, Object pToken) {
      for(OverlayInstance<?> overlayInstance : this.visible) {
         if (overlayInstance != null && pIOverlayItem.isAssignableFrom(overlayInstance.getOverlayItem().getClass()) && overlayInstance.getOverlayItem().getToken().equals(pToken)) {
            return (T)overlayInstance.getOverlayItem();
         }
      }
      return null;
   }

   public void clear() {
      Arrays.fill(this.visible, null);
   }

   private Minecraft getMinecraft() {
      return this.minecraft;
   }

   private ClientPlayerEntity getPlayer() {
      return Objects.requireNonNull(this.minecraft.player);
   }
}