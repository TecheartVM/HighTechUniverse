package techeart.htu.objects.pump;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import techeart.htu.objects.HTUFluidTank;
import techeart.htu.utils.CapabilityUtils;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class TileEntityWaterPump extends TileEntity implements ITickableTileEntity
{
    protected static final int internalVolumeWater = 4000;
    protected static final int internalVolumeSteam = 4000;
    protected static final int ejectionRate = 125;
    protected static final int operationTime = 40;          //40 == 2 seconds
    protected static final int steamConsumptionRate = 25;   //25 == 500 per second

    protected static final int radius = 10;

    //fluid constant links
    private static final Fluid WATER = Fluids.WATER;
    private static final Fluid STEAM = RegistryHandler.FLUID_STEAM.get();

    private final HTUFluidTank tankWater;
    private final HTUFluidTank tankSteam;

    public TileEntityWaterPump()
    {
        super(RegistryHandler.WATER_PUMP.getMainBlock().getMachineTile());
        tankWater = new HTUFluidTank(internalVolumeWater, WATER, HTUFluidTank.Type.EJECT_ONLY);
        tankSteam = new HTUFluidTank(internalVolumeSteam, STEAM, HTUFluidTank.Type.INSERT_ONLY);
    }

    private IFluidHandler output = null;
    public void updateConnections()
    {
        output = null;
        if(world == null || isRemoved()) return;

        Direction pumpFace = BlockWaterPump.getFacing(getBlockState());
        //output connection check
        CapabilityUtils.getCapability(world, pos.offset(pumpFace.rotateYCCW()), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, pumpFace.rotateY())
                .ifPresent(handler -> output = handler);
    }

    private final LinkedHashSet<FluidPath> waterSources = new LinkedHashSet<>();
    public void updateWaterSources(BlockPos firstSourcePos)
    {
        if(world == null || world.isRemote) return;

        waterSources.clear();
        FluidState f = world.getFluidState(firstSourcePos);
        if(f.isEmpty() || f.getFluid() != WATER) return;

        FluidPath initial = new FluidPath(world, firstSourcePos, null);
        Set<FluidPath> curCheck = new HashSet<>();
        Set<FluidPath> nextCheck = new HashSet<>();
        curCheck.add(initial);

        for (int i = 0; i < radius; i++)
        {
            nextCheck.clear();
            for (FluidPath fp : curCheck)
                for (FluidPath np : fp.getNext())
                    if(!waterSources.contains(np))
                        nextCheck.add(np);
            curCheck.clear();
            curCheck.addAll(nextCheck);
            waterSources.addAll(nextCheck);
        }
    }

    private int operationProgress = 0;
    protected void suck()
    {
        if(tankWater.isFull())
        {
            operationProgress = 0;
            return;
        }

        if(operationProgress < operationTime) operationProgress++;
        else
        {
            if(consumeWaterSource())
            {
                tankWater.forceFill(new FluidStack(WATER, FluidAttributes.BUCKET_VOLUME), FluidAction.EXECUTE);
            }
            operationProgress = 0;
        }
    }

    protected boolean consumeWaterSource()
    {
        if(world == null) return false;
        updateWaterSources(pos.down());
        if(waterSources.isEmpty()) return false;
        //getting the last added source
        BlockPos sourcePos = ((FluidPath) waterSources.toArray()[waterSources.size() - 1]).pos;
        return world.setBlockState(sourcePos, Blocks.AIR.getDefaultState());
    }

    /*ITickableTileEntity*/
    @Override
    public void tick()
    {
        if (!this.world.isRemote)
        {
            //ejecting sucked water if possible
            if(output != null && !tankWater.isEmpty())
                output.fill(tankWater.forceDrain(ejectionRate, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);

            //consuming steam, if is present, and do operation
            suck();
//            if(!tankSteam.isEmpty())
//            {
//                if(tankSteam.forceDrain(steamConsumptionRate, IFluidHandler.FluidAction.EXECUTE).getAmount() >= steamConsumptionRate)
//                {
//                    suck();
//                }
//                else operationProgress = 0;
//            }
        }
    }

    /*TileEntity*/
    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        tankWater.readFromNBT(nbt);
        tankSteam.readFromNBT(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        super.write(nbt);
        tankWater.writeToNBT(nbt);
        tankSteam.writeToNBT(nbt);
        return nbt;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> tankWater));
        return super.getCapability(capability);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            Direction pumpFace = BlockWaterPump.getFacing(getBlockState());
            //water port
            if(facing == pumpFace.rotateY())
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> tankWater));
            //steam port
            if(facing == pumpFace.rotateYCCW())
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> tankSteam));
        }
        return super.getCapability(capability, facing);
    }

    /*tanks accessors*/
    @Nonnull
    public FluidStack getFluidInTank(int tank) { return tank == 1 ? tankSteam.getFluid() : tankWater.getFluid(); }

    /*fluid path*/
    protected static class FluidPath
    {
        public World world;
        public BlockPos pos;
        public FluidPath prev;

        public FluidPath(World world, BlockPos pos, FluidPath prev)
        {
            this.world = world;
            this.pos = pos;
            this.prev = prev;
        }

        public Set<FluidPath> getNext()
        {
            Fluid f = world.getFluidState(pos).getFluid();
            Set<FluidPath> result = new HashSet<>();
            Direction checkDir = Direction.NORTH;
            for (int i = 0; i < 4; i++)
            {
                BlockPos newPos = pos.offset(checkDir);
                FluidState fs = world.getFluidState(newPos);
                if(!fs.isEmpty() && fs.isSource() && fs.getFluid() == f)
                    result.add(new FluidPath(world, newPos, this));
                checkDir = checkDir.rotateY();
            }
            return result;
        }
    }
}
