package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.MusicType;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

import static aeronicamc.mods.mxtune.render.RenderHelper.*;

public class InstrumentOverlay implements IOverlayItem {
    private static final String NAME = new TranslationTextComponent("gui.mxtune.overlay.instrument.name").getString();
    private final ItemStack itemStack;
    private long lastChanged;
    private boolean changed;
    private final ITextComponent titleText;
    private final ITextComponent extraText;
    private final int totalWidth;
    private final int lastSlot;
    private final boolean managePosition;

    public InstrumentOverlay(ItemStack itemStack) {
        this.lastSlot = RenderHelper.getSelectedSlot();
        this.itemStack = itemStack;
        ItemStack sheetMusic = SheetMusicHelper.getIMusicFromIInstrument(itemStack);
        this.titleText = SheetMusicHelper.getFormattedMusicTitle(sheetMusic);
        this.extraText = SheetMusicHelper.getFormattedExtraText(sheetMusic);
        this.totalWidth = Math.max(Math.max(mc.font.width(titleText), mc.font.width(extraText)) + 40, this.baseWidth());
        this.managePosition = false;
    }

    public InstrumentOverlay() {
        this.lastSlot = -1;
        this.itemStack = new ItemStack(ModItems.MULTI_INST.get(), 1);
        ((IInstrument) this.itemStack.getItem()).setPatch(itemStack, SoundFontProxyManager.getProxy("flute_pan").index);
        ItemStack sheetMusic = new ItemStack(ModItems.SHEET_MUSIC.get(), 1);
        SheetMusicHelper.writeIMusic(sheetMusic,NAME, new byte[]{1,1}, "MML@rrrrrrrr;", new String[]{"flute_pan"}, MusicType.PART, UUID.randomUUID(), "FakePlayer");
        this.titleText = SheetMusicHelper.getFormattedMusicTitle(sheetMusic);
        this.extraText = SheetMusicHelper.getFormattedExtraText(sheetMusic);
        this.totalWidth = Math.max(Math.max(mc.font.width(titleText), mc.font.width(extraText)) + 40, this.baseWidth());
        this.managePosition = true;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean isManagedPosition() {
        return this.managePosition;
    }

    @Override
    public int totalHeight() {
        return this.baseHeight();
    }

    @Override
    public int totalWidth() {
        return this.totalWidth;
    }

    private boolean isNotInstrumentItem() {
        return !(getPlayer().inventory.getSelected().getItem() instanceof IInstrument);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Visibility render(MatrixStack pPoseStack, long delta) {
        if (this.changed) {
            this.lastChanged = delta;
            this.changed = false;
        }
        mc.getTextureManager().bind(IOverlayItem.TEXTURE);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        RenderHelper.blit(pPoseStack, 0, 0, 0, 0, this.baseWidth(), this.baseHeight());
        RenderHelper.blit(pPoseStack, ((totalWidth - this.baseWidth())/2) + 5, 0, 10, 0, this.baseWidth() -10, this.baseHeight());
        RenderHelper.blit(pPoseStack, totalWidth - this.baseWidth() + 10, 0, 10, 0, this.baseWidth(), this.baseHeight());
        mc.getItemRenderer().renderAndDecorateItem(itemStack, 8, 8);

        mc.font.draw(pPoseStack, titleText, 30.0F, 7.0F, -11534256);
        mc.font.draw(pPoseStack, extraText, 30.0F, 17.0F, -11534256);

        if (managePosition)
            return delta - this.lastChanged >= 1100L ? Visibility.HIDE : Visibility.SHOW;
        else
            return delta - this.lastChanged >= 5000L || lastSlot != getSelectedSlot() || isNotInstrumentItem() ? Visibility.HIDE : Visibility.SHOW;
    }
}
