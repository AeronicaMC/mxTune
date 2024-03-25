package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.gui.widget.GuiHelpButton;
import aeronicamc.mods.mxtune.gui.widget.IHooverText;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXSlider;
import aeronicamc.mods.mxtune.items.MusicVenueToolItem;
import aeronicamc.mods.mxtune.render.*;
import aeronicamc.mods.mxtune.util.IInstrument;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

import static aeronicamc.mods.mxtune.gui.ModGuiHelper.*;

public class OverlayManagerScreen extends MXScreen implements IItemOverlayPosition {

    private final OverlayInstance<?>[] visible = { null, null };
    private final Screen parent;
    private IOverlayItem.Position xPosVenueTool;
    private int yPosVenueTool;
    private double lastYPosVenueTool = -1D;
    private IOverlayItem.Position xPosInstrument;
    private int yPosInstrument;
    private double lastYPosInstrument = -1D;

    private final MXButton doneButton = new MXButton(done->onDone());
    private final MXButton cancelButton = new MXButton(cancel->onClose());
    private final MXButton instXPosButton = new MXButton(p->nextInstXPos());
    private final MXSlider instYPosSlider = new MXSlider((p, v)-> applyInstYPos(v));
    private final MXButton toolXPosButton = new MXButton(p->nextToolXPos());
    private final MXSlider toolYPosSlider = new MXSlider((p, v)-> applyToolYPos(v));
    private final GuiHelpButton helpButton = new GuiHelpButton(p -> helpClicked());

    public OverlayManagerScreen(@Nullable Screen parent) {
        super(StringTextComponent.EMPTY);
        this.parent=parent;
        xPosInstrument = MXTuneConfig.getInstrumentOverlayPosition();
        yPosInstrument = MXTuneConfig.getInstrumentOverlayPercent();
        xPosVenueTool = MXTuneConfig.getVenueToolOverlayPosition();
        yPosVenueTool = MXTuneConfig.getVenueToolOverlayPercent();
    }
    
