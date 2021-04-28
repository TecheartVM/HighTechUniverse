package techeart.htu.objects.smeltery;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import techeart.htu.MainClass;
import techeart.htu.objects.TileEntityIgnitable;
import techeart.htu.recipes.alloying.AlloyRecipe;
import techeart.htu.recipes.alloying.AlloyRecipes;
import techeart.htu.utils.InventoryHelper;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nullable;

public class TileEntitySmeltery extends TileEntityIgnitable implements ISidedInventory, ITickableTileEntity, INamedContainerProvider
{
    private static final int[] SLOTS_TOP = {0,1,2,3};
    private static final int[] SLOTS_BOTTOM = {4,5};
    private static final int[] SLOTS_SIDE = {4,5};

    protected final int tier = 0;

    /**Slots indexes:    Input[ 0,1,2,3 ];    Fuel[ 4 ];    Output[ 5 ]*/
    private NonNullList<ItemStack> inventory = NonNullList.withSize(6, ItemStack.EMPTY);

    private int burnTime;
    private int burnTimeTotal;
    private int smeltTime;
    private int smeltTimeTotal;

    private AlloyRecipe currentRecipe = null;
    private boolean isSmelting = false;

    public TileEntitySmeltery()
    {
        super(RegistryHandler.SMELTERY.getMainBlock().getMachineTile());
    }

    /*ISidedInventory*/
    @Override
    public int getSizeInventory() { return this.inventory.size(); }

    @Override
    public boolean isEmpty()
    {
        for(ItemStack stack : this.inventory)
            if(!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) { return this.inventory.get(index); }

    @Override
    public ItemStack decrStackSize(int index, int count) { return ItemStackHelper.getAndSplit(this.inventory, index, count); }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return ItemStackHelper.getAndRemove(this.inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack inputStack)
    {
        ItemStack stackInSlot = this.inventory.get(index);
        boolean flag = !inputStack.isEmpty() && inputStack.isItemEqual(stackInSlot) && ItemStack.areItemStackTagsEqual(inputStack, stackInSlot);

        this.inventory.set(index, inputStack);

        if(inputStack.getCount() > this.getInventoryStackLimit())
            inputStack.setCount(this.getInventoryStackLimit());

        if(!flag && index >= 0 && index < 4)
        {
            this.smeltTimeTotal = getCurrentRecipeTime();
            this.smeltTime = 0;
            this.markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        if(index == 5) return false;
        if(index != 4) return true;
        return isItemFuel(stack);
    }

    @Override
    public void clear() { this.inventory.clear(); }

    @Override
    public int[] getSlotsForFace(Direction side) { return side == Direction.UP ? SLOTS_TOP : side == Direction.DOWN ? SLOTS_BOTTOM : SLOTS_SIDE; }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction) { return isItemValidForSlot(index, itemStackIn); }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) { return index == 5; }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return player.world.getTileEntity(this.pos) == this &&
                player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    /*TileEntity*/
    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(nbt, this.inventory);
        updateCurrentRecipe();
        this.burnTime = nbt.getInt("BurnTime");
        this.smeltTime = nbt.getInt("SmeltTime");
        //this.totalCookTime = nbt.getInt("CookTimeTotal");
        this.burnTimeTotal = nbt.getInt("burnTimeTotal"); //getItemBurnTime((ItemStack)this.inventory.get(4));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        super.write(nbt);
        nbt.putInt("BurnTime", (short)this.burnTime);
        nbt.putInt("SmeltTime", (short)this.smeltTime);
        //nbt.putInt("CookTimeTotal", (short)this.totalCookTime);
        nbt.putInt("burnTimeTotal", (short)this.burnTimeTotal);
        ItemStackHelper.saveAllItems(nbt, this.inventory);

