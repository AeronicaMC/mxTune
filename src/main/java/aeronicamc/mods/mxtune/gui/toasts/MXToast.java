package aeronicamc.mods.mxtune.gui.toasts;

import aeronicamc.mods.mxtune.init.ModItems;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class MXToast implements IToast
{
    private static final ITextComponent TITLE_TEXT = new TranslationTextComponent("itemGroup.mxtune");
    private ItemStack itemStack = new ItemStack(ModItems.INSTRUMENT_ITEMS.get(27).get());
    private long lastChanged;
    private boolean changed;

    public MXToast() { /* NOP */ }

    @Override
    public Visibility render(MatrixStack pPoseStack, ToastGui pToastComponent, long delta)
    {
        if (this.changed) {
            this.lastChanged = delta;
            this.changed = false;
        }
        pToastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        pToastComponent.blit(pPoseStack, 0, 0, 0, 32, this.width(), this.height());
        pToastComponent.getMinecraft().font.draw(pPoseStack, TITLE_TEXT, 30.0F, 7.0F, -11534256);

        pToastComponent.getMinecraft().getItemRenderer().renderAndDecorateItem(itemStack, 8, 8);
        return delta - this.lastChanged >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }
}
