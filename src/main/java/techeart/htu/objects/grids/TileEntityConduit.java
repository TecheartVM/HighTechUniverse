package techeart.htu.objects.grids;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction8;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class TileEntityConduit extends TileEntity implements IFluidConduit, ITickableTileEntity, IFluidHandler
{
    public static int CAPACITY = FluidAttributes.BUCKET_VOLUME;
    public static int TRANSFER_RATE = 125;

    private final FluidTank tank = new FluidTank(CAPACITY);

    protected UUID gridId;
    protected Grid grid;

    public TileEntityConduit() { this(RegistryHandler.FLUID_PIPE_TE.get()); }

    public TileEntityConduit(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
        tank.setValidator((fluid) -> tank.isEmpty() || fluid.isFluidEqual(tank.getFluid()));
        System.out.println("Conduit created!");
    }

    /*grid interaction*/
    public void connectOrCreateGrid()
    {
        //check all neighbour tiles
        Set<Connection> peripherals = forEachNonPeripheral(c -> {
            TileEntityConduit conduit = (TileEntityConduit) c.tile;
            if(grid == null)
            {
                if (conduit.grid.addConduit(this))
                    setGrid(conduit.grid);
            }
            else grid.mergeWith(conduit.grid);
        });

        //if grid isn't exist, create it
        if(grid == null)
        {
            setGrid(new Grid());
            grid.addConduit(this);
        }

        grid.addPeripherals(peripherals);
    }

    public void onGridReconstruction(@Nonnull Grid newGrid)
    {
        setGrid(newGrid);

        Set<Connection> peripherals = forEachNonPeripheral(c -> {
            if(((TileEntityConduit) c.tile).grid != newGrid) onGridReconstruction(newGrid);
        });

        grid.addPeripherals(peripherals);
    }

    public void setGrid(Grid grid)
    {
        this.grid = grid;
        gridId = grid.getId();
    }

    /**Applies action to every non-peripheral connection. Returns the Set of rejected connections (peripherals).*/
    protected Set<Connection> forEachNonPeripheral(Consumer<Connection> action)
    {
        Set<Connection> peripherals = new HashSet<>();
        for (Direction dir : Direction.values())
        {
            TileEntity te = world.getTileEntity(pos.offset(dir));
            if (te == null || te.isRemoved()) continue;

            LazyOptional<IFluidHandler> lo = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
            if (!lo.isPresent()) continue;

            IFluidHandler fh = lo.orElse(null);
            Connection c = new Connection(te, fh, this, dir);

            if (!c.isConduit() || dir.getAxis().isVertical()) peripherals.add(c);
            else action.accept(c);
        }
        return peripherals;
    }

    int timer = 0;

    protected Map<Direction, Integer> dirsToSources = new HashMap<>();
    protected int transferToNeighbours()
    {
        //System.out.println(dirsToSources.size());
        Set<Connection> neighbours = new HashSet<>();
        for (Direction dir : Direction.values())
        {
            TileEntity te = world.getTileEntity(pos.offset(dir));
            if (te == null || te.isRemoved()) continue;

            LazyOptional<IFluidHandler> lo = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
            if (!lo.isPresent()) continue;

            IFluidHandler fh = lo.orElse(null);
            Connection c = new Connection(te, fh, this, dir);

            //accepting only horizontal conduit connections with free space for required fluid in internal tank
            if (!dir.getAxis().isVertical() && c.isConduit() && fh.fill(new FluidStack(tank.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE) > 0)
            {
                neighbours.add(c);
            }
        }

        if(neighbours.isEmpty()) return 0;

        int averageAmount = tank.getFluidAmount();
        for (Connection c : neighbours)
            averageAmount += c.fluidHandler.drain(new FluidStack(tank.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE).getAmount();
        averageAmount = Math.floorDiv(averageAmount, neighbours.size());

        //System.out.println(averageAmount);

        int transferredAmount = 0;
        for (Connection c : neighbours)
        {
            //operating connection only if it is not at source direction
            if(!dirsToSources.containsKey(c.side))
            {
                //getting amount of required fluid
                int toFill = c.fluidHandler.drain(new FluidStack(tank.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE).getAmount();
                System.out.println("Fluid in neighbour: " + toFill + " In this pipe: " + tank.getFluid().getAmount());
                System.out.println("Average amount: " + averageAmount);
                //getting amount of fluid we need to send to reach the average amount
                toFill = averageAmount - toFill;
                if(toFill > 0) //TODO fix filling pipes with larger amount of fluid
                {
                    //getting amount, restricted by transfer rate
                    toFill = Math.min(toFill, TRANSFER_RATE);
                    //using special conduit fill method to provide information about fluid source location
                    transferredAmount += ((TileEntityConduit)c.tile).fill(new FluidStack(tank.getFluid(), toFill), FluidAction.EXECUTE, c.side);
                }
            }
            else System.out.println("Can't fill source!");
        }

        System.out.println("Transferred amount: " + transferredAmount);
        return transferredAmount;
    }

    protected int fill(FluidStack resource, FluidAction action, Direction dirToSource)
    {
        //if map has such direction, resetting the last update time
        //else put new direction
        dirsToSources.put(dirToSource, 0);

        return fill(resource, action);
    }

    /*TileEntity*/
    @Override
    public void tick()
    {
        if(getWorld().isRemote) return;

        if(timer < 40)
        {
            timer++;
            return;
        }
        else timer = 0;

        //removing all inactive source directions
        if(!dirsToSources.isEmpty())
        {
            Set<Direction> toRemove = new HashSet<>();
            for (Map.Entry<Direction, Integer> e : dirsToSources.entrySet()) {
                e.setValue(e.getValue() + 1);
                if (e.getValue() > 1) toRemove.add(e.getKey());
            }
            toRemove.forEach(dir -> dirsToSources.remove(dir));
        }
        else System.out.println("No sources in system");

        if(tank.isEmpty()) return;

        drain(transferToNeighbours(), FluidAction.EXECUTE);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        super.write(nbt);
        //nbt.putUniqueId("GridId", gridId);
        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        //gridId = nbt.getUniqueId("GridId");
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap)
    {
        if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));
        return super.getCapability(cap);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));
        return super.getCapability(cap, side);
    }

    @Override
    public void remove()
    {
        //grid.removeConduit(this);
        super.remove();
        //resetting grid for all of connected conduits
        //forEachNonPeripheral(c -> ((TileEntityConduit)c.tile).onGridReconstruction(new Grid()));
    }

    /*IFluidHandler*/
    @Override
    public int getTanks() { return 1; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank)
    {
        //return grid == null ? FluidStack.EMPTY : grid.getFluidPerConduit();
        return this.tank.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) { return CAPACITY; }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
    {
        //return grid != null && grid.isFluidValid(stack);
        return this.tank.isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        //int result = grid == null ? 0 : grid.fill(resource, action);
        int result = tank.fill(resource, action);
        return result;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        //return grid == null ? FluidStack.EMPTY : grid.drain(resource, action);
        return tank.drain(resource, action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        //return grid == null ? FluidStack.EMPTY : grid.drain(maxDrain, action);
        return tank.drain(maxDrain, action);
    }
}
