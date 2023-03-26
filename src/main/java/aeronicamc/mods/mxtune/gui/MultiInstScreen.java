package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.gui.widget.GuiHelpButton;
import aeronicamc.mods.mxtune.gui.widget.GuiVSlideSwitch;
import aeronicamc.mods.mxtune.gui.widget.IHooverText;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.inventory.MultiInstContainer;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.ChooseInstrumentMessage;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.ISlotChangedCallback;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;

import static aeronicamc.mods.mxtune.gui.ModGuiHelper.*;
import static aeronicamc.mods.mxtune.util.SheetMusicHelper.getSheetMusicSoundProxyIndex;

public class MultiInstScreen extends ContainerScreen<MultiInstContainer> implements ISlotChangedCallback
{
    private static final ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/multi_inst_item.png");
    private static final ITextComponent AUTO_SELECT_ON = new TranslationTextComponent("gui.mxtune.switch.multi_inst_screen.auto_select_on").withStyle(TextFormatting.DARK_GRAY);
    private static final ITextComponent AUTO_SELECT_OFF = new TranslationTextComponent("gui.mxtune.switch.multi_inst_screen.auto_select_off").withStyle(TextFormatting.DARK_GRAY);
    private static final ITextComponent AUTO_SELECT_HELP01 = new TranslationTextComponent("gui.mxtune.switch.multi_inst_screen.auto_select.help01").withStyle(TextFormatting.RESET);
    private static final ITextComponent AUTO_SELECT_HELP02 = new TranslationTextComponent("gui.mxtune.switch.multi_inst_screen.auto_select.help02").withStyle(TextFormatting.GREEN);
    private static final ITextComponent AUTO_SELECT_HELP03 = new TranslationTextComponent("gui.mxtune.switch.multi_inst_screen.auto_select.help03").withStyle(TextFormatting.YELLOW);
    private final MXButton buttonChangeInstrument = new MXButton(this::openSelector);
    private final GuiVSlideSwitch autoSelectState = new GuiVSlideSwitch(p -> onChangeAuto());
    private final GuiHelpButton helpButton = new GuiHelpButton(p -> helpClicked());

    public MultiInstScreen(MultiInstContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 184;
        this.imageHeight = 166;
        getMenu().setSlotChangedCallback(this);
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

        autoSelectState.setMessage(AUTO_SELECT_ON);
        autoSelectState.setLayout(xPos + 18 + 32 + 10, yPos + 36, 20, 20);
        this.addButton(autoSelectState);

        helpButton.setLayout(leftPos + imageWidth - 12 - 20, yPos + 53, 20, 20);
        this.addButton(helpButton);
        getSignals();
    }

    int getInstrument()
    {
        return ((IInstrument)inventory.getSelected().getItem()).getPatch(inventory.getSelected());
    }

    void updateButton(int selected)
    {
        int patch = autoSelectState.getOnOff() && SheetMusicHelper.hasSheetMusic(inventory.getSelected()) ? SheetMusicHelper.getSuggestedInstrumentIndex(inventory.getSelected()) : selected;
        ((IInstrument) inventory.getSelected().getItem()).setPatch(inventory.getSelected(), patch);
        buttonChangeInstrument.setMessage(new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(patch)));
        updateSignals();
    }

    private void helpClicked()
    {
        helpButton.setHelpEnabled(!helpButton.isHelpEnabled());
        buttons.stream().filter(b -> b instanceof IHooverText)
                .forEach(b -> ((IHooverText) b).setHooverTextOverride(helpButton.isHelpEnabled()));
        updateButtonStatuses();
    }

    void updateButtonStatuses()
    {
        autoSelectState.addHooverText(true, AUTO_SELECT_HELP01);
        autoSelectState.addHooverText(false, AUTO_SELECT_HELP02);
        autoSelectState.addHooverText(false, AUTO_SELECT_HELP03);
        autoSelectState.addHooverText(false, autoSelectState.getOnOff() ? AUTO_SELECT_ON.plainCopy().withStyle(TextFormatting.AQUA) : AUTO_SELECT_OFF.plainCopy().withStyle(TextFormatting.AQUA));
        autoSelectState.setMessage(autoSelectState.getOnOff() ? AUTO_SELECT_ON : AUTO_SELECT_OFF);
        helpButton.addHooverText(true, HELP_HELP01);
        helpButton.addHooverText(false, helpButton.isHelpEnabled() ? HELP_HELP02 : HELP_HELP03);
    }

    private void getSignals()
    {
        int signals = getMenu().getSignals();
        int patch = (signals & 0x00FF);
        boolean autoSelect = (signals & 0x2000) > 0;
        autoSelectState.setOnOff(autoSelect);
        updateButton(patch);
        updateButtonStatuses();
    }

    private void updateSignals()
    {
        int signals = 0;
        signals += ((IInstrument)inventory.getSelected().getItem()).getPatch(inventory.getSelected()) & 0x00FF;
        signals += autoSelectState.getOnOff() ? 0x2000 : 0;
        getMenu().setSignals(signals);
        PacketDispatcher.sendToServer(new ChooseInstrumentMessage(signals));
    }

    private void onChangeAuto()
    {
        autoSelectState.setOnOff(!autoSelectState.getOnOff()); // toggle
        if (autoSelectState.getOnOff() && SheetMusicHelper.hasSheetMusic(inventory.getSelected()))
            updateButton(0);
        else updateSignals();
        updateButtonStatuses();
    }

    @Override
    public void onItemStackInserted(int slotIndex, ItemStack itemStack, Type operation)
    {
        if (autoSelectState.getOnOff() && operation.equals(Type.Inserted) && slotIndex == 0 && SheetMusicHelper.hasMusicText(itemStack))
            updateButton(getSheetMusicSoundProxyIndex(itemStack));
    }

    @Override
    public void tick()
    {
        super.tick();

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
        ModGuiHelper.drawHooveringHelp(matrixStack, this, buttons, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack , int mouseX, int mouseY) {
        this.font.draw(matrixStack, new TranslationTextComponent("container.inventory"), 10, 72, TextColorFg.DARK_GRAY);
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
