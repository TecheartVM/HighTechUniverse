package techeart.htu.objects.pipe;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import techeart.htu.MainClass;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TileEntityPipeFluid extends TileEntity implements ITickableTileEntity, IFluidHandler, IPipe
{
    public static int CAPACITY = FluidAttributes.BUCKET_VOLUME;
    public static int TRANSFER_RATE = 125;

    private HorizontalPipeGrid grid = null;
    private UUID gridId;

    private int ticksExisted = 0;

    public TileEntityPipeFluid()
    {
        super(RegistryHandler.FLUID_PIPE_TE.get());
    }

    /*IPipe*/
    @Override
    public IPipeGrid getGrid() { return grid; }

    @Override
    public void setGrid(IPipeGrid grid)
    {
        if(!(grid instanceof HorizontalPipeGrid)) return;
        this.grid = (HorizontalPipeGrid)grid;
        gridId = grid.getId();
        for (PipeConnectionFluid c : getConnections())
        {
            if(!c.isPipe()) { ((HorizontalPipeGrid) grid).addExternal(c); }
            else if(c.side.getAxis() != Direction.Axis.Y) grid.addPipe((IPipe) c.tile);
            else
            {
                ((HorizontalPipeGrid) grid).addExternal(c);
                //if we have external pipe at the bottom or top, we need to add this pipe to its externals list
                // because the neighbour can not attach this pipe acquire it behave an own grid
                LazyOptional<IFluidHandler> lo = getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, c.side.getOpposite());
                if(!lo.isPresent()) continue;
                IFluidHandler fh = lo.orElse(null);
                ((HorizontalPipeGrid)((TileEntityPipeFluid) c.tile).getGrid()).addExternal(new PipeConnectionFluid(this, fh, c.side.getOpposite()));
            }
        }
        this.grid.recalculateCapacity();
    }

    @Override
    public void createGrid() { System.out.println("Created grid!"); new HorizontalPipeGrid().addPipe(this);
        System.out.println("Grid id: " + grid.getId()); }

    @Override
    public void updateGrid() { setGrid(grid); }

    @Override
    public Set<PipeConnectionFluid> getConnections()
    {
        if(world == null) return null;
        Set<PipeConnectionFluid> result = new HashSet<>();
        for (Direction side : Direction.values())
        {
            TileEntity te = world.getTileEntity(pos.offset(side));
            if(te == null) continue;
            LazyOptional<IFluidHandler> lo = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
            IFluidHandler fh = lo.orElse(null);
            if(lo.isPresent()) result.add(new PipeConnectionFluid(te, fh, side));
        }
        return result;
    }

    /*TileEntity*/
    @Override
    public void remove()
    {
        if(grid != null)
        {
            FluidStack gridPerPipeContent = grid.getFluidPerPipe();
            grid.destroy();
            for (PipeConnectionFluid c : getConnections())
            {
                if (!c.isPipe()) continue;
                if (c.side.getAxis() == Direction.Axis.Y) continue;
                ((TileEntityPipeFluid) c.tile).createGrid();
                HorizontalPipeGrid newGrid = (HorizontalPipeGrid) ((TileEntityPipeFluid) c.tile).getGrid();
                newGrid.fill(new FluidStack(gridPerPipeContent.getFluid(), gridPerPipeContent.getAmount() * newGrid.getPipes().size()), FluidAction.EXECUTE);
            }
        }

        super.remove();
    }

    /*IFluidHandler*/
    @Override
    public int getTanks() { return 1; }

    @Override
    public int getTankCapacity(int tank) { return CAPACITY; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank)
    {
        if(getGrid() == null)
        {
            MainClass.LOGGER.warn("Can't get fluid in tank. Pipe grid is null!");
            return FluidStack.EMPTY;
        }
        return ((HorizontalPipeGrid)getGrid()).getFluidPerPipe();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
    {
        if(getGrid() == null)
        {
            MainClass.LOGGER.warn("Can't validate fluid. Pipe grid is null!");
            return false;
        }
        return ((HorizontalPipeGrid)getGrid()).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        if(getGrid() == null)
        {
            MainClass.LOGGER.warn("Can't fill pipe tank. Pipe grid is null!");
            return 0;
        }
        return ((HorizontalPipeGrid)getGrid()).fill(resource, action);
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        if(getGrid() == null)
        {
            MainClass.LOGGER.warn("Can't drain pipe tank with resource. Pipe grid is null!");
            return FluidStack.EMPTY;
        }
        return ((HorizontalPipeGrid)getGrid()).drain(resource, action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        if(getGrid() == null)
        {
            MainClass.LOGGER.warn("Can't drain pipe tank. Pipe grid is null!");
            return FluidStack.EMPTY;
        }
        return ((HorizontalPipeGrid)getGrid()).drain(maxDrain, action);
    }

    /*ICapabilityProvider*/
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap)
    {
        if(getGrid() == null) return super.getCapability(cap);
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));

        return super.getCapability(cap);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if(getGrid() == null) return super.getCapability(cap, side);
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));

        return super.getCapability(cap, side);
    }

    /*INBTSerializable*/
    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        if(!nbt.hasUniqueId("gridId")) return;
        gridId = nbt.getUniqueId("gridId");
        //System.out.println("Grid id: " + gridId);
        super.read(state, nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        if(grid != null) compound.putUniqueId("gridId", grid.getId());
        return super.write(compound);
    }

    @Override
    public void tick()
    {
        if(world.isRemote || grid != null || !MainClass.gridsManager.isLoaded()) return;

        if(gridId == null)
        {
            createGrid();
            return;
        }
        grid = (HorizontalPipeGrid) MainClass.gridsManager.getGrid(gridId);
        if(grid == null)
        {
            createGrid();
        }
        else
        {
            grid.addPipe(this);
        }
    }
}
