package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.caps.venues.MusicVenueProvider;
import aeronicamc.mods.mxtune.caps.venues.ToolManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;

import java.util.Optional;

public class MusicVenueToolItem extends Item
{
    private final ToolManager toolManager = new ToolManager();
    public MusicVenueToolItem(Properties properties)
    {
        super(properties);
    }

    /**
     * This is called when the item is used, before the block is activated.
     * @return Return PASS to allow vanilla handling, any other to skip normal code.
     */
    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
    {
        if (!context.getHand().equals(Hand.MAIN_HAND))
            return super.onItemUseFirst(stack, context);

        getPlayer(context).filter(p -> !p.level.isClientSide()).ifPresent(player -> {
            MusicVenueProvider.getMusicVenues(context.getLevel()).ifPresent(mvp -> {
            if (!player.isShiftKeyDown())
                toolManager.setPosition(player, context);
            else
                toolManager.reset(player);
            });
        });

        return ActionResultType.SUCCESS;
    }

    // player optional wrapper so we can use the syntactic sugar
    private Optional<PlayerEntity> getPlayer(ItemUseContext context)
    {
        return Optional.ofNullable(context.getPlayer());
    }

    public ToolManager getToolManager()
    {
        return toolManager;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        return super.useOn(context);
    }
}