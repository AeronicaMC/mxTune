package net.aeronica.mods.mxtune.blocks;

import net.aeronica.libs.mml.util.TestData;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.Random;

@SuppressWarnings("deprecation")
public class MusicBlock extends Block
{
    private static final Random rand = new Random();

    public MusicBlock()
    {
        super(Properties.of(Material.METAL)
             .sound(SoundType.METAL)
             .strength(2.0F));
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if (!worldIn.isClientSide)
        {
            // worldIn.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL, SoundCategory.BLOCKS, 1F, 2F);
        } else
        {
            ClientAudio.play(PlayIdSupplier.PlayType.PLAYERS.getAsInt(), pos, TestData.getMML(rand.nextInt(TestData.values().length)).getMML());
        }
        return ActionResultType.SUCCESS;
    }
}
