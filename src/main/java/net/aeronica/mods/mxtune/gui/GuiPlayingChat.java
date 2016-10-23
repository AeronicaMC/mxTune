/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.StopPlayMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.TabCompleter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPlayingChat extends GuiChat
{
    public static final int GUI_ID = 4;
    private TabCompleter tabCompleter;

    public GuiPlayingChat() {}

    public GuiPlayingChat(String par1Str) {}

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        this.mc.ingameGUI.getChatGUI().resetScroll();
        sendStop();
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.tabCompleter = new GuiPlayingChat.ChatTabCompleter(this.inputField);
    }

    /** Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e). */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.tabCompleter.resetRequested();

        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            this.mc.displayGuiScreen((GuiScreen) null);
        }
        if (keyCode == 15)
        {
            this.tabCompleter.complete();
        } else
        {
            this.tabCompleter.resetDidComplete();
        }

        if (keyCode == 1)
        {
            this.mc.displayGuiScreen((GuiScreen) null);
        } else if (keyCode != 28 && keyCode != 156)
        {
            if (keyCode == 200)
            {
                this.getSentHistory(-1);
            } else if (keyCode == 208)
            {
                this.getSentHistory(1);
            } else if (keyCode == 201)
            {
                this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
            } else if (keyCode == 209)
            {
                this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
            } else
            {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        } else
        {
            String s = this.inputField.getText().trim();

            if (!s.isEmpty())
            {
                this.sendChatMessage(s);
            }

            /*
             * REMOVED since we don't want to exit until we ESCAPE or the tune
             * ends this.mc.displayGuiScreen((GuiScreen) null);
             */
        }
    }

    /** Sets the list of tab completions, as long as they were previously requested. */
    @Override
    public void setCompletions(String... newCompletions)
    {
        this.tabCompleter.setCompletions(newCompletions);
    }

    @SideOnly(Side.CLIENT)
    public static class ChatTabCompleter extends TabCompleter
    {
        /** The instance of the Minecraft client */
        private Minecraft clientInstance = Minecraft.getMinecraft();

        public ChatTabCompleter(GuiTextField p_i46749_1_)
        {
            super(p_i46749_1_, false);
        }

        /**
         * Called when tab key pressed. If it's the first time we tried to
         * complete this string, we ask the server for completions. When the
         * server responds, this method gets called again (via setCompletions).
         */
        public void complete()
        {
            super.complete();

            if (this.completions.size() > 1)
            {
                StringBuilder stringbuilder = new StringBuilder();

                for (String s : this.completions)
                {
                    if (stringbuilder.length() > 0)
                    {
                        stringbuilder.append(", ");
                    }

                    stringbuilder.append(s);
                }

                this.clientInstance.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(stringbuilder.toString()), 1);
            }
        }

        public BlockPos getTargetBlockPos()
        {

            BlockPos blockpos = null;

            if (this.clientInstance.objectMouseOver != null && this.clientInstance.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                blockpos = this.clientInstance.objectMouseOver.getBlockPos();
            }

            return blockpos;
        }
    }

    protected void sendStop()
    {
        PacketDispatcher.sendToServer(new StopPlayMessage(this.mc.thePlayer.getEntityId()));
    }
}