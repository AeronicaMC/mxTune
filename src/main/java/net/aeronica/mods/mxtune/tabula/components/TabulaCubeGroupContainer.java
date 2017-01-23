package net.aeronica.mods.mxtune.tabula.components;

import java.util.ArrayList;

public class TabulaCubeGroupContainer
{

    private ArrayList<TabulaCubeContainer> cubes = new ArrayList<TabulaCubeContainer>();
    private ArrayList<TabulaCubeGroupContainer> cubeGroups = new ArrayList<TabulaCubeGroupContainer>();

    private String name;

    private boolean txMirror = false;

    private boolean hidden = false;

    private ArrayList<String> metadata = new ArrayList<String>();

    private String identifier;

    public TabulaCubeGroupContainer(ArrayList<TabulaCubeContainer> cubes, ArrayList<TabulaCubeGroupContainer> cubeGroups, String name, boolean txMirror, boolean hidden, ArrayList<String> metadata, String identifier)
    {
        super();
        this.cubes = cubes;
        this.cubeGroups = cubeGroups;
        this.name = name;
        this.txMirror = txMirror;
        this.hidden = hidden;
        this.metadata = metadata;
        this.identifier = identifier;
    }

    public ArrayList<TabulaCubeContainer> getCubes()
    {
        return cubes;
    }

    public ArrayList<TabulaCubeGroupContainer> getCubeGroups()
    {
        return cubeGroups;
    }

    public String getName()
    {
        return name;
    }

    public boolean isTxMirror()
    {
        return txMirror;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public ArrayList<String> getMetadata()
    {
        return metadata;
    }

    public String getIdentifier()
    {
        return identifier;
    }

}
