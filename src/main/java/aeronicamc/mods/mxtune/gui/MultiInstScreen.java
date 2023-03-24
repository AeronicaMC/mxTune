package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.gui.widget.GuiVSlideSwitch;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.inventory.MultiInstContainer;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.ChooseInstrumentMessage;
import aeronicamc.mods.mxtune.util.IChangedCallBack;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;

public class MultiInstScreen extends ContainerScreen<MultiInstContainer> implements IChangedCallBack
{
    private static final ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/multi_inst_item.png");
    private static final ITextComponent LABEL_AUTO = new TranslationTextComponent("gui.mxtune.MultiInstScreen.auto_select_instrument");
    private final MXButton buttonChangeInstrument = new MXButton(this::openSelector);
    private final GuiVSlideSwitch autoSelectState = new GuiVSlideSwitch(p -> onChangeAuto());
    private int count;
    public MultiInstScreen(MultiInstContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 184;
        this.imageHeight = 166;
        screenContainer.setChainedCallback(this);
    }

    private void openSelector(Button button)
    {
        Objects.requireNonNull(minecraft).setScreen(new GuiMultiInstChooser(this, getMenu()));
    }

    @Override
    protected void init()
    {
        super.init();

        int yPos = topPos + 6;
        int xPos = leftPos + 12;

        buttonChangeInstrument.setLayout(xPos, yPos, imageWidth - 24, 20);
        this.addButton(buttonChangeInstrument);

        autoSelectState.setMessage(LABEL_AUTO);
        autoSelectState.setLayout(xPos + 18 + 32 + 10, yPos + 36, /* Objects.requireNonNull(minecraft).font.width(CHECKBOX_AUTO) + 30*/ 20, 20);
        this.addButton(autoSelectState);

        getSignals();
        //updateButton(((IInstrument)inventory.getSelected().getItem()).getPatch(inventory.getSelected()));
    }

    int getInstrument()
    {
        return ((IInstrument)inventory.getSelected().getItem()).getPatch(inventory.getSelected());
    }

    boolean isAutoSelectEnable()
    {
        return autoSelectState.getOnOff();
    }

    void updateButton(int selected)
    {
        int patch = autoSelectState.getOnOff() && SheetMusicHelper.hasSheetMusic(inventory.getSelected()) ? SheetMusicHelper.getSuggestedInstrumentIndex(inventory.getSelected()) : selected;
        ((IInstrument) inventory.getSelected().getItem()).setPatch(inventory.getSelected(), patch);
        buttonChangeInstrument.setMessage(new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(patch)));
        updateSignals();
    }

    private void getSignals()
    {
        int signals = getMenu().getSignals();
        int patch = (signals & 0x0FFF);
        boolean autoSelect = (signals & 0x2000) > 0;
        autoSelectState.setOnOff(autoSelect);
        updateButton(patch);
        ((IInstrument)inventory.getSelected().getItem()).setPatch(inventory.getSelected(), (signals & 0x0FFF));
        ((IInstrument)inventory.getSelected().getItem()).setAutoSelect(inventory.getSelected(),(signals & 0x2000) > 0);
    }

    private void updateSignals()
    {
        int signals = 0;
        signals += ((IInstrument)inventory.getSelected().getItem()).getPatch(inventory.getSelected()) & 0x0FFF;
        signals += autoSelectState.getOnOff() ? 0x2000 : 0;
        PacketDispatcher.sendToServer(new ChooseInstrumentMessage(signals));
    }

    private void onChangeAuto()
    {
        autoSelectState.setOnOff(!autoSelectState.getOnOff()); // toggle
        if (autoSelectState.getOnOff() && SheetMusicHelper.hasSheetMusic(inventory.getSelected()))
            updateButton(0);
        else updateSignals();
    }

    @Override
    public void onChangedCallback()
    {
        this.count++;
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
        ModGuiHelper.RenderGuiItemScaled(this.itemRenderer, inventory.getSelected(),
                getGuiLeft() + 51, getGuiTop() + 50, 2, true);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack , int mouseX, int mouseY) {
        this.font.draw(matrixStack, new TranslationTextComponent("container.inventory"), 10, 72, TextColorFg.DARK_GRAY);
        this.font.draw(matrixStack, new StringTextComponent(String.format("%d", count)), 70, 72, TextColorFg.DARK_GRAY);

    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        // Prevent the Instrument from being swapped
        if (Objects.requireNonNull(this.minecraft).options.keyHotbarSlots[inventory.selected].isActiveAndMatches(InputMappings.getKey(pKeyCode, pScanCode)) || (hoveredSlot != null && hoveredSlot.index == inventory.selected))
            return true;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
