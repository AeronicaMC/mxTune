package net.aeronica.mods.mxtune.tabula.components;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Ordering;

public class TabulaAnimation
{

    private String name;
    private String identifier;

    private boolean loops;
    
    // cube identifier to animation component
    private TreeMap<String, ArrayList<TabulaAnimationComponent>> sets = new TreeMap<String, ArrayList<TabulaAnimationComponent>>(Ordering.natural());
    
    private transient int playTime;
    private transient boolean playing = false;
    public int getPlayTime()
    {
        return playTime;
    }
    
    public boolean isPlaying()
    {
        return playing;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getIdentifier()
    {
        return identifier;
    }
    
    public boolean isLoops()
    {
        return loops;
    }
    
    public void setLoop(boolean loop)
    {
        loops = loop;
    }
    
    public TreeMap<String, ArrayList<TabulaAnimationComponent>> getSets()
    {
        return sets;
    }
    
    public void update()
    {
        if(playing)
        {
            playTime++;
            if(playTime > getLength())
            {
                if(loops)
                {
                    playTime = 0;
                }
                else
                {
                    stop();
                }
            }
        }
    }

    public void play()
    {
        if(!playing)
        {
            playing = true;
            playTime = 0;
        }
    }

    public void stop()
    {
        playing = false;
    }

    public void resume()
    {
        playing = true;
    }
    
    public int getLength()
    {
        int lastTick = 0;
        for(Map.Entry<String, ArrayList<TabulaAnimationComponent>> e : sets.entrySet())
        {
            for(TabulaAnimationComponent comp : e.getValue())
            {
                if(comp.getStartKey() + comp.getLength() > lastTick)
                {
                    lastTick = comp.getStartKey() + comp.getLength();
                }
            }
        }
        return lastTick;
    }
}
