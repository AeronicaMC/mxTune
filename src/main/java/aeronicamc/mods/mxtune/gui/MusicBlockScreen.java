package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.ILockable;
import aeronicamc.mods.mxtune.blocks.LockableHelper;
import aeronicamc.mods.mxtune.gui.widget.GuiHelpButton;
import aeronicamc.mods.mxtune.gui.widget.GuiLockButton;
import aeronicamc.mods.mxtune.gui.widget.GuiRedstoneButton;
import aeronicamc.mods.mxtune.gui.widget.IHooverText;
import aeronicamc.mods.mxtune.inventory.MusicBlockContainer;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.MusicBlockMessage;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;

import static aeronicamc.mods.mxtune.gui.ModGuiHelper.*;

public class MusicBlockScreen extends ContainerScreen<MusicBlockContainer>
{
    public static final ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/music_block.png");

    private static final ITextComponent LOCK_HELP01 = new TranslationTextComponent("gui.mxtune.button.lock.help01").withStyle(TextFormatting.RESET);
    private static final ITextComponent LOCK_HELP02 = new TranslationTextComponent("gui.mxtune.button.lock.help02").withStyle(TextFormatting.GREEN);
    private static final ITextComponent LOCK_HELP03 = new TranslationTextComponent("gui.mxtune.button.lock.help03").withStyle(TextFormatting.YELLOW);
    private static final ITextComponent LOCK_HELP04 = new TranslationTextComponent("gui.mxtune.button.lock.help04").withStyle(TextFormatting.GREEN);
    private static final ITextComponent LOCK_HELP05 = new TranslationTextComponent("gui.mxtune.button.lock.help05").withStyle(TextFormatting.YELLOW);
    private static final ITextComponent LOCK_HELP06 = new TranslationTextComponent("gui.mxtune.button.lock.help06").withStyle(TextFormatting.RED);
    private static final ITextComponent LOCK_LOCKED = new TranslationTextComponent("gui.mxtune.button.lock.locked").withStyle(TextFormatting.AQUA);
    private static final ITextComponent LOCK_UNLOCKED = new TranslationTextComponent("gui.mxtune.button.lock.unlocked").withStyle(TextFormatting.AQUA);
    private static final ITextComponent BACK_RS_IN_HELP01 = new TranslationTextComponent("gui.mxtune.button.back_rs_in.help01").withStyle(TextFormatting.RESET);
    private static final ITextComponent BACK_RS_IN_HELP02 = new TranslationTextComponent("gui.mxtune.button.back_rs_in.help02").withStyle(TextFormatting.GREEN);
    private static final ITextComponent BACK_RS_IN_HELP03 = new TranslationTextComponent("gui.mxtune.button.back_rs_in.help03").withStyle(TextFormatting.YELLOW);
    private static final ITextComponent BACK_RS_IN_ENABLED = new TranslationTextComponent("gui.mxtune.button.back_rs_in.enabled").withStyle(TextFormatting.AQUA);
    private static final ITextComponent BACK_RS_IN_DISABLED = new TranslationTextComponent("gui.mxtune.button.back_rs_in.disabled").withStyle(TextFormatting.AQUA);
    private static final ITextComponent LEFT_RS_OUT_HELP01 = new TranslationTextComponent("gui.mxtune.button.left_rs_out.help01").withStyle(TextFormatting.RESET);
    private static final ITextComponent LEFT_RS_OUT_HELP02 = new TranslationTextComponent("gui.mxtune.button.left_rs_out.help02").withStyle(TextFormatting.GREEN);
    private static final ITextComponent LEFT_RS_OUT_HELP03 = new TranslationTextComponent("gui.mxtune.button.left_rs_out.help03").withStyle(TextFormatting.YELLOW);
    private static final ITextComponent LEFT_RS_OUT_ENABLED = new TranslationTextComponent("gui.mxtune.button.left_rs_out.enabled").withStyle(TextFormatting.AQUA);
    private static final ITextComponent LEFT_RS_OUT_DISABLED = new TranslationTextComponent("gui.mxtune.button.left_rs_out.disabled").withStyle(TextFormatting.AQUA);
    private static final ITextComponent RIGHT_RS_OUT_HELP01 = new TranslationTextComponent("gui.mxtune.button.right_rs_out.help01").withStyle(TextFormatting.RESET);
    private static final ITextComponent RIGHT_RS_OUT_HELP02 = new TranslationTextComponent("gui.mxtune.button.right_rs_out.help02").withStyle(TextFormatting.GREEN);
    private static final ITextComponent RIGHT_RS_OUT_HELP03 = new TranslationTextComponent("gui.mxtune.button.right_rs_out.help03").withStyle(TextFormatting.YELLOW);
    private static final ITextComponent RIGHT_RS_OUT_ENABLED = new TranslationTextComponent("gui.mxtune.button.right_rs_out.enabled").withStyle(TextFormatting.AQUA);
    private static final ITextComponent RIGHT_RS_OUT_DISABLED = new TranslationTextComponent("gui.mxtune.button.right_rs_out.disabled").withStyle(TextFormatting.AQUA);

