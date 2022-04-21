package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.gui.widget.list.GuiRedstoneButton;
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
    public static final ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/inv_music_block_gui.png");
    private final GuiRedstoneButton backRSIn = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.DOWN, p -> toggleBackRSIn());
    private final GuiRedstoneButton leftRSOut = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.LEFT, p -> toggleLeftRSOut());
    private final GuiRedstoneButton rightSOut = new GuiRedstoneButton(GuiRedstoneButton.ArrowFaces.RIGHT, p -> toggleRightRSOut());

    public MusicBlockScreen(MusicBlockContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 180;
        this.imageHeight = 176;
    }

    @Override
    protected void init()
    {
        super.init();
        backRSIn.setLayout(leftPos + 141, topPos + 12, 20, 20);
        backRSIn.addHooverText(false, new StringTextComponent("Backside Redstone Input"));
        leftRSOut.setLayout(leftPos + 131, topPos + 32, 20, 20);
        rightSOut.setLayout(leftPos + 151, topPos + 32, 20, 20);
        addButton(backRSIn);
        addButton(leftRSOut);
        addButton(rightSOut);
    }

    private void toggleBackRSIn()
    {

    }

    private void toggleLeftRSOut()
    {
       // TODO:
    }

    private void toggleRightRSOut()
    {
        // TODO:
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
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack , int mouseX, int mouseY) {
        this.font.draw(matrixStack, menu.getName(), 10, 2, 4210752);
        this.font.draw(matrixStack, this.inventory.getDisplayName(), 10, 84, 4210752);
    }
}
