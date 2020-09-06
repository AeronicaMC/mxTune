package net.aeronica.mods.mxtune.model.entity;

import net.aeronica.mods.mxtune.entity.living.EntityTimpani;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

/**
 * mxmob_timpani - aeronica
 * Created using Tabula 5.1.0
 */
public class ModelTimpani extends ModelBase
{
    public ModelRenderer kettle;
    public ModelRenderer head;
    public ModelRenderer base_rear;
    public ModelRenderer head_clamp_f_r;
    public ModelRenderer head_clamp_f_l;
    public ModelRenderer head_clamp_r_f;
    public ModelRenderer head_clamp_r_r;
    public ModelRenderer head_clamp_l_f;
    public ModelRenderer head_clamp_l_r;
    public ModelRenderer head_clamp_b_r;
    public ModelRenderer head_clamp_b_l;
    public ModelRenderer base_right;
    public ModelRenderer base_left;
    public ModelRenderer base_post;
    public ModelRenderer base_pedal;
    public ModelRenderer base_wheel_rear;
    public ModelRenderer base_wheel_front_r;
    public ModelRenderer base_wheel_front_l;

    public ModelTimpani() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.base_wheel_rear = new ModelRenderer(this, 58, 0);
        this.base_wheel_rear.setRotationPoint(-6.0F, 2.0F, 0.0F);
        this.base_wheel_rear.addBox(-1.0F, -1.0F, -0.5F, 2, 2, 1, 0.0F);
        this.head_clamp_b_r = new ModelRenderer(this, 54, 16);
        this.head_clamp_b_r.setRotationPoint(-2.0F, 0.0F, 4.5F);
        this.head_clamp_b_r.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.setRotateAngle(head_clamp_b_r, 0.0F, 0.0F, 1.5707963267948966F);
        this.base_rear = new ModelRenderer(this, 37, 0);
        this.base_rear.setRotationPoint(0.0F, 8.0F, 0.0F);
        this.base_rear.addBox(-6.1F, 0.0F, -0.5F, 6, 1, 1, 0.0F);
        this.setRotateAngle(base_rear, 0.0F, 1.5707963267948966F, 0.0F);
        this.head_clamp_l_r = new ModelRenderer(this, 48, 18);
        this.head_clamp_l_r.setRotationPoint(5.5F, 0.0F, 2.0F);
        this.head_clamp_l_r.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.setRotateAngle(head_clamp_l_r, 0.0F, 0.0F, 1.5707963267948966F);
        this.head_clamp_f_l = new ModelRenderer(this, 42, 18);
        this.head_clamp_f_l.setRotationPoint(3.0F, 0.0F, -5.3F);
        this.head_clamp_f_l.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.setRotateAngle(head_clamp_f_l, 0.0F, 0.0F, 1.5707963267948966F);
        this.base_wheel_front_r = new ModelRenderer(this, 58, 3);
        this.base_wheel_front_r.setRotationPoint(2.5F, 2.0F, -5.2F);
        this.base_wheel_front_r.addBox(-1.0F, -1.0F, -0.5F, 2, 2, 1, 0.0F);
        this.base_pedal = new ModelRenderer(this, 37, 12);
        this.base_pedal.setRotationPoint(-5.5F, -0.5F, 0.0F);
        this.base_pedal.addBox(-1.0F, -0.5F, -1.0F, 4, 1, 2, 0.0F);
        this.setRotateAngle(base_pedal, 0.0F, 0.0F, -0.7853981633974483F);
        this.base_wheel_front_l = new ModelRenderer(this, 58, 6);
        this.base_wheel_front_l.setRotationPoint(2.5F, 2.0F, 5.2F);
        this.base_wheel_front_l.addBox(-1.0F, -1.0F, -0.5F, 2, 2, 1, 0.0F);
        this.head = new ModelRenderer(this, 0, 16);
        this.head.setRotationPoint(0.0F, -3.5F, 0.0F);
        this.head.addBox(-5.0F, -0.5F, -5.0F, 10, 1, 10, 0.0F);
        this.base_left = new ModelRenderer(this, 37, 4);
        this.base_left.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.base_left.addBox(0.5F, 0.0F, -0.5F, 6, 1, 1, 0.0F);
        this.setRotateAngle(base_left, 0.0F, -1.0471975511965976F, 0.0F);
        this.head_clamp_b_l = new ModelRenderer(this, 54, 18);
        this.head_clamp_b_l.setRotationPoint(3.0F, 0.0F, 4.5F);
        this.head_clamp_b_l.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.setRotateAngle(head_clamp_b_l, 0.0F, 0.0F, 1.5707963267948966F);
        this.kettle = new ModelRenderer(this, 0, 0);
        this.kettle.setRotationPoint(0.0F, 13.0F, 0.0F);
        this.kettle.addBox(-4.5F, -3.0F, -4.5F, 9, 7, 9, 0.0F);
        this.head_clamp_l_f = new ModelRenderer(this, 48, 16);
        this.head_clamp_l_f.setRotationPoint(5.5F, 0.0F, -3.0F);
        this.head_clamp_l_f.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.setRotateAngle(head_clamp_l_f, 0.0F, 0.0F, 1.5707963267948966F);
        this.head_clamp_f_r = new ModelRenderer(this, 42, 16);
        this.head_clamp_f_r.setRotationPoint(-2.0F, 0.0F, -5.5F);
        this.head_clamp_f_r.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.setRotateAngle(head_clamp_f_r, 0.0F, 0.0F, 1.5707963267948966F);
        this.head_clamp_r_f = new ModelRenderer(this, 36, 16);
        this.head_clamp_r_f.setRotationPoint(-4.5F, 0.0F, -3.0F);
        this.head_clamp_r_f.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.setRotateAngle(head_clamp_r_f, 0.0F, 0.0F, 1.5707963267948966F);
        this.head_clamp_r_r = new ModelRenderer(this, 36, 18);
        this.head_clamp_r_r.setRotationPoint(-4.5F, 0.0F, 2.0F);
        this.head_clamp_r_r.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.setRotateAngle(head_clamp_r_r, 0.0F, 0.0F, 1.5707963267948966F);
        this.base_right = new ModelRenderer(this, 37, 2);
        this.base_right.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.base_right.addBox(0.5F, 0.0F, -0.5F, 6, 1, 1, 0.0F);
        this.setRotateAngle(base_right, 0.0F, 1.0471975511965976F, 0.0F);
        this.base_post = new ModelRenderer(this, 37, 7);
        this.base_post.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.base_post.addBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, 0.0F);
        this.setRotateAngle(base_post, 0.0F, 0.0F, -1.5707963267948966F);
        this.base_rear.addChild(this.base_wheel_rear);
        this.head.addChild(this.head_clamp_b_r);
        this.kettle.addChild(this.base_rear);
        this.head.addChild(this.head_clamp_l_r);
        this.head.addChild(this.head_clamp_f_l);
        this.base_rear.addChild(this.base_wheel_front_r);
        this.base_rear.addChild(this.base_pedal);
        this.base_rear.addChild(this.base_wheel_front_l);
        this.kettle.addChild(this.head);
        this.base_rear.addChild(this.base_left);
        this.head.addChild(this.head_clamp_b_l);
        this.head.addChild(this.head_clamp_l_f);
        this.head.addChild(this.head_clamp_f_r);
        this.head.addChild(this.head_clamp_r_f);
        this.head.addChild(this.head_clamp_r_r);
        this.base_rear.addChild(this.base_right);
        this.base_rear.addChild(this.base_post);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.kettle.render(f5);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
    
    /**
     * Used for easily adding entity-dependent animations. The second and third float params here are the same second
     * and third as in the setRotationAngles method.
     */
    @Override
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime)
    {
        EntityTimpani entityTimpani = (EntityTimpani)entitylivingbaseIn;
        float f = entityTimpani.prevSquishFactor + (entityTimpani.squishFactor - entityTimpani.prevSquishFactor) * partialTickTime;
        head.offsetY =  -f * 0.25F;
        base_rear.offsetY = f * 0.5F;
        base_pedal.rotateAngleZ = -0.7853981633974483F - f * 2F;
        super.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTickTime);
    }
}
