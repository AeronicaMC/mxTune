package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.MXScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.Objects;

public class GuiGroup extends MXScreen
{
    private int counter;
    public GuiGroup()
    {
        super(StringTextComponent.EMPTY);
    }

    @Override
    protected void init()
    {
        super.init();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.fillGradient(pMatrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    @Override
    public void tick()
    {
        if (counter++ % 20 == 0) { /* TODO */ }
        super.tick();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    private ClientPlayerEntity player()
    {
        return Objects.requireNonNull(mc().player);
    }

    private Minecraft mc()
    {
        return Objects.requireNonNull(minecraft);
    }
}
