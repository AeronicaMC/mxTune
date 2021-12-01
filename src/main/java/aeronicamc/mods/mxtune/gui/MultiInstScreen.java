package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.inventory.InstrumentContainer;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.Objects;

@Deprecated
public class MultiInstScreen extends ContainerScreen<InstrumentContainer>
{
    private final ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/multi_inst_inventory.png");
    Button buttonChangeInstrument;
    int instIndex;
    String musicKey = "";

    public MultiInstScreen(InstrumentContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        minecraft = Minecraft.getInstance();
        assert minecraft.player != null;
        this.imageWidth = 166;
        this.imageWidth = 184;
    }

    @Override
    protected void init()
    {
        super.init();

        int yPos = topPos + 6;
        int xPos = leftPos + 12;
        assert minecraft != null;
        assert minecraft.player != null;

        buttonChangeInstrument = new Button(xPos, yPos, imageWidth - 24, 20, inventory.getSelected().getHoverName(), (open) ->
                minecraft.setScreen(new GuiMultiInstChooser(this)));
        this.addButton(buttonChangeInstrument);
        buttonChangeInstrument.setMessage(new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(inventory.getSelected().getMaxDamage())));
    }

    @Override
    public void tick()
    {
        int newIndex;
        String newMusicKey;
        super.tick();
        if (instIndex != (newIndex = inventory.getSelected().getMaxDamage()))
        {
            instIndex = newIndex;
            updateInstrumentName();
        }
        ItemStack sheetMusic = SheetMusicHelper.getIMusicFromIInstrument(inventory.getSelected());
        if (SheetMusicHelper.hasMusicText(sheetMusic) && !(newMusicKey = SheetMusicHelper.getMusicTextKey(sheetMusic)).equals(musicKey))
        {
            musicKey = newMusicKey;
            updateSheetMusic();
        }
    }

    private boolean mcPlayerOk(@Nullable Minecraft minecraft) { return minecraft != null && minecraft.player != null; }

    private void updateInstrumentName()
    {
        if (mcPlayerOk(minecraft))
            buttonChangeInstrument.setMessage(new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(inventory.getSelected().getMaxDamage())));
    }

    private void updateSheetMusic()
    {

    }


    @Override
    public void removed()
    {
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(false);
        super.removed();
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y)
    {
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bind(GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(MatrixStack matrixStack , int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        ModGuiHelper.RenderGuiItemScaled(this.itemRenderer,
            Objects.requireNonNull(Objects.requireNonNull(minecraft).player).getMainHandItem().getStack(),
                getGuiLeft() + 51, getGuiTop() + 50, 2, true);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack , int mouseX, int mouseY) {
        this.font.draw(matrixStack, this.title, 10, 72, TextColorFg.DARK_GRAY);
    }
}