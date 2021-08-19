package techeart.htu.objects.boiler;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import techeart.htu.MainClass;
import techeart.htu.objects.HTUFluidTank;
import techeart.htu.objects.HTUTileEntity;
import techeart.htu.utils.FluidUtils;
import techeart.htu.utils.HTUHooks;
import techeart.htu.utils.registration.RegistryHandler;
import techeart.htu.utils.temperature.ITemperatureHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntitySteamBoilerTank extends HTUTileEntity implements ITickableTileEntity, ITemperatureHandler
{
    //water to steam conversion constants
    public static final int CONVERSION_TEMPERATURE = 100;
    public static final int CONVERSION_RATE = 1;
    public static final int CONVERSION_FACTOR = 3;

    //internal tanks volume constants in mB
    public static final int CAPACITY_WATER = 4000;
    public static final int CAPACITY_STEAM = 4000;

    //fluid constant links
    private static final Fluid WATER = Fluids.WATER;
    private static final Fluid STEAM = RegistryHandler.FLUID_STEAM.get();

    //internal tanks
    private final HTUFluidTank tankWater;
    private final HTUFluidTank tankSteam;

    private int temperature;

    private ITemperatureHandler heater = null;

    public TileEntitySteamBoilerTank()
    {
        super(RegistryHandler.STEAM_BOILER.getMachineBlock(1).getMachineTile());

        tankWater = new HTUFluidTank(CAPACITY_WATER, WATER, HTUFluidTank.Type.DEFAULT);
        tankSteam = new HTUFluidTank(CAPACITY_STEAM, STEAM, HTUFluidTank.Type.EJECT_ONLY);
    }

    public void updateConnections()
    {
        if(world == null)
        {
            MainClass.LOGGER.error("Tried to update boiler tank connections while world was unloaded");
            return;
        }

        TileEntitySteamBoiler boiler = (TileEntitySteamBoiler) world.getTileEntity(pos.down());
        if(boiler == null)
        {
            MainClass.LOGGER.error("No boiler heater at required pos: " + pos.down());
            return;
        }
        heater = boiler;
        System.out.println(heater);
    }

    /*HTUTileEntity*/
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState)
    {
        super.onBlockAdded(state, world, pos, oldState);
        updateConnections();
    }

    @Override
    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState)
    {
        super.onBlockRemoved(state, world, pos, newState);

        //remove heater
        BlockState blockBelow = world.getBlockState(pos.down());
        if(blockBelow.getBlock() instanceof BlockSteamBoiler)
            world.setBlockState(pos.down(), Blocks.AIR.getDefaultState());
    }

    @Override
    public void onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
    {
        super.onBlockActivated(state, world, pos, player, hand, hit);
        if(!world.isRemote)
        {
            ItemStack heldItem = player.getHeldItem(hand);
            LazyOptional<IFluidHandler> lo = getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
            FluidUtils.interactWithTank(player, hand, heldItem, lo.orElse(null), 0);
        }
    }

    /*ITickableTileEntity*/
    @Override
    public void tick()
    {
        if(world.isRemote()) return;
        if(heater == null) return;
        System.out.println(getTemperature());
        if(getTemperature() < CONVERSION_TEMPERATURE) return;
        if(tankWater.isEmpty()) return;

        if(tankSteam.isFull()) return; //TODO remove this and add explosion logic

        int toConvert = tankWater.forceDrain(CONVERSION_RATE, IFluidHandler.FluidAction.EXECUTE).getAmount();
        int converted = tankSteam.forceFill(new FluidStack(STEAM, toConvert * CONVERSION_FACTOR), IFluidHandler.FluidAction.EXECUTE);

        System.out.println("Water: " + tankWater.getFluidAmount() + " / Steam: " + tankSteam.getFluidAmount());
    }

    /*TileEntity*/
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap)
    {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
             return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> tankWater));
        return super.getCapability(cap);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            if(side != Direction.UP) return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> tankWater));
            else return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> tankSteam));
        return super.getCapability(cap, side);
    }

    /*ITemperatureHandler*/
    @Override
    public int getTemperature()
    {
        if(heater != null) return heater.getTemperature();
        return HTUHooks.getAmbientTemperature(world, pos);
    }

    @Override
    public void setTemperature(int value) { }

    @Override
    public int getMaxTemperature() { return 120; }

    @Override
    public int getMinTemperature() { return -20; }
}
