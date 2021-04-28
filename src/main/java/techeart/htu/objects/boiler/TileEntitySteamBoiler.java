package techeart.htu.objects.boiler;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import techeart.htu.MainClass;
import techeart.htu.objects.HTUFluidTank;
import techeart.htu.objects.TileEntityIgnitable;
import techeart.htu.utils.FuelTemperatures;
import techeart.htu.utils.HTUHooks;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class TileEntitySteamBoiler extends TileEntityIgnitable implements ISidedInventory, INamedContainerProvider, ITickableTileEntity
{
    private Random random = new Random();

    //internal tanks volume constants in mB
    public static final int internalVolumeWater = 4000;
    public static final int internalVolumeSteam = 4000;

    //temperature constants
    public static final int maxTemperature = 120;
    public static final int minTemperature = -30;
    public static final int conversionTemperature = 100;

    //pressure constants
    public static final int maxPressure = 640;
    public static final int initialPressure = 40;
    public static final int ejectionPressure = 560;

    public static final int waterConsumptionRate = 1;
    public static final int conversionFactor = 3;

    //tracked fields
    private int burnTime;
    private int burnTimeTotal;
    private int temperature;
    private int pressure;

    private int fuelBurnTemperature;
    private int ambientTemperature = 16;

    private ITextComponent customName;

    //fluid constant links
    private static final Fluid WATER = Fluids.WATER;
    private static final Fluid STEAM = RegistryHandler.FLUID_STEAM.get();

    private final HTUFluidTank tankWater;
    private final HTUFluidTank tankSteam;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

    public TileEntitySteamBoiler()
    {
        super(RegistryHandler.STEAM_BOILER.getMainBlock().getMachineTile());

        int steamVolume = internalVolumeSteam + (Math.floorDiv(internalVolumeSteam, ejectionPressure) * (maxPressure - ejectionPressure));

        tankWater = new HTUFluidTank(internalVolumeWater, WATER, HTUFluidTank.Type.INSERT_ONLY);
        tankSteam = new HTUFluidTank(steamVolume, STEAM, HTUFluidTank.Type.EJECT_ONLY);

        if(world != null && pos != null)
            ambientTemperature = HTUHooks.getAmbientTemperature(world, pos);
        temperature = ambientTemperature;
        pressure = initialPressure;
    }

    @Override
    public void tick()
    {
        if (this.isBurning()) --this.burnTime;

        if (!this.world.isRemote)
        {

        }
    }

    @Override
    public void ignite()
    {
        if(world.isRemote) return;

        ItemStack fuel = this.inventory.get(0);
        if (!fuel.isEmpty())
        {
            if (!isBurning())
            {
                this.burnTimeTotal = getItemBurnTime(fuel);
                this.burnTime = this.burnTimeTotal;
                this.fuelBurnTemperature = FuelTemperatures.getBurnTemperature(fuel);
                if (this.isBurning())
                {
                    setIgnited(true);
                    if (fuel.hasContainerItem()) this.inventory.set(0, fuel.getContainerItem());
                    else
                    {
                        fuel.shrink(1);
                        if (fuel.isEmpty()) this.inventory.set(0, fuel.getContainerItem());
                    }
                    markDirty();
                }
            }
        }
        else setIgnited(false);
    }

    @Override
    protected void onIgnited()
    {
        this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(BlockStateProperties.LIT, true), 3);
        markDirty();
    }

    @Override
    protected void onExtinguished()
    {
        this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(BlockStateProperties.LIT, false), 3);
        markDirty();
    }

    @Override
    protected void tickIgnition()
    {
        if(isIgnited() && !isBurning())
        {
            if(random.nextInt(100) < 99 - EXTINCTION_CHANCE) ignite();
            else setIgnited(false);
        }
    }

    @Override
    public boolean receiveClientEvent(int id, int type)
    {
        if(id == 0)
        {
            if (this.world.isRemote)
                world.addParticle(ParticleTypes.CLOUD, this.pos.getX() + 0.5D, this.pos.getY() + 1, this.pos.getZ() + 0.5D, 0, 0.2D, 0);
            return true;
        }
        return false;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);

        this.burnTime = nbt.getInt("BurnTime");
        this.burnTimeTotal = nbt.getInt("BurnTimeTotal");
        this.temperature = nbt.getInt("Temperature");
        this.pressure = nbt.getInt("Pressure");

        this.setIgnited(nbt.getBoolean("Ignited"));
        this.fuelBurnTemperature = nbt.getInt("FuelTemperature");

        this.inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(nbt, this.inventory);

        tankWater.readFromNBT(nbt);
        tankSteam.readFromNBT(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        super.write(nbt);

        nbt.putInt("BurnTime", (short)this.burnTime);
        nbt.putInt("BurnTimeTotal", (short)this.burnTimeTotal);
        nbt.putInt("Temperature", (short)this.temperature);
        nbt.putInt("Pressure", (short)this.pressure);

        nbt.putBoolean("Ignited", this.isIgnited());
        nbt.putInt("FuelTemperature", this.fuelBurnTemperature);

        ItemStackHelper.saveAllItems(nbt, this.inventory);

        tankWater.writeToNBT(nbt);
        tankSteam.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public CompoundNBT getUpdateTag() { return write(new CompoundNBT()); }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) { read(state, tag); }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT compound = new CompoundNBT();
        write(compound);
        return new SUpdateTileEntityPacket(this.pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) { read(this.world.getBlockState(pkt.getPos()), pkt.getNbtCompound()); }

    private static int getItemBurnTime(ItemStack stack)
    {
        return ForgeHooks.getBurnTime(stack);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) { return getItemBurnTime(stack) > 0; }

    public int getField(int id)
    {
        int field = -1;
        switch (id)
        {
            case 0: field = this.burnTime;
                break;
            case 1: field = this.burnTimeTotal;
                break;
            case 2: field = this.tankWater.getFluidAmount();
                break;
            case 3: field = this.tankSteam.getFluidAmount();
                break;
            case 4: field = this.temperature;
                break;
            case 5: field = this.pressure;
                break;
        }
        return field;
    }

    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0: this.burnTime = value;
                break;
            case 1: this.burnTimeTotal = value;
                break;
            case 2: //this.waterAmount = value;
                break;
            case 3: //this.steamAmount = value;
                break;
            case 4: this.temperature = value;
                break;
            case 5: this.pressure = value;
                break;
        }
    }

    public int getFieldCount() { return 6; }

    public boolean isBurning()
    {
        return this.burnTime > 0;
    }

    public ITextComponent getDefaultName() { return new TranslationTextComponent("container." + MainClass.MODID + ".steam_boiler"); }

    public void setCustomName(ITextComponent name) { this.customName = name; }

    @Override
    public int getSizeInventory()
    {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty()
    {
        return inventory.get(0).isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return this.inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) { return ItemStackHelper.getAndSplit(this.inventory, index, count); }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return ItemStackHelper.getAndRemove(this.inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        this.inventory.set(index, stack);
        if(stack.getCount() > this.getInventoryStackLimit()) stack.setCount(this.getInventoryStackLimit());
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return this.world.getTileEntity(this.pos) == this &&
                player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clear() { this.inventory.clear(); }

    @Override
    public ITextComponent getDisplayName() { return this.customName != null ? this.customName : this.getDefaultName(); }

    public NonNullList<ItemStack> getInventory() { return inventory; }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) { return new ContainerSteamBoiler(windowId, playerInventory, this); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability)
    {
//        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
//        {
//            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> fluidHandler));
//        }
        return super.getCapability(capability);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
//        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != Direction.DOWN)
//        {
//            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> fluidHandler));
//        }
        return super.getCapability(capability, facing);
    }

    @Override
    public int[] getSlotsForFace(Direction side)
    {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return false;
    }
}