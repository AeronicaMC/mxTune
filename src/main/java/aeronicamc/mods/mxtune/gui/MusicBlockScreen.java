package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.ILockable;
import aeronicamc.mods.mxtune.blocks.LockableHelper;
import aeronicamc.mods.mxtune.gui.widget.GuiLockButton;
import aeronicamc.mods.mxtune.gui.widget.GuiRedstoneButton;
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

import java.util.Objects;

public class MusicBlockScreen extends ContainerScreen<MusicBlockContainer>
{
    public static final ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/music_block.png");
    private final GuiLockButton lockButton = new GuiLockButton(p -> toggleLock());
    private final GuiRedstoneButton backRSIn = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.DOWN, p -> toggleBackRSIn());
    private final GuiRedstoneButton leftRSOut = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.LEFT, p -> toggleLeftRSOut());
    private final GuiRedstoneButton rightSOut = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.RIGHT, p -> toggleRightRSOut());
    private int lastSignals;

    public MusicBlockScreen(MusicBlockContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 186;
        this.imageHeight = 184;
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected void init()
    {
        super.init();
        boolean isLockActive = LockableHelper.canLock(menu.getPlayerEntity(), (ILockable) menu.getBlockEntity());
        boolean isActive = LockableHelper.canManage(menu.getPlayerEntity(), (ILockable) menu.getBlockEntity());
        lockButton.setLayout(leftPos + 21, topPos + 17, 20, 20);
        lockButton.addHooverText(true, new StringTextComponent("lock or not"));
        lockButton.setLocked((menu.getSignals() & 0x0008) > 0);
        lockButton.active = isLockActive;
        backRSIn.setLayout(leftPos + 146, topPos + 17, 20, 20);
        backRSIn.addHooverText(true, new StringTextComponent("Back side Redstone Input"));
        backRSIn.setSignalEnabled((menu.getSignals() & 0x0001) > 0);
        backRSIn.active = isActive;
        leftRSOut.setLayout(leftPos + 136, topPos + 37, 20, 20);
        leftRSOut.addHooverText(true, new StringTextComponent("Left side Redstone Output"));
        leftRSOut.setSignalEnabled((menu.getSignals() & 0x0002) > 0);
        leftRSOut.active = isActive;
        rightSOut.setLayout(leftPos + 156, topPos + 37, 20, 20);
        rightSOut.addHooverText(true, new StringTextComponent("Right side Redstone Output"));
        rightSOut.setSignalEnabled((menu.getSignals() & 0x0004) > 0);
        rightSOut.active = isActive;
        addButton(lockButton);
        addButton(backRSIn);
        addButton(leftRSOut);
        addButton(rightSOut);
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
        lastSignals = signals;
        PacketDispatcher.sendToServer(new MusicBlockMessage(menu.getPosition(), signals));
    }

    private void updateButtonStatuses()
    {
        boolean isLockActive = LockableHelper.canLock(menu.getPlayerEntity(), (ILockable) menu.getBlockEntity());
        boolean isActive = LockableHelper.canManage(menu.getPlayerEntity(), (ILockable) menu.getBlockEntity());
        lockButton.setLocked((menu.getSignals() & 0x0008) > 0);
        lockButton.active = isLockActive;
        backRSIn.setSignalEnabled((menu.getSignals() & 0x0001) > 0);
        backRSIn.active = isActive;
        leftRSOut.setSignalEnabled((menu.getSignals() & 0x0002) > 0);
        leftRSOut.active = isActive;
        rightSOut.setSignalEnabled((menu.getSignals() & 0x0004) > 0);
        rightSOut.active = isActive;
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y)
    {
        //RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
        ModGuiHelper.drawHooveringHelp(matrixStack, this, buttons, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack , int mouseX, int mouseY) {
        String text = SheetMusicHelper.formatDuration(menu.getDuration());
        int durationWidth = this.font.width(text);

        NetworkPlayerInfo playerInfo = minecraft.getConnection() != null ? minecraft.getConnection().getPlayerInfo(((ILockable)menu.getBlockEntity()).getOwner()) : null;
        ITextComponent ownerName = new StringTextComponent(playerInfo != null ? playerInfo.getProfile().getName() : "-offline-").withStyle(TextFormatting.ITALIC);
        int nameWidth = this.font.width(ownerName);
        this.font.draw(matrixStack, SheetMusicHelper.formatDuration(menu.getDuration()), imageWidth - durationWidth - 8, 6, 4210752);
        this.font.draw(matrixStack, menu.getName(), 8, 6, 4210752);
        this.font.draw(matrixStack, this.inventory.getDisplayName(), 8, 92, 4210752);
        this.font.draw(matrixStack, ownerName, imageWidth - nameWidth - 8, 92, 4210752);
    }
}
