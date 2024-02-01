package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.TextColorFg;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXLabel;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.managers.Group;
import aeronicamc.mods.mxtune.managers.GroupClient;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.SendPinEntryMessage;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class GuiPin extends MXScreen
{
    private final static int RETURN = 0x23CE; // Return Symbol
    private final static int DEL = 0x2421; // DEL Symbol For Delete
    private final static int BS = 0x2408; // BS Symbol For Backspace

    private final static int[] keys = {GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3,
                                       GLFW.GLFW_KEY_4, GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_6,
                                       GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8, GLFW.GLFW_KEY_9,
                                       DEL, GLFW.GLFW_KEY_0, RETURN
    };
    private final static int[][] numPadLayout = {{0,0}, {1,0}, {2,0},
                                                 {0,1}, {1,1}, {2,1},
                                                 {0,2}, {1,2}, {2,2},
                                                 {0,3}, {1,3}, {2,3}};
    private final MXButton[] numPad = new MXButton[12];
    private final MXTextFieldWidget pinDisplay = new MXTextFieldWidget(4);
    private final MXLabel groupDisplay = new MXLabel();
    private final int groupId;
    private int charPos;
    private int counter;
    private int lastHash;

    public GuiPin(int groupId)
    {
        super(StringTextComponent.EMPTY);
        this.groupId = groupId;
        int index = 0;
        charPos = 0;
        pinDisplay.setCursorPosition(0);
        pinDisplay.active = false;
        pinDisplay.setEditable(false);
        pinDisplay.setTextColorUneditable(TextColorFg.WHITE);
        for (int key : keys)
        {
            numPad[index] = new MXButton(press -> numPress(key));
            index++;
        }
        ITextComponent msg = getGroupLeaderInfo(groupDisplay, player(), groupId);
        groupDisplay.setLabelText(msg);
        lastHash = msg.hashCode();
        disableSubmitButton();
    }

    @Override
    protected void init()
    {
        super.init();
        int left = width;
        int top = height;
        int minWidth  = 4 + 30 + 2 + 30 + 2 + 30 + 4;
        int minHeight = 4 + 20 + 2 + 20 + 2 + 20 + 4 + 20 + 2;
        int xPos;
        int yPos;
        int numPadLeft = (left - minWidth) / 2;
        int numPadTop = (top - minHeight) / 2;
        xPos = numPadLeft;
        yPos = numPadTop;
        pinDisplay.setLayout(numPadLeft + 4, numPadTop - 21, minWidth - 8, 20);
        for (int index = 0; index < numPadLayout.length; index++)
        {
            numPad[index].setLayout(xPos + numPadLayout[index][0] + 4,
                                       yPos + numPadLayout[index][1] + 4, 30, 20);
            numPad[index].setMessage(new StringTextComponent(String.valueOf((char) keys[index])));
            addButton(numPad[index]);
            if (numPadLayout[index][0] == 2) {xPos = numPadLeft;}
            else {xPos += 30 + 2;}
            if (numPadLayout[index][0] == 2) {yPos += 20 + 2;}
        }
        groupDisplay.setLabelText(getGroupLeaderInfo(groupDisplay, player(), groupId));
        int groupDisplayWidth = Math.max(getFont().width(groupDisplay.getLabelText()) + 8, minWidth - 8);
        int groupLeft = (width - groupDisplayWidth) / 2;
        groupDisplay.setCentered(true);
        groupDisplay.setLayout(groupLeft, pinDisplay.y - 26, groupDisplayWidth, 20);
    }

    private int numPress(int codePoint)
    {
        int cp = codePoint;
        if (cp == RETURN || cp == GLFW.GLFW_KEY_ENTER) cp = RETURN;
        if (cp == DEL || cp == BS || cp == GLFW.GLFW_KEY_DELETE || cp == GLFW.GLFW_KEY_BACKSPACE) cp = DEL;
        if (cp >= GLFW.GLFW_KEY_0 && cp <= GLFW.GLFW_KEY_9)
        {
            pinDisplay.setCursorPosition(charPos);
            pinDisplay.insertText(String.valueOf(((char) codePoint)));
            charPos = charPos++ == 3 ? 0 : charPos;
            updateSubmitButtonState();
        } else if (cp == DEL)
        {
            pinDisplay.setValue("");
            charPos = 0;
            disableSubmitButton();
        } else if (cp == RETURN && canSubmit())
        {
            PacketDispatcher.sendToServer(new SendPinEntryMessage(pinDisplay.getValue()));
            pinDisplay.setValue("");
            updateSubmitButtonState();
            this.onClose();
        }
        return cp;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        return super.keyPressed(numPress(pKeyCode), pScanCode, pModifiers);
    }

    private boolean canSubmit()
    {
        return pinDisplay.getValue().length() == 4;
    }

    private void updateSubmitButtonState()
    {
        numPad[11].active = pinDisplay.getValue().length() == 4;
    }

    private void disableSubmitButton()
    {
        numPad[11].active = false;
    }

    static ITextComponent getGroupLeaderInfo(MXLabel widget, ClientPlayerEntity player, int groupId)
    {
        Group group = GroupClient.getGroupById(groupId);
        if (group.isEmpty())
        {
            widget.setTextColor(TextColorFg.YELLOW);
            return new TranslationTextComponent("gui.mxtune.gui_pin.group_disbanded").withStyle(TextFormatting.YELLOW);
        } else
        {
            Entity entity;
            if ((entity = player.level.getEntity(group.getLeader())) != null)
            {
                ITextComponent name = entity.getDisplayName();
                widget.setTextColor(TextColorFg.GREEN);
                return new TranslationTextComponent("gui.mxtune.gui_pin.leaders_group", name.getString()).withStyle(TextFormatting.GREEN);
            } else
            {
                widget.setTextColor(TextColorFg.RED);
                return new TranslationTextComponent("gui.mxtune.gui_pin.unexpected_error").withStyle(TextFormatting.RED);
            }
        }
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.fillGradient(pMatrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        groupDisplay.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        pinDisplay.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    @Override
    public void tick()
    {
        if (counter++ % 20 == 0)
        {
            ITextComponent msg = getGroupLeaderInfo(groupDisplay, player(), groupId);
            groupDisplay.setLabelText(msg);
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

    private ClientPlayerEntity player()
    {
        return Objects.requireNonNull(getMC().player);
    }
}
