package net.aeronica.mods.mxtune.sound;

import java.util.concurrent.Callable;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayStatusCapabillity {

    public static void register() {
        CapabilityManager.INSTANCE.register(IPlayStatus.class, new Storage(), new Factory());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static class EventHandler
    {

        @SubscribeEvent
        public void onEntityConstruct(AttachCapabilitiesEvent.Entity event)
        {
            if (event.getEntity() instanceof EntityPlayer)
            {
                event.addCapability(new ResourceLocation(MXTuneMain.MODID, "IPlayStatus"), new ICapabilitySerializable<NBTPrimitive>()
                {
                    IPlayStatus inst = PlayStatusUtil.PLAY_STATUS.getDefaultInstance();

                    @Override
                    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                    {
                        return capability == PlayStatusUtil.PLAY_STATUS;
                    }

                    @Override
                    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                    {
                        return capability == PlayStatusUtil.PLAY_STATUS ? PlayStatusUtil.PLAY_STATUS.<T> cast(inst) : null;
                    }

                    @Override
                    public NBTPrimitive serializeNBT()
                    {
                        return (NBTPrimitive) PlayStatusUtil.PLAY_STATUS.getStorage().writeNBT(PlayStatusUtil.PLAY_STATUS, inst, null);
                    }

                    @Override
                    public void deserializeNBT(NBTPrimitive nbt)
                    {
                        PlayStatusUtil.PLAY_STATUS.getStorage().readNBT(PlayStatusUtil.PLAY_STATUS, inst, null, nbt);
                    }

                });
            }
        }

        @SubscribeEvent
        public void OnPlayerClone(PlayerEvent.Clone evt) {
            IPlayStatus dead = evt.getOriginal().getCapability(PlayStatusUtil.PLAY_STATUS, null);
            IPlayStatus live = evt.getEntityPlayer().getCapability(PlayStatusUtil.PLAY_STATUS, null);
            live.setPlaying(evt.getEntityPlayer(), false);
        }

    }

    private static class Factory implements Callable<IPlayStatus> {

        @Override
        public IPlayStatus call() throws Exception {
            return new PlayStatusImpl();
        }
    }

    private static class Storage implements Capability.IStorage<IPlayStatus> {

        @Override
        public NBTBase writeNBT(Capability<IPlayStatus> capability, IPlayStatus instance, EnumFacing side) {
            return new NBTTagByte((byte)0);
        }

        @Override
        public void readNBT(Capability<IPlayStatus> capability, IPlayStatus instance, EnumFacing side, NBTBase nbt) {
            ((NBTPrimitive)nbt).getByte();
        }
    }
    
}