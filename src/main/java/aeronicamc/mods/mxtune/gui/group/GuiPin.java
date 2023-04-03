package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
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
    private final int groupId;

    public GuiPin(Screen parent, int groupId)
    {
        super(StringTextComponent.EMPTY);
        this.parent = parent;
        this.groupId = groupId;
        int[] index = new int[1];
        for (int key : keys)
        {
            numPad[index[0]] = new MXButton(press -> numPress(key));
            index[0]++;
        }

    }

    private void numPress(int codePoint)
    {
        int cp = codePoint;
        if (cp == RETURN || cp == GLFW.GLFW_KEY_ENTER) cp = RETURN;
        if (cp == DEL || cp == BS || cp == GLFW.GLFW_KEY_DELETE || cp == GLFW.GLFW_KEY_BACKSPACE) cp = DEL;
        player().chat(String.format("char: %s, codePoint: %d, screen[%d x %d]", ((char)codePoint), cp, width, height));
    }

    @Override
    protected void init()
    {
        super.init();
        int left = width / 3;
        int top = height / 3;
        int minWidth = 4 + 20 + 2 + 20 + 2 + 20 + 4;
        int minHeight = minWidth + 20 + 2;
        int[] index = new int[1];
        int[] xPos = new int[1];
        int[] yPos = new int[1];
        final int numPadLeft = left + ((left - minWidth) / 2);
        final int numPadTop = top + ((top - minHeight) / 2);
        xPos[0] = numPadLeft;
        yPos[0] = numPadTop;
        Arrays.stream(numPadLayout).forEach(pair->{
            numPad[index[0]].setLayout(xPos[0] + numPadLayout[index[0]][0] + 4,
                                       yPos[0] + numPadLayout[index[0]][1] + 4, 20, 20);
            numPad[index[0]].setMessage(new StringTextComponent(String.valueOf((char)keys[index[0]])));
            addButton(numPad[index[0]]);
            if (numPadLayout[index[0]][0] == 2) { xPos[0] = numPadLeft; } else { xPos[0] += 20 + 2; }
            if (numPadLayout[index[0]][0] == 2) { yPos[0] += 20 + 2; }
            index[0]++;
        });
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
