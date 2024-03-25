package aeronicamc.mods.mxtune.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public interface IOverlayItem {
   ResourceLocation TEXTURE = new ResourceLocation("textures/gui/toasts.png");
   Object NO_TOKEN = new Object();

   IOverlayItem.Visibility render(MatrixStack pPoseStack, long delta);

   default Object getToken() {
      return NO_TOKEN;
   }

   ItemStack getItemStack();

   boolean isManagedPosition();

   default int baseWidth() {
      return 160;
   }

   default int baseHeight() {
      return 32;
   }

   int totalHeight();

   int totalWidth();

   enum Visibility {
      SHOW(SoundEvents.UI_TOAST_IN),
      HIDE(SoundEvents.UI_TOAST_OUT);

      private final SoundEvent soundEvent;

      Visibility(SoundEvent pSoundEvent) {
         this.soundEvent = pSoundEvent;
      }

      public void playSound(SoundHandler pHandler) {
         pHandler.play(SimpleSound.forUI(this.soundEvent, 1.2F, 0.80F));
      }
   }

   enum Position {
      LEFT("enum.mxtune.item.overlay.position.left"), CENTER("enum.mxtune.item.overlay.position.center"), RIGHT("enum.mxtune.item.overlay.position.right");

      private final String positionKey;

      Position(String positionKey) {
         this.positionKey = positionKey;
      }

      public String getPositionKey() {
         return positionKey;
      }

      private static final Position[] values = values();
      public static Position getPosition(int ordinal) {
         return ordinal >= 0 && ordinal < values.length ? values[ordinal] : LEFT;
      }
      public static Position nextPosition(Position pos) {
         int cycle = (pos.ordinal() + 1) % values.length;
         return getPosition(cycle);
      }
   }
}