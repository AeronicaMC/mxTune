package net.aeronica.mods.mxtune.tabula.components;

import java.util.ArrayList;

import net.aeronica.mods.mxtune.tabula.math.PolynomialFunctionLagrangeForm;
import net.minecraft.util.math.MathHelper;

public class TabulaAnimationComponent implements Comparable<Object>
{

    private double[] posChange = new double[3];
    private double[] rotChange = new double[3];
    private double[] scaleChange = new double[3];
    private double opacityChange = 0.0D;

    private double[] posOffset = new double[3];
    private double[] rotOffset = new double[3];
    private double[] scaleOffset = new double[3];
    private double opacityOffset = 0.0D;

    private ArrayList<double[]> progressionCoords;

    private PolynomialFunctionLagrangeForm progressionCurve;

    private String name;

    private int length;
    private int startKey;

    private boolean hidden;

    private String identifier;

    public double[] getPosChange()
    {
        return posChange;
    }

    public double[] getRotChange()
    {
        return rotChange;
    }

    public double[] getScaleChange()
    {
        return scaleChange;
    }

    public double getOpacityChange()
    {
        return opacityChange;
    }

    public double[] getPosOffset()
    {
        return posOffset;
    }

    public double[] getRotOffset()
    {
        return rotOffset;
    }

    public double[] getScaleOffset()
    {
        return scaleOffset;
    }

    public double getOpacityOffset()
    {
        return opacityOffset;
    }

    public ArrayList<double[]> getProgressionCoords()
    {
        return progressionCoords;
    }

    public PolynomialFunctionLagrangeForm getProgressionCurve()
    {
        return progressionCurve;
    }

    public String getName()
    {
        return name;
    }

    public int getLength()
    {
        return length;
    }

    public int getStartKey()
    {
        return startKey;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void animate(TabulaCubeContainer cubeInfo, float time)
    {
        float prog = MathHelper.clamp_float((time - startKey) / (float)length, 0F, 1F);
        float mag = prog;
        if(getProgressionCurve() != null)
        {
            mag = MathHelper.clamp_float((float)getProgressionCurve().value(prog), 0.0F, 1.0F);
        }
        if(time >= startKey)
        {
            for(int i = 0; i < 3; i++)
            {
                cubeInfo.position[i] += posOffset[i];
                cubeInfo.rotation[i] += rotOffset[i];
                cubeInfo.scale[i] += scaleOffset[i];
            }
            cubeInfo.opacity += opacityOffset;
        }
        for(int i = 0; i < 3; i++)
        {
            cubeInfo.position[i] += posChange[i] * mag;
            cubeInfo.rotation[i] += rotChange[i] * mag;
            cubeInfo.scale[i] += scaleChange[i] * mag;
        }
        cubeInfo.opacity += opacityChange * mag;

        if(cubeInfo.getModelCube() != null)
        {
            cubeInfo.getModelCube().setRotationPoint((float)cubeInfo.position[0], (float)cubeInfo.position[1], (float)cubeInfo.position[2]);
            cubeInfo.getModelCube().rotateAngleX = (float)Math.toRadians(cubeInfo.rotation[0]);
            cubeInfo.getModelCube().rotateAngleY = (float)Math.toRadians(cubeInfo.rotation[1]);
            cubeInfo.getModelCube().rotateAngleZ = (float)Math.toRadians(cubeInfo.rotation[2]);
        }
    }

    public void reset(TabulaCubeContainer cubeInfo, float time)
    {
        float prog = MathHelper.clamp_float((time - startKey) / (float)length, 0F, 1F);
        float mag = prog;
        if(getProgressionCurve() != null)
        {
            mag = MathHelper.clamp_float((float)getProgressionCurve().value(prog), 0.0F, 1.0F);
        }
        if(time >= startKey)
        {
            for(int i = 0; i < 3; i++)
            {
                cubeInfo.position[i] -= posOffset[i];
                cubeInfo.rotation[i] -= rotOffset[i];
                cubeInfo.scale[i] -= scaleOffset[i];
            }
            cubeInfo.opacity -= opacityOffset;
        }
        for(int i = 0; i < 3; i++)
        {
            cubeInfo.position[i] -= posChange[i] * mag;
            cubeInfo.rotation[i] -= rotChange[i] * mag;
            cubeInfo.scale[i] -= scaleChange[i] * mag;
        }
        cubeInfo.opacity -= opacityChange * mag;

        if(cubeInfo.getModelCube() != null)
        {
            cubeInfo.getModelCube().setRotationPoint((float)cubeInfo.position[0], (float)cubeInfo.position[1], (float)cubeInfo.position[2]);
            cubeInfo.getModelCube().rotateAngleX = (float)Math.toRadians(cubeInfo.rotation[0]);
            cubeInfo.getModelCube().rotateAngleY = (float)Math.toRadians(cubeInfo.rotation[1]);
            cubeInfo.getModelCube().rotateAngleZ = (float)Math.toRadians(cubeInfo.rotation[2]);
        }
    }

    @Override
    public int compareTo(Object arg0)
    {
        if(arg0 instanceof TabulaAnimationComponent)
        {
            TabulaAnimationComponent comp = (TabulaAnimationComponent)arg0;
            return ((Integer)startKey).compareTo(comp.startKey);
        }
        return 0;
    }
}
