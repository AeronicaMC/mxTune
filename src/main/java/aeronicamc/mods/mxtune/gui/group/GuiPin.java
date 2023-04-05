package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.TextColorFg;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class GuiPin extends MXScreen
{
    private final Screen parent;
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
    private final int groupId;
    private int charPos;

    public GuiPin(Screen parent, int groupId)
    {
        super(StringTextComponent.EMPTY);
        this.parent = parent;
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
    }

    @Override
    protected void init()
    {
        super.init();
        int left = width / 3;
        int top = height / 3;
        int minWidth  = 4 + 30 + 2 + 30 + 2 + 30 + 4;
        int minHeight = 4 + 20 + 2 + 20 + 2 + 20 + + 4 + 20 + 2;
        int xPos;
        int yPos;
        int numPadLeft = left + ((left - minWidth) / 2);
        int numPadTop = top + ((top - minHeight) / 2);
        xPos = numPadLeft;
        yPos = numPadTop;
        pinDisplay.setLayout(numPadLeft + 4, numPadTop - 22, minWidth - 8, 20);
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
            System.out.printf("char: %s, codePoint: %#4x, pos: %d, screen[%d x %d]%n", ((char)codePoint), cp, charPos, width, height);
            charPos = charPos++ == 3 ? 0 : charPos;
        } else if (cp == DEL)
        {
            pinDisplay.setValue("");
            charPos = 0;
            System.out.printf("Clear");
        } else if (cp == RETURN)
        {
            System.out.printf("Submit %s%n", pinDisplay.getValue());
        }
        return cp;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        return super.keyPressed(numPress(pKeyCode), pScanCode, pModifiers);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.fillGradient(pMatrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        pinDisplay.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
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
