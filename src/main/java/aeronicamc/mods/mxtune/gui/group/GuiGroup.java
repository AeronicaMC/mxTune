package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.managers.GroupClient;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;

public class GuiGroup extends MXScreen
{
    private final MXButton buttonDone = new MXButton(p -> done());
    private final MXButton buttonCancel = new MXButton(p -> cancel());
    private final MXTextFieldWidget groupDisplay = new MXTextFieldWidget(1024);
    private int counter;
    private int groupId;
    private int lastHash;
    public GuiGroup()
    {
        super(StringTextComponent.EMPTY);
    }

    @Override
    protected void init()
    {
        super.init();
        groupId = GroupClient.getGroup(player().getId()).getGroupId();
        int groupDisplayWidth = mc().font.width(GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId));
        groupDisplay.setLayout(width/2 -groupDisplayWidth /2, 10, groupDisplayWidth + 8, 20);
        ITextComponent msg = GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId);
        lastHash = msg.hashCode();
        groupDisplay.setValue(msg.getString());
        buttonDone.setLayout(10,50,100,20);
        buttonDone.setMessage(new TranslationTextComponent("gui.done"));
        buttonCancel.setLayout(10,82,100,20);
        buttonCancel.setMessage(new TranslationTextComponent("gui.cancel"));
        addButton(buttonDone);
        addButton(buttonCancel);
    }

    private void cancel()
    {
        this.onClose();
    }

    private void done()
    {
        this.onClose();
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
        groupDisplay.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    @Override
    public void tick()
    {
        if (counter++ % 20 == 0)
        {
            ITextComponent msg = GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId);
            groupDisplay.setValue(msg.getString());
            if (lastHash != msg.hashCode())
            {
                lastHash = msg.hashCode();
                this.init();
            }
        }
        super.tick();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc()
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