    @Override
    protected void init() {
        super.init();
        setBlitOffset(1000);
        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);
        doneButton.setLayout(((width/2)-25), height-24-20, 50, 20);
        doneButton.setMessage(new TranslationTextComponent("gui.done").withStyle(TextFormatting.GREEN));
        cancelButton.setLayout(((width/2)-25), height-24, 50, 20);
        cancelButton.setMessage(new TranslationTextComponent("gui.cancel").withStyle(TextFormatting.YELLOW));
        addButton(doneButton);
        addButton(cancelButton);
        instYPosSlider.setLayout(doneButton.getLeft() - 150 - 4, height-24-20, 150, 20);
        instYPosSlider.forceValue(yPosInstrument /100D);
        instYPosSlider.setMessage(new TranslationTextComponent("gui.mxtune.button.inst_y_pos_slider"));
        addButton(instYPosSlider);
        instXPosButton.setLayout(instYPosSlider.getLeft(), height-24, 50, 20);
        instXPosButton.setMessage(new TranslationTextComponent(xPosInstrument.getPositionKey()));
        addButton(instXPosButton);
        toolYPosSlider.setLayout( doneButton.getRight() + 4, height-24-20, 150, 20);
        toolYPosSlider.forceValue(yPosVenueTool /100D);
        toolYPosSlider.setMessage(new TranslationTextComponent("gui.mxtune.button.tool_y_pos_slider"));
        addButton(toolYPosSlider);
        toolXPosButton.setLayout(toolYPosSlider.getRight()-50, height-24, 50, 20);
        toolXPosButton.setMessage(new TranslationTextComponent(xPosVenueTool.getPositionKey()));
        addButton(toolXPosButton);
        helpButton.setPosition(toolYPosSlider.getLeft(), height-24);
        addButton(helpButton);
        updateButtonState();
    }

    @Override
    public IOverlayItem.Position getPosition(IOverlayItem overlayItem) {

        if (overlayItem.getItemStack().getItem() instanceof IInstrument)
            return xPosInstrument;
        else if (overlayItem.getItemStack().getItem() instanceof MusicVenueToolItem)
            return xPosVenueTool;
        else
            return IOverlayItem.Position.LEFT;
    }

    @Override
    public float getPercent(IOverlayItem overlayItem) {
        if (overlayItem.getItemStack().getItem() instanceof IInstrument)
            return yPosInstrument;
        else if (overlayItem.getItemStack().getItem() instanceof MusicVenueToolItem)
            return yPosVenueTool;
        else
            return 0F;
    }

    private void nextInstXPos() {
        xPosInstrument = IOverlayItem.Position.nextPosition(xPosInstrument);
        instXPosButton.setMessage(new TranslationTextComponent(xPosInstrument.getPositionKey()));
        updateButtonState();
    }

    private void applyInstYPos(double value) {
        if ((100 * value) != lastYPosInstrument) {
            yPosInstrument = ((int) (100 * value));
            lastYPosInstrument = yPosInstrument;
            instYPosSlider.setMessage(new StringTextComponent("Instrument VPos"));
        }
    }

    private void nextToolXPos() {
        xPosVenueTool = IOverlayItem.Position.nextPosition(xPosVenueTool);
        toolXPosButton.setMessage(new TranslationTextComponent(xPosVenueTool.getPositionKey()));
        updateButtonState();
    }

    private void applyToolYPos(double value) {
        if ((100 * value) != lastYPosVenueTool) {
            yPosVenueTool = ((int) (100 * value));
            lastYPosVenueTool = yPosVenueTool;
            toolYPosSlider.setMessage(new StringTextComponent("Venue Tool VPos"));
        }
    }

    private void helpClicked()
    {
        helpButton.setHelpEnabled(!helpButton.isHelpEnabled());
        updateButtonState();
    }

    private void updateButtonState() {
        doneButton.addHooverText(true, new TranslationTextComponent("gui.done").withStyle(TextFormatting.RESET));
        doneButton.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.oms_done.help01").withStyle(TextFormatting.YELLOW));

        cancelButton.addHooverText(true, new TranslationTextComponent("gui.cancel").withStyle(TextFormatting.RESET));
        cancelButton.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.oms_cancel.help01").withStyle(TextFormatting.YELLOW));

        instYPosSlider.addHooverText(true, new TranslationTextComponent("gui.mxtune.button.inst_y_pos_slider").withStyle(TextFormatting.RESET));
        instYPosSlider.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.inst_y_pos_slider.help01").withStyle(TextFormatting.YELLOW));
        instYPosSlider.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.inst_y_pos_slider.help02").withStyle(TextFormatting.GREEN));
        toolYPosSlider.addHooverText(true, new TranslationTextComponent("gui.mxtune.button.tool_y_pos_slider").withStyle(TextFormatting.RESET));
        toolYPosSlider.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.tool_y_pos_slider.help01").withStyle(TextFormatting.YELLOW));
        toolYPosSlider.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.tool_y_pos_slider.help02").withStyle(TextFormatting.GREEN));

        instXPosButton.addHooverText(true, new TranslationTextComponent(xPosInstrument.getPositionKey()).withStyle(TextFormatting.RESET));
        instXPosButton.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.inst_x_pos_enum.help01").withStyle(TextFormatting.YELLOW));
        toolXPosButton.addHooverText(true, new TranslationTextComponent(xPosVenueTool.getPositionKey()).withStyle(TextFormatting.RESET));
        toolXPosButton.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.tool_x_pos_enum.help01").withStyle(TextFormatting.YELLOW));

        helpButton.addHooverText(true, HELP_HELP01);
        helpButton.addHooverText(false, helpButton.isHelpEnabled() ? HELP_HELP02 : HELP_HELP03);
        buttons.stream().filter(IHooverText.class::isInstance)
                .forEach(b -> ((IHooverText) b).setHooverTextOverride(helpButton.isHelpEnabled()));
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    private void onDone() {
        MXTuneConfig.CLIENT.instrumentOverlayXPosition.set(xPosInstrument);
        MXTuneConfig.CLIENT.instrumentOverlayYPercent.set(yPosInstrument);
        MXTuneConfig.CLIENT.venueToolOverlayXPosition.set(xPosVenueTool);
        MXTuneConfig.CLIENT.venueToolOverlayYPercent.set(yPosVenueTool);
        onClose();
    }

    @Override
    public void onClose() {
        getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
        if (parent != null)
            getMinecraft().setScreen(parent);
        else
            super.onClose();
    }
    
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Nullable
    private OverlayInstance<?> getOverLayInstance(int idx) {
        if (0 == idx)
            return new OverlayInstance<>(this, new InstrumentOverlay());
        else if (1 == idx)
            return new OverlayInstance<>(this, new VenueToolOverlay());
        return null;
    }

    private int zPos;
    private void render(MatrixStack pPoseStack) {
        if (!getMinecraft().options.hideGui) {
            OverlayInstance<?> overlayInstance;
            for(int instIndex = 0; instIndex < this.visible.length; ++instIndex) {
                overlayInstance = null;
                if (this.visible[instIndex] == null && getMinecraft().player != null) {
                    this.visible[instIndex] = getOverLayInstance(instIndex);
                }

                if (this.visible[instIndex] != null)
                    overlayInstance = this.visible[instIndex];
                if (overlayInstance != null && overlayInstance.render(getMinecraft().getWindow().getGuiScaledWidth(), getMinecraft().getWindow().getGuiScaledHeight(), zPos, pPoseStack)) {
                    this.visible[instIndex] = null;
                    zPos = ((zPos+1) % 3)-1;
                }
            }
        }
    }

    @Override
    public void renderBackground(MatrixStack pMatrixStack) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this, pMatrixStack));
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        render(pMatrixStack);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
