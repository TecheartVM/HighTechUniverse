package techeart.htu.objects.boiler;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.network.NetworkHooks;
import techeart.htu.MainClass;
import techeart.htu.objects.TileEntityIgnitable;
import techeart.htu.utils.FuelTemperatures;
import techeart.htu.utils.HTUHooks;
import techeart.htu.utils.registration.RegistryHandler;
import techeart.htu.utils.temperature.ITemperatureHandler;

import javax.annotation.Nullable;

public class TileEntitySteamBoiler extends TileEntityIgnitable implements ISidedInventory, INamedContainerProvider, ITickableTileEntity, ITemperatureHandler
{
    //tracked fields
    private int burnTime;
    private int burnTimeTotal;
    private int temperature;
    private int pressure;

    private int fuelBurnTemperature;
    private int ambientTemperature = 16;

    private ITextComponent customName;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

    public TileEntitySteamBoiler()
    {
        super(RegistryHandler.STEAM_BOILER.getMainBlock().getMachineTile());
    }

    //--Potentially good temperature per-tick-incrementer formula--//
    //    float inc = -Math.exp(0.1f*(curTemp - maxTemp)) + 1;     //
    //-------------------------------------------------------------//

    @Override
    public void tick()
    {
        if (isBurning()) --burnTime;

        if (!world.isRemote())
        {
            tickIgnition();

            if(temperature < fuelBurnTemperature) temperature++;
            else if(!isBurning() && temperature > ambientTemperature) temperature--;
        }
    }

    /*HTUTileEntity*/
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        super.onPlaced(world, pos, state, placer, stack);

        if(world.isRemote()) return;
        world.setBlockState(pos.up(), RegistryHandler.STEAM_BOILER.getMachineBlock(1).getBlock().getDefaultState());
        if(stack.hasDisplayName()) setCustomName(stack.getDisplayName());

        ambientTemperature = HTUHooks.getAmbientTemperature(world, pos);
        temperature = ambientTemperature;
    }

    @Override
    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState)
    {
        super.onBlockRemoved(state, world, pos, newState);

        if(world.isRemote()) return;
        //remove boiler top
        BlockState blockAbove = world.getBlockState(pos.up());
        if(blockAbove.getBlock() == RegistryHandler.STEAM_BOILER.getMachineBlock(1).getBlock())
            world.setBlockState(pos.up(), Blocks.AIR.getDefaultState());
    }

    @Override
    public void onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
    {
        super.onBlockActivated(state, world, pos, player, hand, hit);

        if(world.isRemote()) return;
        ItemStack heldItem = player.getHeldItem(hand);
        if(!TileEntityIgnitable.interact(this, heldItem))
            NetworkHooks.openGui((ServerPlayerEntity)player, this, pos);
    }

    /*TileEntityIgnitable*/
    @Override
    public void ignite()
    {
        if(world.isRemote()) return;

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

    /*data*/
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

        return nbt;
    }

    /*network*/
    @Override
    public CompoundNBT getUpdateTag() { return write(new CompoundNBT()); }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) { read(state, tag); }

    @Override
    public boolean receiveClientEvent(int id, int type)
    {
        if(id == 0)
        {
            if (this.world.isRemote)
                world.addParticle(ParticleTypes.CLOUD, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, 0, 0.2D, 0);
            return true;
        }
        return false;
    }

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

    /*misc*/
    private static int getItemBurnTime(ItemStack stack)
    {
        return ForgeHooks.getBurnTime(stack);
    }

    public int getField(int id)
    {
        int field = -1;
        switch (id)
        {
            case 0: field = this.burnTime;
                break;
            case 1: field = this.burnTimeTotal;
                break;
            case 2: //field = this.tankWater.getFluidAmount();
                break;
            case 3: //field = this.tankSteam.getFluidAmount();
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

    /*name*/
    public ITextComponent getDefaultName() { return new TranslationTextComponent("container." + MainClass.MODID + ".steam_boiler"); }

    public void setCustomName(ITextComponent name) { this.customName = name; }

    /*inventory*/
    @Override
    public int getSizeInventory() { return this.inventory.size(); }

    @Override
    public boolean isEmpty() { return inventory.get(0).isEmpty(); }

    @Override
    public ItemStack getStackInSlot(int index) { return this.inventory.get(index); }

    @Override
    public ItemStack decrStackSize(int index, int count) { return ItemStackHelper.getAndSplit(this.inventory, index, count); }

    @Override
    public ItemStack removeStackFromSlot(int index) { return ItemStackHelper.getAndRemove(this.inventory, index); }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        this.inventory.set(index, stack);
        if(stack.getCount() > this.getInventoryStackLimit()) stack.setCount(this.getInventoryStackLimit());
    }

    @Override
    public int getInventoryStackLimit() { return 64; }

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

    @Override
    public int[] getSlotsForFace(Direction side)
    {
        if(side == Direction.DOWN) return new int[]{0};
        return new int[0];
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) { return index == 0 && getItemBurnTime(stack) > 0; }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction)
    {
        if(direction.getAxis().isVertical()) return false;
        return isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) { return false; }

    /*ITemperatureHandler*/
    @Override
    public int getTemperature() { return temperature; }

    @Override
    public void setTemperature(int value) { temperature = value; }

    @Override
    public int getMaxTemperature() { return 120; }

    @Override
    public int getMinTemperature() { return -20; }
}