package aeronicamc.mods.mxtune.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestScreen extends Screen
{
    private static int depth;
    private static final Logger LOGGER = LogManager.getLogger();
    private final Screen lastScreen;

    public TestScreen(Screen lastScreen)
    {
        super(new TranslationTextComponent("screen.mxtune.test.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    public void init()
    {
        super.init();
        this.addButton(new Button(this.width / 2 - 100, (this.height / 6 + 168) - 20, 200, 20, new TranslationTextComponent("gui.mxtune.open"), (done) -> {
            this.minecraft.setScreen(new TestScreen(this));
            ++depth;
        }));
        this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, new TranslationTextComponent("gui.done"), (done) -> {
            this.minecraft.setScreen(this.lastScreen);
            if (depth >= 1)
                depth--;
        }));

    }

    @Override
    public void render(MatrixStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_)
    {
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, 15, 16777215);
        drawCenteredString(matrixStack, this.font, String.format("Depth %d", depth + 1), this.width / 2, 25, 16777215);
        super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
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
        depth = 0;
        LOGGER.debug("TestScreen onClose");
        super.removed();
    }

    // Called on ESC key and minecraft.displayGuiScreen(this.lastScreen);
    @Override
    public void onClose()
    {
        LOGGER.debug("TestScreen removed {}", depth);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
