package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.caps.venues.EntityVenueState;
import aeronicamc.mods.mxtune.caps.venues.MusicVenueHelper;
import aeronicamc.mods.mxtune.caps.venues.ToolManager;
import aeronicamc.mods.mxtune.caps.venues.ToolState;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.items.MusicVenueToolItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import static aeronicamc.mods.mxtune.render.RenderHelper.*;

public class VenueToolOverlay implements IOverlayItem {
    private static final String NAME = new TranslationTextComponent("gui.mxtune.overlay.venue_tool.name").getString();
    private final ItemStack itemStack;
    private long lastChanged;
    private boolean changed;
    private int totalWidth;
    private final int lastSlot;
    private final boolean managedPosition;

    public VenueToolOverlay(ItemStack itemStack) {
        this.lastSlot = RenderHelper.getSelectedSlot();
        this.itemStack = itemStack;
        this.totalWidth = this.baseWidth();
        this.managedPosition = false;
    }

    public VenueToolOverlay() {
        this.lastSlot = -1;
        this.itemStack = new ItemStack(ModItems.MUSIC_VENUE_TOOL.get(), 1);
        this.totalWidth = this.baseWidth();
        this.managedPosition = true;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean isManagedPosition() {
        return this.managedPosition;
    }

    @Override
    public int totalHeight() {
        return this.baseHeight();
    }

    @Override
    public int totalWidth() {
        return totalWidth;
    }

    private boolean isNotToolItem() {
        return !(getPlayer().inventory.getSelected().getItem() instanceof MusicVenueToolItem);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Visibility render(MatrixStack pPoseStack, long delta) {
        if (this.changed) {
            this.lastChanged = delta;
            this.changed = false;
        }
        final RayTraceResult raytraceresult = mc.hitResult;
        final Vector3d vector3d;
        final EntityVenueState evs = MusicVenueHelper.getEntityVenueState(getPlayer().level, getPlayer().getId());
        final ITextComponent blockName;
        BlockPos blockpos = BlockPos.ZERO;

        if (raytraceresult instanceof BlockRayTraceResult)
        {
            blockpos = ((BlockRayTraceResult) raytraceresult).getBlockPos();
        }
        else if (raytraceresult instanceof EntityRayTraceResult)
        {
            vector3d = ((EntityRayTraceResult) raytraceresult).getEntity().getPosition(mc.getFrameTime());
            blockpos = new BlockPos(vector3d.x, vector3d.y, vector3d.z);
        }

        ToolState.Type[] stateName = {ToolState.Type.START};
        ToolManager.getToolOpl(getPlayer()).ifPresent(tool-> stateName[0] = tool.getToolState());

        ITextComponent testText;
        if (managedPosition)
            testText = new StringTextComponent(NAME).withStyle(TextFormatting.WHITE);
        else
            testText = new TranslationTextComponent(stateName[0].getTranslationKey()).withStyle(TextFormatting.WHITE).append(" ").append(evs.inVenue() ? evs.getVenue().getVenueAABB().getCenter().toString() : "");
        totalWidth = Math.max(mc.font.width(testText) + 40, this.baseWidth());

        if (mc.level != null && raytraceresult instanceof BlockRayTraceResult && !managedPosition)
            blockName = mc.level.getBlockState(blockpos).getBlock().getName().withStyle(TextFormatting.YELLOW);
        else if (raytraceresult instanceof EntityRayTraceResult && !managedPosition)
            blockName = new StringTextComponent(((EntityRayTraceResult) raytraceresult).getEntity().getName().getString()).withStyle(TextFormatting.YELLOW);
        else
            blockName = new StringTextComponent("---").withStyle(TextFormatting.AQUA);

        mc.getTextureManager().bind(IOverlayItem.TEXTURE);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        RenderHelper.blit(pPoseStack, 0, 0, 0, 0, this.baseWidth(), this.baseHeight());
        RenderHelper.blit(pPoseStack, ((totalWidth - this.baseWidth())/2) + 5, 0, 10, 0, this.baseWidth() -10, this.baseHeight());
        RenderHelper.blit(pPoseStack, totalWidth - this.baseWidth() + 10, 0, 10, 0, this.baseWidth(), this.baseHeight());

        mc.font.draw(pPoseStack, testText, 30.0F, 7.0F, -11534256);
        mc.font.draw(pPoseStack, blockName, 30.0F, 17.0F, -11534256);
        mc.getItemRenderer().renderAndDecorateItem(itemStack, 8, 8);

        if (managedPosition)
            return delta - this.lastChanged >= 900L ? Visibility.HIDE : Visibility.SHOW;
        else
            return lastSlot != getSelectedSlot() || isNotToolItem() ? Visibility.HIDE : Visibility.SHOW;
    }
}
