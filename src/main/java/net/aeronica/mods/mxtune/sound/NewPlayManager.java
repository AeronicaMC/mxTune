package net.aeronica.mods.mxtune.sound;

import net.aeronica.mods.mxtune.blocks.BlockPiano;
import net.aeronica.mods.mxtune.items.BasicItem;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.PlaySoloMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * @author Paul
 *
 * Simplified Play Manager for mod189stuff 
 */
public class NewPlayManager
{
    
    private NewPlayManager() {}

    /**
     * PlayManager.playMusic(EntityPLayer playerIn, BlockPos pos, boolean isPlaced)
     * 
     * This is intended to be used on Side.SERVER only
     * 
     * @param playerIn  the players entity
     * @param pos       position of block instrument
     * @param isPlaced  true is this is a block instrument
     */
    public static void playMusic(EntityPlayer playerIn, BlockPos pos, boolean isPlaced)
    {
        if (MusicOptionsUtil.isMuteAll(playerIn)) return;
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(pos, playerIn, isPlaced);
        if (sheetMusic != null)
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("MusicBook");
            if (contents != null)
            {
                String playerName = playerIn.getDisplayName().getUnformattedText();
                String title = sheetMusic.getDisplayName();
                String mml = contents.getString("MML");
                // String mml = TestData.MML6.getMML();
                mml = mml.replace("MML@", "MML@I" + getPatch(pos, playerIn, isPlaced));
                ModLogger.debug("MML Title: " + title);
                ModLogger.debug("MML Sub25: " + mml.substring(0, (mml.length() >= 25 ? 25 : mml.length())));

                    /** Solo Play */
                    NewPlayManager.playSolo(playerIn, title, mml, playerName, isPlaced);
                    ModLogger.debug("playMusic playSolo");
            }
        }
    }

    private static void playSolo(EntityPlayer playerIn, String title, String mml, String playerName, boolean isPlaced)
    {
        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playerIn.getEntityId(), playerName, title, mml, isPlaced);
        PacketDispatcher.sendToAllAround(packetPlaySolo, playerIn.dimension, playerIn.posX, playerIn.posY, playerIn.posZ, 25);  
    }
    
    
    private static int getPatch(BlockPos pos, EntityPlayer playerIn, boolean isPlaced)
    {
        if (isPlaced)
        {
            if (playerIn.worldObj.getBlockState(pos).getBlock() instanceof BlockPiano)
            {
                BlockPiano piano = (BlockPiano) playerIn.worldObj.getBlockState(pos).getBlock();
                return piano.getPatch();
            }
        } else if (playerIn.getHeldItemMainhand().getItem() instanceof ItemInstrument)
        {
            ItemInstrument.EnumInstruments enumInst = ItemInstrument.EnumInstruments.byMetadata(playerIn.getHeldItemMainhand().getMetadata());
            return enumInst.getPatch();
        } else if (playerIn.getHeldItemMainhand().getItem() instanceof BasicItem)
        {
            String patch = MusicOptionsUtil.getSParam1(playerIn);
            return Integer.valueOf(patch);
        }
        return 0;
    }

}
