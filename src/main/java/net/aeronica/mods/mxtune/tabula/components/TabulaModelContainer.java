package net.aeronica.mods.mxtune.tabula.components;

import java.util.ArrayList;

public class TabulaModelContainer
{

    private String modelName;
    private String authorName;
    private ArrayList<String> metadata;
    
    private ArrayList<TabulaCubeContainer> cubes;
    private ArrayList<TabulaCubeGroupContainer> cubeGroups;
    private ArrayList<TabulaAnimation> anims;
    
    private double[] scale = new double[] { 1D, 1D, 1D };
    private int textureWidth;
    private int textureHeight;
    
    private ArrayList<String> states;
    private int switchState = -1;
    
    private int cubeCount;
    private int projVersion;

    public TabulaModelContainer(String modelName, String authorName, ArrayList<String> metadata, ArrayList<TabulaCubeContainer> cubes, ArrayList<TabulaCubeGroupContainer> cubeGroups, ArrayList<TabulaAnimation> anims,
            int textureWidth, int textureHeight, ArrayList<String> states, int switchState, int cubeCount, int projVersion)
    {
        this.modelName = modelName;
        this.authorName = authorName;
        this.metadata = metadata;
        this.cubes = cubes;
        this.cubeGroups = cubeGroups;
        this.anims = anims;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.states = states;
        this.switchState = switchState;
        this.cubeCount = cubeCount;
        this.projVersion = projVersion;
    }

    public TabulaModelContainer() {}
    
    public String getModelName()
    {
        return modelName;
    }

    public String getAuthorName()
    {
        return authorName;
    }

    public ArrayList<String> getMetadata()
    {
        return metadata;
    }

    public ArrayList<TabulaCubeContainer> getCubes()
    {
        return cubes;
    }

    public ArrayList<TabulaCubeGroupContainer> getCubeGroups()
    {
        return cubeGroups;
    }

    public ArrayList<TabulaAnimation> getAnims()
    {
        return anims;
    }

    public ArrayList<String> getStates()
    {
        return states;
    }

    public int getSwitchState()
    {
        return switchState;
    }

    public int getCubeCount()
    {
        return cubeCount;
    }

    public int getProjVersion()
    {
        return projVersion;
    }

    public int getTextureWidth()
    {
        return textureWidth;
    }

    public int getTextureHeight()
    {
        return textureHeight;
    }

    public double[] getScale()
    {
        return scale;
    }
    
}
