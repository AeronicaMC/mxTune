package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.gui.ModGuiHelper;
import aeronicamc.mods.mxtune.gui.widget.GuiLockButton;
import aeronicamc.mods.mxtune.gui.widget.GuiRedstoneButton;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Objects;

public class MusicBlockScreen extends ContainerScreen<MusicBlockContainer>
{
    public static final ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/music_block.png");
    private final GuiLockButton lockButton = new GuiLockButton(p -> toggleLock());
    private final GuiRedstoneButton backRSIn = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.DOWN, p -> toggleBackRSIn());
    private final GuiRedstoneButton leftRSOut = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.LEFT, p -> toggleLeftRSOut());
    private final GuiRedstoneButton rightSOut = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.RIGHT, p -> toggleRightRSOut());

    public MusicBlockScreen(MusicBlockContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 186;
        this.imageHeight = 184;
    }

    @Override
    protected void init()
    {
        super.init();
        lockButton.setLayout(leftPos + 21, topPos + 17, 20, 20);
        lockButton.addHooverText(true, new StringTextComponent("lock or not"));
        backRSIn.setLayout(leftPos + 146, topPos + 17, 20, 20);
        backRSIn.addHooverText(true, new StringTextComponent("Back side Redstone Input"));
        leftRSOut.setLayout(leftPos + 136, topPos + 37, 20, 20);
        leftRSOut.addHooverText(true, new StringTextComponent("Left side Redstone Output"));
        rightSOut.setLayout(leftPos + 156, topPos + 37, 20, 20);
        rightSOut.addHooverText(true, new StringTextComponent("Right side Redstone Output"));
        addButton(lockButton);
        addButton(backRSIn);
        addButton(leftRSOut);
        addButton(rightSOut);
    }

    private void toggleLock()
    {
        lockButton.setLocked(!lockButton.isLocked());
    }

    private void toggleBackRSIn()
    {
        backRSIn.setSignalEnabled(!backRSIn.isSignalEnabled());
    }

    private void toggleLeftRSOut()
    {
       leftRSOut.setSignalEnabled(!leftRSOut.isSignalEnabled());
    }

    private void toggleRightRSOut()
    {
        rightSOut.setSignalEnabled(!rightSOut.isSignalEnabled());
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
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
        ModGuiHelper.drawHooveringHelp(matrixStack, this, buttons, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack , int mouseX, int mouseY) {
        this.font.draw(matrixStack, menu.getName(), 8, 6, 4210752);
        this.font.draw(matrixStack, this.inventory.getDisplayName(), 8, 92, 4210752);
    }
}