    private final GuiHelpButton helpButton = new GuiHelpButton(p -> helpClicked());
    private final GuiLockButton lockButton = new GuiLockButton(p -> toggleLock());
    private final GuiRedstoneButton backRSIn = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.DOWN, p -> toggleBackRSIn());
    private final GuiRedstoneButton leftRSOut = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.LEFT, p -> toggleLeftRSOut());
    private final GuiRedstoneButton rightSOut = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.RIGHT, p -> toggleRightRSOut());

    NetworkPlayerInfo netPlayerInfo;
    ITextComponent ownerName;

    public MusicBlockScreen(MusicBlockContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 186;
        this.imageHeight = 184;
        this.minecraft = Minecraft.getInstance();

        this.netPlayerInfo = minecraft.getConnection() != null ? minecraft.getConnection().getPlayerInfo(((ILockable)menu.getBlockEntity()).getOwner()) : null;
        this.ownerName = new StringTextComponent(netPlayerInfo != null ? netPlayerInfo.getProfile().getName() : "-offline-").withStyle(TextFormatting.ITALIC);
    }

    @Override
    protected void init()
    {
        super.init();
        boolean isLockActive = LockableHelper.canLock(menu.getPlayerEntity(), (ILockable) menu.getBlockEntity());
        boolean isActive = LockableHelper.canManage(menu.getPlayerEntity(), (ILockable) menu.getBlockEntity());
        lockButton.setLayout(leftPos + 21, topPos + 17, 20, 20);
        lockButton.setLocked(menu.getLockedState());
        lockButton.active = isLockActive;
        backRSIn.setLayout(leftPos + 146, topPos + 17, 20, 20);
        backRSIn.setSignalEnabled(menu.getBackRSInState());
        backRSIn.active = isActive;
        leftRSOut.setLayout(leftPos + 136, topPos + 37, 20, 20);
        leftRSOut.setSignalEnabled(menu.getLeftRSOutState());
        leftRSOut.active = isActive;
        rightSOut.setLayout(leftPos + 156, topPos + 37, 20, 20);
        rightSOut.setSignalEnabled(menu.getRightRSOutState());
        rightSOut.active = isActive;
        helpButton.setLayout(leftPos + 21, topPos + 37, 20, 20);

        addButton(lockButton);
        addButton(backRSIn);
        addButton(leftRSOut);
        addButton(rightSOut);
        addButton(helpButton);
        updateButtonStatuses();
    }

    private void helpClicked()
    {
        helpButton.setHelpEnabled(!helpButton.isHelpEnabled());
        buttons.stream().filter(b -> b instanceof IHooverText)
                .forEach(b -> ((IHooverText) b).setHooverTextOverride(helpButton.isHelpEnabled()));
        updateButtonStatuses();
    }

    private void toggleLock()
    {
        lockButton.setLocked(!lockButton.isLocked());
        updateSignals();
    }

    private void toggleBackRSIn()
    {
        backRSIn.setSignalEnabled(!backRSIn.isSignalEnabled());
        updateSignals();
    }

    private void toggleLeftRSOut()
    {
       leftRSOut.setSignalEnabled(!leftRSOut.isSignalEnabled());
        updateSignals();
    }

    private void toggleRightRSOut()
    {
        rightSOut.setSignalEnabled(!rightSOut.isSignalEnabled());
        updateSignals();
    }

    private void updateSignals()
    {
        int signals = 0;
        signals += backRSIn.isSignalEnabled() ? 1 : 0;
        signals += leftRSOut.isSignalEnabled() ? 2 : 0;
        signals += rightSOut.isSignalEnabled() ? 4 : 0;
        signals += lockButton.isLocked() ? 8 : 0;
        PacketDispatcher.sendToServer(new MusicBlockMessage(menu.getPosition(), signals));
    }

    private void updateButtonStatuses()
    {
        boolean isLockActive = LockableHelper.canLock(menu.getPlayerEntity(), (ILockable) menu.getBlockEntity());
        boolean isActive = LockableHelper.canManage(menu.getPlayerEntity(), (ILockable) menu.getBlockEntity());
        lockButton.setLocked(menu.getLockedState());
        lockButton.addHooverText(true, LOCK_HELP01);
        lockButton.addHooverText(false, LOCK_HELP02);
        lockButton.addHooverText(false, LOCK_HELP03);
        lockButton.addHooverText(false, LOCK_HELP04);
        lockButton.addHooverText(false, LOCK_HELP05);
        lockButton.addHooverText(false, LOCK_HELP06);
        lockButton.addHooverText(false, menu.getLockedState() ? LOCK_LOCKED : LOCK_UNLOCKED);
        lockButton.active = isLockActive;
        backRSIn.setSignalEnabled(menu.getBackRSInState());
        backRSIn.addHooverText(true, BACK_RS_IN_HELP01);
        backRSIn.addHooverText(false, BACK_RS_IN_HELP02);
        backRSIn.addHooverText(false, BACK_RS_IN_HELP03);
        backRSIn.addHooverText(false, menu.getBackRSInState() ? BACK_RS_IN_ENABLED : BACK_RS_IN_DISABLED);
        backRSIn.active = isActive;
        leftRSOut.setSignalEnabled(menu.getLeftRSOutState());
        leftRSOut.addHooverText(true, LEFT_RS_OUT_HELP01);
        leftRSOut.addHooverText(false, LEFT_RS_OUT_HELP02);
        leftRSOut.addHooverText(false, LEFT_RS_OUT_HELP03);
        leftRSOut.addHooverText(false, menu.getLeftRSOutState() ? LEFT_RS_OUT_ENABLED : LEFT_RS_OUT_DISABLED);
        leftRSOut.active = isActive;
        rightSOut.setSignalEnabled(menu.getRightRSOutState());
        rightSOut.addHooverText(true, RIGHT_RS_OUT_HELP01);
        rightSOut.addHooverText(false, RIGHT_RS_OUT_HELP02);
        rightSOut.addHooverText(false, RIGHT_RS_OUT_HELP03);
        rightSOut.addHooverText(false, menu.getRightRSOutState() ? RIGHT_RS_OUT_ENABLED : RIGHT_RS_OUT_DISABLED);
        rightSOut.active = isActive;
        helpButton.addHooverText(true, HELP_HELP01);
        helpButton.addHooverText(false, helpButton.isHelpEnabled() ? HELP_HELP02 : HELP_HELP03);
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
        updateButtonStatuses();
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
        ModGuiHelper.drawHooveringHelp(matrixStack, this, children, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack , int mouseX, int mouseY) {
        String text = SheetMusicHelper.formatDuration(menu.getDuration());
        int durationWidth = this.font.width(text);
        ITextComponent ownerName = new StringTextComponent(netPlayerInfo != null ? netPlayerInfo.getProfile().getName() : "-offline-").withStyle(TextFormatting.ITALIC);
        int nameWidth = this.font.width(ownerName);
        this.font.draw(matrixStack, SheetMusicHelper.formatDuration(menu.getDuration()), imageWidth - durationWidth - 12F, 6, 4210752);
        this.font.draw(matrixStack, menu.getName(), 12, 6, 4210752);
        this.font.draw(matrixStack, this.inventory.getDisplayName(), 12, 91, 4210752);
        this.font.draw(matrixStack, ownerName, imageWidth - nameWidth - 12F, 91, 4210752);
    }
}
