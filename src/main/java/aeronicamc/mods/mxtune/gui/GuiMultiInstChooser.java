package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.gui.widget.list.SoundFontList;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.inventory.MultiInstContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;
import java.util.Optional;

public class GuiMultiInstChooser extends Screen
{
    private final static ResourceLocation GUI_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/multi_inst_chooser.png");
    private final static int imageWidth = 256;
    private final static int imageHeight = 165;
    private int guiLeft;
    private int guiTop;
    private final Screen parent;
    private final SoundFontList widget = new SoundFontList().init();

    public GuiMultiInstChooser(Screen parent, MultiInstContainer menu)
    {
        super(new TranslationTextComponent("gui.mxtune.label.instruments"));
        this.parent = parent;
        setSelected(((MultiInstScreen)parent).getInstrument());
    }

    @Override
    public void init(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        super.init(pMinecraft, pWidth, pHeight);
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);
        this.width = pWidth;
        this.height = pHeight;
        this.guiLeft = (this.width - imageWidth) / 2;
        this.guiTop = (this.height - imageHeight) / 2;

        int instListWidth;
        instListWidth = Math.min(widget.getSuggestedWidth(), 200);
        widget.setLayout(guiLeft + 10, guiTop + 10, instListWidth + 1, imageHeight -20);
        widget.setCallBack(this::selectCallback);
        addWidget(widget);

        int widthButtons = 50;
        int posX = (widget.getRight() + (guiLeft + imageWidth - widget.getRight())/2) - widthButtons/2;
        int posY = guiTop + imageHeight - 20 - 15;

        this.addButton(new Button(posX, posY, widthButtons, 20, new TranslationTextComponent("gui.done"), (done) -> {
            selectCallback(Objects.requireNonNull(widget.getSelected()), false);
            onClose();
        }));
    }

    private void selectCallback(SoundFontList.Entry selected, Boolean doubleClicked)
    {
        getPlayer(Objects.requireNonNull(minecraft)).ifPresent(player->{
            ((MultiInstScreen)parent).updateButton(selected.getIndex());
        });
        if (doubleClicked)
            onClose();
    }

    Optional<PlayerEntity> getPlayer(Minecraft minecraft)
    {
        return Optional.ofNullable(minecraft.player);
    }

    private void setSelected(int index)
    {
        SoundFontList.Entry selected = widget.children().get(index);
        widget.setSelected(selected);
        widget.centerScrollOn(selected);
    }

    @Override
    public String getNarrationMessage() {
        return super.getNarrationMessage() + ". " + this.title.getString();
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return true;
    }

    @Override
    public void onClose()
    {
        removed();
        Objects.requireNonNull(this.minecraft).setScreen(null);
    }

    @Override
    public void removed()
    {
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(false);
        super.removed();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void renderBackground(MatrixStack pMatrixStack)
    {
        super.renderBackground(pMatrixStack);
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bind(GUI_TEXTURE);
        int relX = (this.width - imageWidth) / 2;
        int relY = (this.height - imageHeight) / 2;
        this.blit(pMatrixStack, relX, relY, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        // Render background
        this.renderBackground(pMatrixStack);

        // render the instrument chooser widget
        widget.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

        // Render labels
        int titleWidth = font.width(this.title);
        font.draw(pMatrixStack, this.title, (guiLeft+imageWidth) - titleWidth - 10F, guiTop + 4F, TextColorFg.DARK_GRAY);

        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

        // Render the Instrument GUI image
        int relX = (widget.getRight() + (guiLeft + imageWidth - widget.getRight())/2);
        int relY = guiTop + (imageHeight)/3;
        ItemStack itemStack = widget.getSelected() != null ? ModItems.getMultiInst(widget.getSelected().getIndex()) : Items.CREEPER_HEAD.getDefaultInstance();
        ModGuiHelper.RenderGuiItemScaled(Objects.requireNonNull(minecraft).getItemRenderer(), itemStack, relX, relY, 3, true);
    }
}
