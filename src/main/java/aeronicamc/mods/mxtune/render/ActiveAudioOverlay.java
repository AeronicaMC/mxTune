package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import static aeronicamc.mods.mxtune.render.RenderHelper.getPlayer;
import static aeronicamc.mods.mxtune.render.RenderHelper.mc;

public class ActiveAudioOverlay {
    private static final ItemStack PLACARD_ITEM = new ItemStack(ModItems.PLACARD_ITEM.get());

    @SuppressWarnings("deprecation")
    public static void render(RenderGameOverlayEvent.Post event) {
        // Display SoundManager and ClientAudio debug info when the mxtune:placard_item is on the hot-bar.
        if (isPlacardInHotBar()) {
            final MatrixStack pPoseStack = event.getMatrixStack();
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);

            final ITextComponent infoText = new StringTextComponent("")
                    .append(String.format("%s, %s", mc.getSoundManager().getDebugString(), ClientAudio.getDebugString())).withStyle(TextFormatting.WHITE);
            final int[] posY = { 0 };
            posY[0] = (25);
            mc.font.drawShadow(pPoseStack, infoText, 5.0F, posY[0] += 10, -11534256);
            ClientAudio.getAudioData().forEach(audioData -> mc.font.drawShadow(pPoseStack, audioData.getInfo(), 5, posY[0] += 10, -11534256));
        }
    }

    /**
     * @return true if mxtune:placard_item is on the hot-bar, and the player is not in a portal.
     */
    private static boolean isPlacardInHotBar() {
        int slot = getPlayer().inventory.findSlotMatchingItem(PLACARD_ITEM);
        return slot >= 0 && slot < 9 && !(getPlayer().portalTime > 0);
    }
}
