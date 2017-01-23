package net.aeronica.mods.mxtune.tabula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import net.aeronica.mods.mxtune.tabula.components.TabulaAnimation;
import net.aeronica.mods.mxtune.tabula.components.TabulaAnimationComponent;
import net.aeronica.mods.mxtune.tabula.components.TabulaCubeContainer;
import net.aeronica.mods.mxtune.tabula.components.TabulaModelContainer;

public class AnimationUtils
{

    private TabulaModelContainer tabulaModelData;
    
    public AnimationUtils(TabulaModelContainer tabulaModelData) {this.tabulaModelData = tabulaModelData;}
    
    /**
     * Plays the named animation
     * @param name of the animation
     */
    public void play(String name)
    {
        if (hasAnims(tabulaModelData))
        {
            tabulaModelData.getAnims()
            .stream()
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .forEach(p -> p.play());
        }
    }
    /**
     * Stops the named animation
     * @param name of the animation
     */
    public void stop(String name)
    {
        if (hasAnims(tabulaModelData))
        {
            tabulaModelData.getAnims()
            .stream()
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .forEach(p -> p.stop());
        }        
    }

    /**
     * Resumes a stopped animation
     * @param name of the the animation
     */
    public void resume(String name)
    {
        if (hasAnims(tabulaModelData))
        {
            tabulaModelData.getAnims()
            .stream()
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .forEach(p -> p.resume());
        }        
    }
   
    /**
     * Stops all playing animations except loops
     */
    public void stopAll()
    {
        if (hasAnims(tabulaModelData))
        {
            tabulaModelData.getAnims()
            .stream()
            .forEach(p -> p.stop());
        }                
    }
    
    /**
     * Sets the looping state for the named animation
     * @param name of the animation
     * @param loop true or false
     */
    public void setLoop(String name, boolean loop)
    {
        if (hasAnims(tabulaModelData))
        {
            tabulaModelData.getAnims()
            .stream()
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .forEach(p -> p.setLoop(loop));
        }
    }

    public boolean isPlaying(String name)
    {
        boolean result = false;
        if (hasAnims(tabulaModelData))
        {
            for(TabulaAnimation anims: tabulaModelData.getAnims())
            {
                if (anims.getName().equalsIgnoreCase(name))
                {
                    result =  anims.isPlaying();
                }
            }
        }
        return result;
    }
    
    
    /**
     * Stops all playing animations and loops
     */
    public void halt()
    {
        if (hasAnims(tabulaModelData))
        {
            tabulaModelData.getAnims()
            .stream()
            .forEach(p -> p.setLoop(false));
            tabulaModelData.getAnims()
            .stream()
            .forEach(p -> p.stop());
        }        
    }
 
    public void update()
    {
        if (hasAnims(tabulaModelData))
        {
            tabulaModelData.getAnims()
            .stream()
            .forEach(p -> p.update());
        }        
    }
    
    public void reset()
    {
        if (hasAnims(tabulaModelData)) resetModelAnimations(tabulaModelData, 0F);
    }
    
    public static void applyModelAnimations(TabulaModelContainer modelData, float renderTick)
    {
        for(TabulaAnimation anim : modelData.getAnims())
        {
            for(Map.Entry<String, ArrayList<TabulaAnimationComponent>> e : anim.getSets().entrySet())
            {
                for(TabulaCubeContainer cube : modelData.getCubes())
                {
                    if(cube.getIdentifier().equals(e.getKey()))
                    {
                        ArrayList<TabulaAnimationComponent> components = e.getValue();
                        Collections.sort(components);

                        for(TabulaAnimationComponent comp : components)
                        {
                            if(!comp.isHidden())
                            {
                                comp.animate(cube, anim.getPlayTime() + (anim.isPlaying() ? renderTick : 0));
                            }
                        }
                    }
                }
            }
        }
    }

    public static void resetModelAnimations(TabulaModelContainer modelData, float renderTick)
    {
        for(TabulaAnimation anim : modelData.getAnims())
        {
            for(Map.Entry<String, ArrayList<TabulaAnimationComponent>> e : anim.getSets().entrySet())
            {
                for(TabulaCubeContainer cube : modelData.getCubes())
                {
                    if(cube.getIdentifier().equals(e.getKey()))
                    {
                        ArrayList<TabulaAnimationComponent> components = e.getValue();
                        Collections.sort(components);

                        for(TabulaAnimationComponent comp : components)
                        {
                            if(!comp.isHidden())
                            {
                                comp.reset(cube, anim.getPlayTime() + (anim.isPlaying() ? renderTick : 0));
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean hasAnims(TabulaModelContainer tabulaModelData) {return tabulaModelData != null && tabulaModelData.getAnims() != null && !tabulaModelData.getAnims().isEmpty();}

}
