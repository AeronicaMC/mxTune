package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.gui.widget.list.SoundFontList;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.ChooseInstrumentMessage;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;
import java.util.Optional;

import static aeronicamc.mods.mxtune.init.ModItems.INSTRUMENT_ITEMS;

public class GuiMultiInstChooser extends Screen
{
    private final static ResourceLocation GUI_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/multi_inst_chooser.png");
    private final static int imageWidth = 256;
    private final static int imageHeight = 165;
    private int guiLeft;
    private int guiTop;
    private final Screen parent;
    private SoundFontList widget;

    protected GuiMultiInstChooser(Screen parent)
    {
        super(new TranslationTextComponent("gui.mxtune.label.instruments"));
        this.parent = parent;
        widget = new SoundFontList().init();
    }

    @Override
    public void init(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        super.init(pMinecraft, pWidth, pHeight);
        assert minecraft != null;
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
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
            selectCallback(Objects.requireNonNull(widget.getSelected()));
            this.minecraft.setScreen(parent);
        }));
    }

    private void selectCallback(SoundFontList.Entry selected)
    {
        getPlayer(Objects.requireNonNull(minecraft)).ifPresent(player->{
            player.inventory.getSelected().setDamageValue(selected.getIndex());
            ((InstrumentScreen)parent).updateButton(selected.getIndex());
            PacketDispatcher.sendToServer(new ChooseInstrumentMessage(selected.getIndex()));
        });

    }

    Optional<PlayerEntity> getPlayer(Minecraft minecraft)
    {
        return Optional.ofNullable(minecraft.player);
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
        font.draw(pMatrixStack, this.title, (guiLeft+imageWidth) - titleWidth - 10, guiTop + 4, TextColorFg.DARK_GRAY);

        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

        // Render the Instrument GUI image
        int relX = (widget.getRight() + (guiLeft + imageWidth - widget.getRight())/2);
        int relY = guiTop + (imageHeight)/3;
        ModGuiHelper.RenderGuiItemScaled(Objects.requireNonNull(minecraft).getItemRenderer(),
                                         INSTRUMENT_ITEMS.get(Objects.requireNonNull(widget.getSelected()).getIndex()).get().getDefaultInstance(), relX, relY, 3, true);

    }
}
