package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.items.ItemMultiInst;
import aeronicamc.mods.mxtune.util.Misc;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Supplier;

public class ChooseInstrumentMessage extends AbstractMessage<ChooseInstrumentMessage>
{
    int index;

    public ChooseInstrumentMessage()
    {
        this.index = 0;
    }

    public ChooseInstrumentMessage(int index)
    {
        this.index = index;
    }

    @Override
    public void encode(ChooseInstrumentMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.index);
    }

    @Override
    public ChooseInstrumentMessage decode(PacketBuffer buffer)
    {
        final int index = buffer.readInt();
        return new ChooseInstrumentMessage(index);
    }

    @Override
    public void handle(ChooseInstrumentMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{

                ServerPlayerEntity sPlayer = ctx.get().getSender();
                assert sPlayer != null;
                if (!sPlayer.getMainHandItem().isEmpty() && sPlayer.getMainHandItem().getItem() instanceof ItemMultiInst)
                {
                    int index = Misc.clamp(0, SoundFontProxyManager.soundFontProxyMapByIndex.size(), message.index);
                    ItemStack selected = sPlayer.getMainHandItem();
                    ItemStack newInst = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(Reference.MOD_ID, SoundFontProxyManager.getName(index)))).getDefaultInstance().copy();
                    CompoundNBT transfer = selected.serializeNBT();
                    newInst.deserializeNBT(transfer);
                    sPlayer.setItemInHand(Hand.MAIN_HAND, newInst);
                }
            });
        ctx.get().setPacketHandled(true);
    }
}
