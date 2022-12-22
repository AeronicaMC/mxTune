package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.gui.mml.GuiFileImporter;
import aeronicamc.mods.mxtune.gui.mml.GuiMXT;
import aeronicamc.mods.mxtune.gui.toasts.MXToast;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXLabel;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.gui.widget.list.SoundFontList;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.util.Misc;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class TestScreen extends Screen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final SoundFontList sfpWidget = new SoundFontList();
    private final MXTextFieldWidget titleTextWidget = new MXTextFieldWidget(Reference.MXT_SONG_TITLE_LENGTH);
    private final MXTextFieldWidget musicTextWidget = new MXTextFieldWidget(Reference.MAX_MML_PART_LENGTH);
    private MXLabel labelTitle;
    private boolean initialized;
    private final MXButton buttonOpen = new MXButton((open) -> onButtonOpen());
    private final MXButton buttonFile = new MXButton((file) -> onButtonFile());
    private final MXButton buttonGuiMXT = new MXButton((file) -> onGuiMXT());

    public TestScreen()
    {
        super(new TranslationTextComponent("gui.mxtune.gui_test.title"));
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

        buttonGuiMXT.setLayout(this.width - 105, (this.height / 6 + 168) - 60, 100, 20);
        buttonGuiMXT.setMessage(new TranslationTextComponent("gui.mxtune.gui_music_library.title"));
        addButton(buttonGuiMXT);
        buttonFile.setLayout(this.width - 105, (this.height / 6 + 168) - 40, 100, 20);
        buttonFile.setMessage(new TranslationTextComponent("gui.mxtune.gui_file_importer.title"));
        addButton(buttonFile);
        buttonOpen.setLayout(this.width - 55, (this.height / 6 + 168) - 20, 50, 20);
        buttonOpen.setMessage(new TranslationTextComponent("gui.mxtune.button.open"));
        addButton(buttonOpen);

        this.addButton(new MXButton(buttonOpen.getLeft(), buttonOpen.getBottom(), 50, 20, new TranslationTextComponent("gui.done"),
                                    (done) -> Objects.requireNonNull(minecraft).popGuiLayer()));

        //sfpWidget.setLayout(128, height - 30 , 15, height - 15, 15);
        sfpWidget.setLayout(5, 5, 128, height - 10);
        sfpWidget.setCallBack((entry, doubleClicked)-> {
            Misc.addToast(new MXToast());
            LOGGER.info("Selected {}", entry.getId());
        });

        if (!initialized)
        {
            sfpWidget.init();
            initialized = true;
        }

        titleTextWidget.setLayout(sfpWidget.getRight() + 5, height / 3, (width - sfpWidget.getRight()) - 10, font.lineHeight + 4);
        titleTextWidget.setMessage(new StringTextComponent("Title"));

        musicTextWidget.setLayout(sfpWidget.getRight() + 5, titleTextWidget.getBottom() + 5, (width - sfpWidget.getRight()) - 10, font.lineHeight + 4);
        musicTextWidget.setMessage(new StringTextComponent("MML"));

        addWidget(sfpWidget);
        addWidget(titleTextWidget);
        addWidget(musicTextWidget);
    }

    public void onButtonOpen()
    {
        LOGGER.debug("Nothing to do :P");
    }

    public void onButtonFile()
    {
        Objects.requireNonNull(minecraft).setScreen(new GuiFileImporter(this));
    }

    public void onGuiMXT()
    {
        Objects.requireNonNull(minecraft).setScreen(new GuiMXT(this));
    }

    @Override
    public void render(MatrixStack matrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(matrixStack);
        labelTitle.render(matrixStack, pMouseX, pMouseY, pPartialTicks);
        this.sfpWidget.render(matrixStack, pMouseX, pMouseY, pPartialTicks);
        this.titleTextWidget.render(matrixStack, pMouseX, pMouseY, pPartialTicks);
        this.musicTextWidget.render(matrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(matrixStack, pMouseX, pMouseY, pPartialTicks);

        // Render the Instrument GUI image
        int relX = ((width - sfpWidget.getRight()) / 5) + sfpWidget.getRight();
        int relY = height/5;
        ItemStack itemStack = sfpWidget.getSelected() != null ? ModItems.getMultiInst(sfpWidget.getSelected().getIndex()) : Items.CREEPER_HEAD.getDefaultInstance();
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
