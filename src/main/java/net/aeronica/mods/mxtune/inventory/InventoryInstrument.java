package net.aeronica.mods.mxtune.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class InventoryInstrument implements IInventory {
	private static final String name = "container.mxtune.instrument";

	/** Provides NBT Tag Compound to reference */
	private final ItemStack stack;

	/** Defining your inventory size this way is handy */
	public static final int INV_SIZE = 1;

	/** Inventory's size must be same as number of slots you add to the Container class */
	private final NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(INV_SIZE, ItemStack.EMPTY);

	/**
	 * @param itemstack
	 *            - the ItemStack to which this inventory belongs
	 */
	public InventoryInstrument(ItemStack itemstack) {
		this.stack = itemstack;
		// Create a new NBT Tag Compound if one doesn't already exist, or you will crash
		if (!this.stack.hasTagCompound()) {
			this.stack.setTagCompound(new NBTTagCompound());
		}

		// Read the inventory contents from NBT
		readFromNBT(this.stack.getTagCompound());
	}

	@Override
	public int getSizeInventory() {return inventory.size();}

	@Override
	public ItemStack getStackInSlot(int slot) {return inventory.get(slot);}

	@Override	
	public ItemStack decrStackSize(int index, int count)
	{
	    ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventory, index, count);

	    if (!itemstack.isEmpty())
	    {
	        this.markDirty();
	    }

	    return itemstack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) {
		inventory.set(slot, itemstack);
		if (!itemstack.isEmpty() && itemstack.getCount() > this.getInventoryStackLimit()) {
			itemstack.setCount(this.getInventoryStackLimit());
		}
		// Don't forget this line or your inventory will not be saved!
		this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {return INV_SIZE;}

	/**
	 * This is the method that will handle saving the inventory contents, as it is called (or should be called!) anytime
	 * the inventory changes. Perfect. Much better than using onUpdate in an Item, as this will also let you change
	 * things in your inventory without ever opening a Gui, if you want.
	 */
	@Override
	public void markDirty() {
		for (int i = 0; i < this.getSizeInventory(); ++i) {
			if (!this.getStackInSlot(i).isEmpty() && this.getStackInSlot(i).getCount() == 0)
				this.setInventorySlotContents(i, ItemStack.EMPTY);
		}
		// This line here does the work:
		this.writeToNBT(this.stack.getTagCompound());
	}

	/**
	 * This method doesn't seem to do what it claims to do, as items can still be left-clicked and placed in the
	 * inventory even when this returns false
	 */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		// Don't want to be able to store the inventory item within itself
		// Bad things will happen, like losing your inventory
		// Actually, this needs a custom Slot to work
		return !(itemstack.getItem() instanceof IInstrument);
	}

	/**
	 * A custom method to read our inventory from an ItemStack's NBT compound
	 */
	public void readFromNBT(NBTTagCompound compound) {
		// Gets the custom taglist we wrote to this compound, if any
		NBTTagList items = compound.getTagList("ItemInventory", 10);

		for (int i = 0; i < items.tagCount(); ++i) {
			NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(i);
			int slot = item.getInteger("Slot");

			// Just double-checking that the saved slot index is within our inventory array bounds
			if (slot >= 0 && slot < getSizeInventory()) {
				setInventorySlotContents(slot, new ItemStack(item));
			}
		}
	}

	/**
	 * A custom method to write our inventory to an ItemStack's NBT compound
	 */
	public void writeToNBT(NBTTagCompound compound) {
		// Create a new NBT Tag List to store ItemStacks as NBT Tags
		NBTTagList items = new NBTTagList();

		for (int i = 0; i < getSizeInventory(); ++i) {
			// Only write stacks that contain items
			if (!getStackInSlot(i).isEmpty()) {
				// Make a new NBT Tag Compound to write the ItemStack and slot index to
				NBTTagCompound item = new NBTTagCompound();
				item.setInteger("Slot", i);
				// Writes the ItemStack in slot(i) to the Tag Compound we just made
				getStackInSlot(i).writeToNBT(item);

				// add the tag compound to our tag list
				items.appendTag(item);
			}
		}
		// Add the TagList to the ItemStack's Tag Compound with the name "ItemInventory"
		compound.setTag("ItemInventory", items);
	}

	@Override
	public String getName() {return name;}

	@Override
	public boolean hasCustomName() {return false;}

	@Override
	public ITextComponent getDisplayName() {return null;}

	@Override
	public ItemStack removeStackFromSlot(int index) {return decrStackSize(index, stack.getCount());}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public int getField(int id) {return 0;}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {return 0;}

	@Override
	public void clear() {}

    @Override
    public boolean isEmpty() {return false;}

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {return false;}
    
}