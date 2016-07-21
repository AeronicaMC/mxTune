/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.capabilities.IPlayerMusicOptions;
import net.aeronica.mods.mxtune.capabilities.PlayerMusicDefImpl;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.MusicOptionsMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiMusicOptions extends GuiScreen
{
    public static final int GUI_ID = 8;
    private Minecraft mc;
    private String TITLE;

    private GuiButtonExt btn_muteOption;
    private GuiSliderMX btn_midiVolume;
    private GuiButtonExt btn_cancel, btn_done;
    
    private EntityPlayer player;
    private IPlayerMusicOptions musicOptionsInstance;
    
    private float midiVolume;
    private int muteOption;
    private int lastMuteOption = -1;
    
    public GuiMusicOptions() {midiInit();}
    
    @Override
    public void updateScreen()
    {
        // TODO Auto-generated method stub
        updateGuiElments();
        midiUpdate();
        super.updateScreen();
    }

    @Override
    public void initGui()
    {
        this.mc = Minecraft.getMinecraft();
        player = this.mc.thePlayer;
        TITLE = I18n.format("mxtune.gui.GuiMusicOptions.title", new Object[0]);
        musicOptionsInstance = player.getCapability(MXTuneMain.MUSIC_OPTIONS, null);
        midiVolume = musicOptionsInstance.getMidiVolume();
        muteOption = musicOptionsInstance.getMuteOption();
        
        this.buttonList.clear();

        int y = 30;
        int x = (width - 200) / 2;
        btn_muteOption = new GuiButtonExt(0, x, y, 200, 20, (PlayerMusicDefImpl.EnumMuteOptions.byMetadata(muteOption).toString()));
        btn_midiVolume = new GuiSliderMX(1, x, y+=25, 200, 20, I18n.format("mxtune.gui.slider.midiVolume"), midiVolume*100F, 0F, 100F, 1F);
        
        btn_cancel = new GuiButtonExt(2, x, y+=25, 200, 20, I18n.format("gui.cancel"));
        btn_done = new GuiButtonExt(3, x, y+=25, 200, 20, I18n.format("gui.done"));
        
        this.buttonList.add(btn_muteOption);
        this.buttonList.add(btn_midiVolume);
        this.buttonList.add(btn_cancel);
        this.buttonList.add(btn_done);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        /** draw "TITLE" at the top/right column middle */
        int posX = (this.width - getFontRenderer().getStringWidth(TITLE)) / 2 ;
        int posY = 10;
        getFontRenderer().drawStringWithShadow(TITLE, posX, posY, 0xD3D3D3);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException
    {
        /** if button is disabled ignore click */
        if (!guibutton.enabled) return;

        /** id 0 = okay; 1 = cancel; 2 = play; 3 = stop */
        switch (guibutton.id)
        {
        case 0:
            /** Increment Mute Option */
            this.muteOption = ((++this.muteOption) % PlayerMusicDefImpl.EnumMuteOptions.values().length);
            ModLogger.logInfo("muteOption meta: " + muteOption + ", text: " + PlayerMusicDefImpl.EnumMuteOptions.byMetadata(muteOption).toString() +
                    ", enum: " + PlayerMusicDefImpl.EnumMuteOptions.byMetadata(muteOption).name());
            btn_muteOption.displayString = I18n.format(PlayerMusicDefImpl.EnumMuteOptions.byMetadata(muteOption).toString(), new Object[0]);
            break;

        case 1:
            /** Volume */
            updateGuiElments();
            break;
        case 3:
            /** done */
            this.midiVolume = btn_midiVolume.getValue() / 100;
            sendOptionsToServer(this.midiVolume, this.muteOption);
        case 2:
            /** cancel */
            midiClose();
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        default:
        }
    }

    private void updateGuiElments()
    {
        this.midiVolume = btn_midiVolume.getValue() / 100;
        if (this.midiVolume == 0F)
        {
            this.lastMuteOption = this.muteOption;
            btn_muteOption.displayString = PlayerMusicDefImpl.EnumMuteOptions.byMetadata(PlayerMusicDefImpl.EnumMuteOptions.ALL.getMetadata()).toString();
            btn_muteOption.enabled = false;
        } else if (this.lastMuteOption != -1)
        {
            btn_muteOption.enabled = true;
            this.muteOption = this.lastMuteOption;
            btn_muteOption.displayString = PlayerMusicDefImpl.EnumMuteOptions.byMetadata(this.lastMuteOption).toString();
            this.lastMuteOption = -1;
        }   
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        /** capture the ESC key do we close cleanly */
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            actionPerformed((GuiButton) buttonList.get(btn_cancel.id));
        }
        super.keyTyped(typedChar, keyCode);
    }

    @SuppressWarnings("unused")
    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        
        if (btn_midiVolume.isMouseOver()) {
            updateGuiElments();
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public Minecraft getMinecraftInstance() {return mc;}

    public FontRenderer getFontRenderer() {return mc.fontRendererObj;}
    
    protected void sendOptionsToServer(float midiVolume, int muteOption)
    {
        PacketDispatcher.sendToServer(new MusicOptionsMessage(midiVolume, muteOption));
    }
    
    /** MIDI generator for testing volume */
    private Synthesizer synth = null;
    MidiChannel[]   channels;
    MidiChannel channel;
    
    private void midiInit()
    {
        try
        {
            synth = MidiSystem.getSynthesizer();
        }
        catch (MidiUnavailableException e)
        {
            e.printStackTrace();
        }

        try
        {
            synth.open();
        }
        catch (MidiUnavailableException e)
        {
            e.printStackTrace();
        }
        channels = synth.getChannels();
        channel = channels[0];
        channel.programChange(10);
        setWait(10);
    }
    
    private void midiUpdate()
    {
        flipFlop(btn_midiVolume.isMouseOver());
        nextNote();
    }
    
    private void midiClose()
    {
        synth.close();
    }

    private int tickLen = 1; // Set in the initialize method
    private int noteMidi;
    private long tick;
    private long tock; // 1 ms
    private boolean waiting;
    private int noteActive = 0;
    private boolean noteOff = true;
    private double cyc = 0D;
    
    public void nextTick()
    {
        if (waiting)
        {
            //ModLogger.logInfo("tick:" + tick);
            if ((++tick % tickLen) == 0)
            {
                if (tock-- <= 0)
                {
                    waiting = false;
                    //ModLogger.logInfo("Tock");
                }
            }
        }
    }

    public void setWait(int ms)
    {
        if (ms <= 0) return;
        //ModLogger.logInfo("setWait = " + ms + ", tickLen = " + tickLen + ", tick: " + tick + ",  tock: "+ tock);
        tock = ms;
        tick = 0;
        waiting = true;
    }

    public void flipFlop(boolean hasHoover)
    {
        if (hasHoover)
        {
            noteActive = 1;
        } else 
        {
            noteActive = 0;
        }
    }
    
    public void nextNote()
    {
        nextCmd();
        cyc =  cyc + 0.16D;
    }


    public void nextCmd()
    {
        nextTick();
        // Test if WAIT(x)ING: dec ticks, tocks at this point.
        // NOT_WAITING:
        // Parse commands and execute commands
        // if WAIT command
        // set/trigger WAIT(x)ING
        // else
        // WAIT(x)ING: X milliseconds: return immediately
        if (waiting) return;   
        
        if (noteOff) {
            noteMidi = (int) Math.round(((((Math.sin(cyc)+1.D)/2D) *40.0D)+40.0D));
            channel.noteOn(noteMidi, scaleVolume(127*noteActive));

            setWait(2);
            noteOff=false;
        }
        if (!noteOff) {
            channel.noteOff(noteMidi);
            setWait(1);
            noteOff = true;
        }
    }
    
    private int scaleVolume(int volumeIn)
    {
        int temp = (int) Math.round((float)volumeIn * (Math.exp(this.midiVolume)-1)/(Math.E-1));
        //ModLogger.logInfo("volumeIn: " + volumeIn + ", midiVolume: " + midiVolume + ", scaled: " + temp);
        return temp;
    }

}
