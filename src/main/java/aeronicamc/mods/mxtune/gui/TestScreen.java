package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.label.MXLabel;
import aeronicamc.mods.mxtune.gui.widget.list.SoundFontList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static aeronicamc.mods.mxtune.init.ModItems.INSTRUMENT_ITEMS;

public class TestScreen extends Screen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final SoundFontList sfpWidget = new SoundFontList();
    private TextFieldWidget titleWidget;
    private TextFieldWidget musicTextWidget;
    private MXLabel labelTitle;
    private boolean initialized;
    private final MXButton buttonOpen = new MXButton((open) -> onButtonOpen());

    public TestScreen()
    {
        super(new TranslationTextComponent("screen.mxtune.test.title"));
    }

    @Override
    public void init()
    {
        super.init();
        labelTitle = new MXLabel(font, width / 2 , 5, font.width(title.getString()) + 4, font.lineHeight + 4, title, TextColorFg.WHITE);
        labelTitle.setBackground(false);
        labelTitle.setBorder(1);
        labelTitle.setCentered(true);
        labelTitle.setUlColor(TextColorBg.WHITE);
        labelTitle.setBrColor(TextColorBg.DARK_GRAY);
        labelTitle.setBackColor(TextColorBg.BLUE);
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);
        buttonOpen.setLayout(this.width - 65, (this.height / 6 + 168) - 20, 50, 20);
        buttonOpen.setMessage(new TranslationTextComponent("gui.mxtune.open"));
        addButton(buttonOpen);

        this.addButton(new MXButton(buttonOpen.getLeft(), buttonOpen.getBottom(), 50, 20, new TranslationTextComponent("gui.done"), (done) -> {
            minecraft.popGuiLayer();
        }));

        sfpWidget.setLayout(128, height - 30 , 15, height - 15, 15);
        sfpWidget.setCallBack((entry)-> {
            LOGGER.info("Selected {}", entry.getId());
        });

        if (!initialized)
        {
            sfpWidget.init();
            initialized = true;
        }

        titleWidget = new TextFieldWidget(font, sfpWidget.getRight() + 10, height / 2, (width - sfpWidget.getRight()) - 20, font.lineHeight + 4, new StringTextComponent("Title"));

        musicTextWidget = new TextFieldWidget(font, sfpWidget.getRight() + 10, (height / 2) + 20, (width - sfpWidget.getRight()) - 20, font.lineHeight + 4, new StringTextComponent("MML"));
        musicTextWidget.setMaxLength(10000);

        addWidget(sfpWidget);
        addWidget(titleWidget);
        addWidget(musicTextWidget);
    }

    public void onButtonOpen()
    {
        Objects.requireNonNull(minecraft).setScreen(new GuiMultiInstChooser(this));
    }


    @Override
    public void render(MatrixStack matrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(matrixStack);
        labelTitle.render(matrixStack, pMouseX, pMouseY, pPartialTicks);
        //drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, 15, TextColorFg.WHITE);
        this.sfpWidget.render(matrixStack, pMouseX, pMouseY, pPartialTicks);
        this.titleWidget.render(matrixStack, pMouseX, pMouseY, pPartialTicks);
        this.musicTextWidget.render(matrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(matrixStack, pMouseX, pMouseY, pPartialTicks);

        // Render the Instrument GUI image
        int relX = ((width - sfpWidget.getRight()) / 5) + sfpWidget.getRight();
        int relY = height/5;
        ItemStack itemStack = sfpWidget.getSelected() != null ? INSTRUMENT_ITEMS.get(sfpWidget.getSelected().getIndex()).get().getDefaultInstance() : Items.CREEPER_HEAD.getDefaultInstance();
        ModGuiHelper.RenderGuiItemScaled(Objects.requireNonNull(minecraft).getItemRenderer(), itemStack, relX, relY, 3, true);
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
