package techeart.htu.objects.grids;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techeart.htu.MainClass;

import java.util.*;

public class Grid implements IGrid
{
    private final Set<IFluidConduit> conduits = new HashSet<>();
    private final GridPeripheralsHolder peripherals = new GridPeripheralsHolder();

    private FluidTank content;

    private final UUID id;
    private boolean dirty = true;
    private boolean removed = false;

    public Grid() { this(UUID.randomUUID()); }

    public Grid(UUID id)
    {
        this.id = id;
        content = new FluidTank(0, (fluid) -> content.isEmpty() || fluid.isFluidEqual(content.getFluid()));
    }

    /*IGrid*/
    public void tick()
    {
        if(removed) MainClass.LOGGER.error("Removed Grid is still ticking!");

        if(content.isEmpty()) return;
        content.drain(transferTo(peripherals.bottom, content.getFluid()), FluidAction.EXECUTE); //filling tanks below
        if(content.isEmpty()) return;
        content.drain(transferTo(peripherals.side, content.getFluid()), FluidAction.EXECUTE); //filling tanks at the side
    }

    @Override
    public boolean isDirty() { return dirty; }

    @Override
    public void readFromNBT(CompoundNBT nbt)
    {

    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT nbt)
    {
        nbt.putUniqueId("id", id);
        content.writeToNBT(nbt);
        return nbt;
    }

    /*transfer logic*/
//    protected int transferToPeripherals()
//    {
//        if(content.isEmpty()) return 0;
//
//        Fluid fluid = content.getFluid().getFluid();
//        int fluidAvailable = content.getFluidAmount();
//        int fluidTransferred = 0;
//
//        int count = peripherals.bottom.size();
//        int toFill = Math.min(Math.floorDiv(fluidAvailable, count), TileEntityConduit.TRANSFER_RATE);
//        int remainder = fluidAvailable % count;
//        for (Connection c : peripherals.bottom)
//        {
//            int f = toFill;
//            if(remainder > 0 && toFill < TileEntityConduit.TRANSFER_RATE)
//            {
//                f++;
//                remainder--;
//            }
//            fluidTransferred += c.fluidHandler.fill(new FluidStack(fluid, f), FluidAction.EXECUTE);
//        }
//
//        fluidAvailable -= fluidTransferred;
//        if(fluidAvailable <= 0)
//        {
//            content.drain(fluidTransferred, FluidAction.EXECUTE);
//            return fluidTransferred;
//        }
//
//
//        count = peripherals.side.size();
//        for (Connection c : peripherals.side)
//        {
//
//        }
//
//        return 0;
//    }

    protected int transferTo(Set<Connection> connections, FluidStack fluid)
    {
        int fluidAvailable = fluid.getAmount();
        int fluidTransferred = 0;

        int count = connections.size();
        int toFill = Math.min(Math.floorDiv(fluidAvailable, count), TileEntityConduit.TRANSFER_RATE);
        int remainder = fluidAvailable % count;
        for (Connection c : connections)
        {
            int f = toFill;
            if(remainder > 0 && toFill < TileEntityConduit.TRANSFER_RATE)
            {
                f++;
                remainder--;
            }
            fluidTransferred += c.fluidHandler.fill(new FluidStack(fluid, f), FluidAction.EXECUTE);
        }

        return fluidTransferred;
    }

//    protected int transferUp()
//    {
//
//        return 0;
//    }

    /*internal fluid accessors*/
    public FluidStack getFluidPerConduit() { return new FluidStack(content.getFluid(), Math.floorDiv(content.getFluidAmount(), conduits.size())); }

    public boolean isFluidValid(FluidStack fluid) { return content.isFluidValid(fluid); }

    public int fill(FluidStack resource, FluidAction action)
    {
        //if grid is full, try to fill tanks above
        if(content.getFluid().getAmount() >= content.getCapacity())
            return transferTo(peripherals.top, content.getFluid());
        return content.fill(resource, action);
    }

    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        return content.drain(resource, action);
    }

    public FluidStack drain(int maxDrain, FluidAction action)
    {
        return content.drain(maxDrain, action);
    }

    /*grid accessors*/
    public UUID getId() { return id; }

    public void recalculateCapacity() { content.setCapacity(conduits.size() * TileEntityConduit.CAPACITY); }

    public boolean addConduit(IFluidConduit conduit)
    {
        if(removed) { MainClass.LOGGER.error("Trying to connect a conduit to removed grid with id: " + id); return false; }
        boolean flag = conduits.add(conduit);
        if(flag) recalculateCapacity();
        return flag;
    }

    public boolean removeConduit(IFluidConduit conduit)
    {
        boolean result = conduits.remove(conduit);
        if(conduits.isEmpty()) destroy();
        else
        {
            if(result) peripherals.removeByParent(conduit);
            recalculateCapacity();
        }
        return result;
    }

    public boolean mergeWith(Grid grid)
    {
        if(!isFluidValid(grid.getFluidPerConduit())) return false;

        for (IFluidConduit conduit : conduits)
        {
            grid.addConduit(conduit);
            conduit.setGrid(grid);
        }
        grid.addPeripherals(peripherals);
        destroy();
        return true;
    }

    public boolean addPeripherals(GridPeripheralsHolder peripherals)
    {
        return peripherals.mergeWith(peripherals);
    }

    public boolean addPeripherals(Collection<Connection> connections)
    {
        return peripherals.add(connections);
    }

    public boolean isRemoved() { return removed; }

    public void destroy()
    {
        //TODO unsubscribe all conduits ?
        conduits.clear();
        peripherals.clear();
        MainClass.gridsManager.unregisterGrid(this);
        removed = true;
    }
}