        return nbt;
    }

    @Override
    public void tick()
    {
        boolean burning = this.isBurning();
        boolean isDirty = false;

        if(this.isBurning()) { --this.burnTime; }

        if(!this.world.isRemote)
        {
            tickIgnition();

            ItemStack fuel = this.inventory.get(4);
            if(this.isBurning()
                    || !fuel.isEmpty() &&
                      (!this.inventory.get(0).isEmpty()
                    || !this.inventory.get(1).isEmpty()
                    || !this.inventory.get(2).isEmpty()
                    || !this.inventory.get(3).isEmpty()))
            {

                if(this.isBurning() && this.canSmelt())
                {
                    ++this.smeltTime;
                    isSmelting = true;
                    if(this.smeltTime >= this.smeltTimeTotal)
                    {
                        this.isSmelting = false;
                        this.smeltTime = 0;
                        this.smeltItem();
                        isDirty = true;
                        updateCurrentRecipe();
                    }
                }
                else
                {
                    this.isSmelting = false;
                    this.smeltTime = 0;
                }
            }
            else if(!this.isBurning() && smeltTime > 0)
                this.smeltTime = MathHelper.clamp(this.smeltTime - 2, 0, this.smeltTimeTotal);

            if(burning != this.isBurning())
            {
                isDirty = true;
                BlockSmeltery.setState(this.isBurning(), this.world, this.pos);
            }
        }
        if(isDirty) this.markDirty();
    }

    /*TileEntityIgnitable*/
    @Override
    public void ignite()
    {
        if(!world.isRemote)
        {
            ItemStack fuel = this.inventory.get(4);
            if (!fuel.isEmpty() && !isBurning())
            {
                setField(1, getItemBurnTime(fuel));
                setField(0, getField(1));
                if (this.isBurning())
                {
                    setIgnited(true);
                    if (fuel.hasContainerItem()) this.inventory.set(4, fuel.getContainerItem()); //used for fuels like lava bucket
                    else
                    {
                        fuel.shrink(1);
                        if (fuel.isEmpty()) this.inventory.set(1, fuel.getContainerItem());
                    }
                    BlockSmeltery.setState(this.isBurning(), this.world, this.pos);
                }
            }
            else extinguish();
        }
    }

    @Override
    protected void tickIgnition()
    {
        if(isIgnited() && !isBurning())
        {
            if(random.nextInt(100) <= 99 - EXTINCTION_CHANCE) ignite();
            else extinguish();
        }
    }

    @Override
    protected void onIgnited() { }

    @Override
    protected void onExtinguished() { }

    /*work logic*/
    protected void updateCurrentRecipe()
    {
        if(world == null || world.isRemote) return;
        currentRecipe = AlloyRecipes.getValidRecipe(tier, inventory, 0, 4);
        this.smeltTimeTotal = this.getCurrentRecipeTime();
//        if(currentRecipe == null) System.out.println("Current recipe is null");
//        else System.out.println("Current recipe is: " + currentRecipe.toString());
    }

    public int getCurrentRecipeTime()
    {
        if(currentRecipe == null) return 400;
        return currentRecipe.time;
    }

    public boolean isSmelting() { return isSmelting; }

    private boolean canSmelt()
    {
        if(currentRecipe == null) return false;

        ItemStack result = currentRecipe.result;
        if(result.isEmpty()) return false;

        ItemStack output = this.inventory.get(5);
        if (output.isEmpty()) return true;
        if (!output.isItemEqual(result)) return false;
        int resCount = output.getCount() + result.getCount();
        return resCount <= getInventoryStackLimit() && resCount <= output.getMaxStackSize();
    }

    public void smeltItem()
    {
        if(currentRecipe == null) return;

        ItemStack result = currentRecipe.result;
        ItemStack output = this.inventory.get(5);

        if(InventoryHelper.takeItems(inventory, currentRecipe.ingredientsList, true, 0, 4))
        {
            if (output.isEmpty()) this.inventory.set(5, result.copy());
            else if (output.getItem() == result.getItem()) output.grow(result.getCount());
            else MainClass.LOGGER.error("Can't place output item: " + result.getItem());
        }
    }

    public boolean isBurning() { return this.burnTime > 0; }

    public static int getItemBurnTime(ItemStack stack) { return ForgeHooks.getBurnTime(stack); }

    public static boolean isItemFuel(ItemStack stack) { return getItemBurnTime(stack) > 0; }

    /*accessors*/
    /**0=burnTime; 1=burnTimeTotal; 2=smeltTime; 3=smeltTimeTotal*/
    public int getField(int id)
    {
        switch(id)
        {
            case 0: return this.burnTime;
            case 1: return this.burnTimeTotal;
            case 2: return this.smeltTime;
            case 3: return smeltTimeTotal;

            default: return 0;
        }
    }

    /**0=burnTime; 1=burnTimeTotal; 2=smeltTime; 3=smeltTimeTotal*/
    public void setField(int id, int value)
    {
        switch(id)
        {
            case 0: this.burnTime = value;
                break;
            case 1: this.burnTimeTotal = value;
                break;
            case 2: this.smeltTime = value;
                break;
            case 3: this.smeltTimeTotal = value;
                break;
        }
    }

    public int getFieldCount() { return 4; }

    public NonNullList<ItemStack> getInventory() { return inventory; }

    /*INamedContainerProvider*/
    @Override
    public ITextComponent getDisplayName() { return new TranslationTextComponent("container." + MainClass.MODID + ".smeltery"); }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity p_createMenu_3_)
    {
        return new ContainerSmeltery(windowId, playerInventory, this);
    }
}
