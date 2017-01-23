package net.aeronica.mods.mxtune.tabula.components;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TabulaCubeContainer
{

    private String name;
    private ArrayList<String> metadata = new ArrayList<String>();
    private String parentIdentifier;
    private String identifier;
    
    public int[] dimensions = new int[3];
    public double[] position = new double[3];
    public double[] offset = new double[3];
    public double[] rotation = new double[3];
    public double[] scale = new double[3];

    public int[] txOffset = new int[2];
    public boolean txMirror = false;

    public double mcScale = 0.0D;
    public double opacity = 100D;
    public boolean hidden = false;

    private List<TabulaCubeContainer> children = new ArrayList<TabulaCubeContainer>();

    @SideOnly(Side.CLIENT)
    private ModelRenderer modelCube;

    public TabulaCubeContainer(String name, int[] dimensions, double[] position, double[] offset, double[] rotation, double[] scale, int[] txOffset, boolean txMirror, double mcScale, double opacity,
            boolean hidden, ArrayList<String> metadata, String parentIdentifier, String identifier)
    {
        this.name = name;
        this.dimensions = dimensions;
        this.position = position;
        this.offset = offset;
        this.rotation = rotation;
        this.scale = scale;
        this.txOffset = txOffset;
        this.txMirror = txMirror;
        this.mcScale = mcScale;
        this.opacity = opacity;
        this.hidden = hidden;
        this.metadata = metadata;
        this.parentIdentifier = parentIdentifier;
        this.identifier = identifier;
    }

//    public void addChild(TabulaCubusCustos info)
//    {
//        children.add(info);
//        info.scale = new double[] { 1D, 1D, 1D };
//        info.mcScale = 0.0D;
//        info.opacity = opacity;
//        info.parentIdentifier = identifier;
//        info.hidden = false;
//    }
//
//    public void removeChild(TabulaCubusCustos info)
//    {
//        children.remove(info);
//        if(info.parentIdentifier != null && info.parentIdentifier.equals(identifier))
//        {
//            info.parentIdentifier = null;
//        }
//    }

    public TabulaCubeContainer createModel(ModelBase base)
    {
        this.modelCube = new ModelRenderer(base, this.txOffset[0], this.txOffset[1]);
        this.modelCube.mirror = this.txMirror;
        this.modelCube.setRotationPoint((float)this.position[0], (float)this.position[1], (float)this.position[2]);
        this.modelCube.addBox((float)this.offset[0], (float)this.offset[1], (float)this.offset[2], this.dimensions[0], this.dimensions[1], this.dimensions[2], (float)this.mcScale);
        this.modelCube.rotateAngleX = (float)Math.toRadians(this.rotation[0]);
        this.modelCube.rotateAngleY = (float)Math.toRadians(this.rotation[1]);
        this.modelCube.rotateAngleZ = (float)Math.toRadians(this.rotation[2]);

        createChildren(base);

        return this;
    }

    private void createChildren(ModelBase base)
    {
        for(TabulaCubeContainer child : getChildren())
        {
            child.modelCube = new ModelRenderer(base, child.txOffset[0], child.txOffset[1]);
            child.modelCube.mirror = child.txMirror;
            child.modelCube.addBox((float)child.offset[0], (float)child.offset[1], (float)child.offset[2], child.dimensions[0], child.dimensions[1], child.dimensions[2]);
            child.modelCube.setRotationPoint((float)child.position[0], (float)child.position[1], (float)child.position[2]);
            child.modelCube.rotateAngleX = (float)Math.toRadians(child.rotation[0]);
            child.modelCube.rotateAngleY = (float)Math.toRadians(child.rotation[1]);
            child.modelCube.rotateAngleZ = (float)Math.toRadians(child.rotation[2]);

            this.modelCube.addChild(child.modelCube);

            child.createChildren(base);
        }
    }

    public List<TabulaCubeContainer> getChildren()
    {
        return children;
    }

    public String getParentIdentifier()
    {
        return parentIdentifier;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public ModelRenderer getModelCube()
    {
        return modelCube;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public ArrayList<String> getMetadata()
    {
        return metadata;
    }

    public String getName()
    {
        return name;
    }

    public int[] getDimensions()
    {
        return dimensions;
    }

    public double[] getPosition()
    {
        return position;
    }

    public double[] getOffset()
    {
        return offset;
    }

    public double[] getRotation()
    {
        return rotation;
    }

    public double[] getScale()
    {
        return scale;
    }

    public int[] getTxOffset()
    {
        return txOffset;
    }

    public boolean isTxMirror()
    {
        return txMirror;
    }

    public double getMCScale()
    {
        return mcScale;
    }

    public double getOpacity()
    {
        return opacity;
    }
    
}
