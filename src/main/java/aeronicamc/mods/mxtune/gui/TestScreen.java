package aeronicamc.mods.mxtune.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static aeronicamc.mods.mxtune.init.ModItems.INSTRUMENT_ITEMS;

public class TestScreen extends Screen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private SoundFontProxyWidget sfpWidget;

    public TestScreen()
    {
        super(new TranslationTextComponent("screen.mxtune.test.title"));
    }

    @Override
    public void init()
    {
        super.init();
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(new Button(this.width - 65, (this.height / 6 + 168) - 20, 50, 20, new TranslationTextComponent("gui.mxtune.open"), (open) -> {
            minecraft.setScreen(new GuiMultiInstChooser(this));
        }));
        this.addButton(new Button(this.width - 65, this.height / 6 + 168, 50, 20, new TranslationTextComponent("gui.done"), (done) -> {
            assert minecraft != null;
            minecraft.popGuiLayer();
        }));

        sfpWidget = new SoundFontProxyWidget(minecraft, 128, height - 30 , 15, height - 15, font.lineHeight + 4, 15, (entry)-> {
            LOGGER.info("Selected {}", entry.soundFontProxy.id);
        }).init();
        sfpWidget.changeFocus(true);
        children.add(sfpWidget);
        children.add(sfpWidget.getSelected());
    }

    @Override
    public void render(MatrixStack matrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(matrixStack);
        this.sfpWidget.render(matrixStack, pMouseX, pMouseY, pPartialTicks);
        drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, 15, TextColorFg.WHITE);
        super.render(matrixStack, pMouseX, pMouseY, pPartialTicks);

        // Render the Instrument GUI image
        int relX = ((width - sfpWidget.getRight()) / 2) + sfpWidget.getRight();
        int relY = height/2;
        ModGuiHelper.RenderGuiItemScaled(Objects.requireNonNull(minecraft).getItemRenderer(),
                                         INSTRUMENT_ITEMS.get(Objects.requireNonNull(sfpWidget.getSelected()).getIndex()).get().getDefaultInstance(), relX, relY, 8, true);

    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    // Call on ESC key force close! Ignores chained GUI's?
    // Override "shouldCloseOnEsc and return false" to prevent closing on ESC.
    @Override
    public void removed()
    {
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(false);
        super.removed();
    }

    // Called on ESC key and minecraft.displayGuiScreen(this.lastScreen);
    @Override
    public void onClose()
    {
        super.onClose();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
