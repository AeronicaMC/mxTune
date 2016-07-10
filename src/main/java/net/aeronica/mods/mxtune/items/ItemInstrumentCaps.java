/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.items;

import java.util.List;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.gui.GuiInstrumentInventory;
import net.aeronica.mods.mxtune.handler.IKeyListener;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.PlaySoloMessage;
import net.aeronica.mods.mxtune.network.bidirectional.QueueJamMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ItemInstrumentCaps extends ItemBase implements IInstrument, IKeyListener
{
    public ItemInstrumentCaps(String itemName)
    {
        super(itemName);
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
        setCreativeTab(MXTuneMain.TAB_MUSIC);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int metadata = stack.getMetadata();
        EnumInstruments inst = EnumInstruments.byMetadata(metadata);
        return super.getUnlocalizedName() + "_" + inst.getName();
    }

    @Override
    public boolean getShareTag() {return true;}

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (EnumInstruments inst : EnumInstruments.values())
        {
            ItemStack subItemStack = new ItemStack(itemIn, 1, inst.getMetadata());
            subItems.add(subItemStack);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (!worldIn.isRemote)
        {
            /** Server Side - Open the instrument inventory if sneaking */
            if (playerIn.isSneaking() && hand.equals(EnumHand.MAIN_HAND))
            {
                boolean hasCap = stackIn.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                ModLogger.debug("BasicItem has ITEM_HANDLER_CAPABILITY " + hasCap);
                if (hasCap)
                    playerIn.openGui(MXTuneMain.instance, GuiInstrumentInventory.GUI_ID, worldIn, 0, 0, 0);
            }
        } else
        {
            /** Client Side - play the instrument */
            if (!playerIn.isSneaking() && hand.equals(EnumHand.MAIN_HAND)) playMusic(playerIn, stackIn);
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stackIn);
    }

    /** Activate the instrument unconditionally */
    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        // ModLogger.logInfo("Inst#onItemUseFirst hand: " + hand + ", side: " + side + ", pos: " + pos);
        // return EnumActionResult.SUCCESS to activate on AIR only
        // return EnumActionResult.FAIL to activate unconditionally and skip vanilla processing
        // return EnumActionResult.PASS to activate on AIR, or let Vanilla process
        return EnumActionResult.FAIL;
    }
    
    @Override
    public void onKeyPressed(String key, EntityPlayer playerIn, ItemStack stackIn)
    {
        /** Client Side - play the instrument */
        if (key.equalsIgnoreCase("key.playInstrument")) playMusic(playerIn, stackIn);
    }

    /** This is where we decide how our item interacts with other entities */
    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand)
    {
        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }

    /**
     * NOTE: If you want to open your GUI on right click and your ItemStore, you
     * MUST override getMaxItemUseDuration to return a value of at least 1,
     * otherwise you won't be able to open the GUI. That's just how it works.
     */
    @Override
    public int getMaxItemUseDuration(ItemStack itemstack) {return 1;}


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addInformation(ItemStack stackIn, EntityPlayer playerIn, List tooltip, boolean advanced)
    {
        String musicTitle = SheetMusicUtil.getMusicTitle(stackIn);
        if (!musicTitle.isEmpty())
        {
            tooltip.add(TextFormatting.GREEN + "Title: " + musicTitle);
        }
    }

    private void playMusic(EntityPlayer playerIn, ItemStack stackIn)
    {
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(stackIn);
        if (sheetMusic != null)
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("MusicBook");
            if (contents != null)
            {
                /** The packet itself notifies the server to grab the NBT item from the ItemStack and distribute it to all clients. */
                if (GROUPS.getMembersGroupID(playerIn.getDisplayName().getUnformattedText()) == null)
                {
                    /** Solo Play */
                    PacketDispatcher.sendToServer(new PlaySoloMessage(playerIn.getDisplayName().getUnformattedText()));
                } else
                {
                    /** Jam Play */
                    PacketDispatcher.sendToServer(new QueueJamMessage());
                }
                System.out.println("+++ queue play request +++");
            }
        }
    }

    /** TODO: Review this and think about handedness and instruments */
    ItemStack getHeldItemStack(EntityPlayer player)
    {
        if (this.isItemInstrument(player.getHeldItem(EnumHand.MAIN_HAND)))
        {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        } else if (this.isItemInstrument(player.getHeldItem(EnumHand.OFF_HAND)))
        {
            return player.getHeldItem(EnumHand.OFF_HAND);
        } else
            return null;
    }

    /** Check of the item stack we are holding is the type we are interested in */
    protected boolean isItemInstrument(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemInstrumentCaps;
    }

    public class SimpleInventoryProvider implements ICapabilitySerializable<NBTBase>
    {
        ItemStack stack;
        NBTTagCompound nbt;
        
        SimpleInventoryProvider(ItemStack stack, NBTTagCompound nbt)
        {
            this.stack = stack;
            this.nbt = nbt;
        }
        
        public StackHandler stackHandler = new StackHandler(1);

        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
        }

        @SuppressWarnings("unchecked")
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) { return (T) this.stackHandler; }

            return null;
        }

        public NBTBase serializeNBT()
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.stackHandler, (EnumFacing) null);
        }

        public void deserializeNBT(NBTBase nbt)
        {
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.stackHandler, (EnumFacing) null, nbt);
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
    {
        return new SimpleInventoryProvider(stack, nbt);
    }

    public class StackHandler extends ItemStackHandler
    {
        private boolean changed = false;
        protected StackHandler(int size) {super(size);}

        @Override
        protected void onLoad()
        {
            super.onLoad();     
            changed = true;
            ModLogger.debug("StackHandler#onLoad size: " + this.getSlots());           
        }

        @Override
        public void onContentsChanged(int slot)
        {
            changed = true;
            ModLogger.debug("StackHandler#onContentsChanged stacks: " + this.stacks);
        }
        
        boolean hasChanged()
        {
            boolean temp = changed;
            changed = false;
            return temp;            
        }
    }
    
    public int getPatch(ItemStack stack)
    {
        int patch = (stack != null) ? stack.getMetadata() : 0;
        EnumInstruments inst = EnumInstruments.byMetadata(patch);
        return inst.getPatch();
    }

    public static enum EnumInstruments implements IStringSerializable
    {
        TUBA(0, "tuba", 59),
        MANDO(1, "mando", 25),
        FLUTE(2, "flute", 74),
        BONGO(3, "bongo", 117),
        BALAL(4, "balalaika", 28),
        CLARI(5, "clarinet", 72);

        public int getMetadata() {return this.meta;}

        @Override
        public String toString() {return this.name;}

        public static EnumInstruments byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length)
            {
                meta = 0;
            }
            return META_LOOKUP[meta];
        }

        public String getName() {return this.name;}

        public int getPatch() {return this.patch;}

        private final int meta;
        private final String name;
        private final int patch;
        private static final EnumInstruments[] META_LOOKUP = new EnumInstruments[values().length];

        private EnumInstruments(int i_meta, String i_name, int i_patch)
        {
            this.meta = i_meta;
            this.name = i_name;
            this.patch = i_patch;
        }

        static
        {
            for (EnumInstruments value : values())
            {
                META_LOOKUP[value.getMetadata()] = value;
            }
        }
    } 
}
