package techeart.htu.objects.conduits.pipe;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import techeart.htu.objects.conduits.AbstractConduit;
import techeart.htu.objects.conduits.grids.FluidGrid;
import techeart.htu.objects.conduits.grids.Grid;
import techeart.htu.objects.conduits.grids.GridComponent;
import techeart.htu.utils.ModUtils;
import techeart.htu.utils.registration.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class FluidConduit extends AbstractConduit implements IFluidHandler {
/*TODO: (Known bugs)
   1. Add steam (and other gases) transport
   2. Fluid amount lower then was on world restart... sometimes...
   3. Wrench configuration
 */

    public static final int MAX_CAPACITY = FluidAttributes.BUCKET_VOLUME;
    public static final int TRANSFER_RATE = MAX_CAPACITY/8;

    private FluidStack PreviousFS = null;

    public FluidConduit() {
        super(RegistryHandler.FLUID_PIPE_TE.get());
    }
    public FluidConduit(boolean isNew)
    {
        super(RegistryHandler.FLUID_PIPE_TE.get(),isNew);
    }

    @Override
    public FluidGrid getGrid() {
        return (FluidGrid) grid;
    }

    @Override
    public <Conduit extends AbstractConduit> void setGrid(Grid<Conduit> grid) {
        this.grid = grid;
        this.getGrid().addComponent(this);
    }

    @Override
    protected <Conduit extends AbstractConduit, GridObject extends Grid<Conduit>> void onReplacinGrid(Grid<? extends AbstractConduit> currentGrid, GridObject newGrid, AbstractConduit conduit) {
        if(currentGrid ==null ) return;
        FluidStack fs = new FluidStack(((FluidGrid)currentGrid).getContent().getFluid(),currentGrid.getComponent(this).getComponentData());
        ((FluidGrid)newGrid).fill(fs,FluidAction.EXECUTE,(FluidConduit) conduit);

        System.out.println("\n\t\tonReplacinGrid!\nAmount="+fs.getAmount()+"\nID="+this.grid.id+"\tReplaced with="+currentGrid.id);
    }

    public GridComponent<FluidConduit> getComponent()
    {
        return this.getGrid().getComponent(this);
    }

    @Override
    public void newGrid() {
        ArrayList<FluidConduit> neighbours = getNeighbours();
        for (FluidConduit te : neighbours) {
            FluidGrid teGrid = te.getGrid();
            if(teGrid == null) continue;
            if (this.grid == null)
                this.setGrid(teGrid);
            else if (this.grid != teGrid)
                this.getGrid().MergeWith(teGrid);
        }

        if(this.grid == null) {
            this.setGrid(new FluidGrid());
        }
    }

    public int getSpace(){
        return MAX_CAPACITY - getComponent().componentData;
    }

    @Override
    protected void onConduitLoad() {
        if(!hasWorld()) return;

        this.getGrid().forceFill(PreviousFS,this);

        getComponent().updateNeighbour(Direction.DOWN,this.world.getTileEntity(ModUtils.getPosByDir(pos,Direction.DOWN)));
        getComponent().updateNeighbour(Direction.UP,this.world.getTileEntity(ModUtils.getPosByDir(pos,Direction.UP)));

        PreviousFS = null;
    }

    @Override
    protected void onConduitRead(CompoundNBT nbt) {
        this.PreviousFS = FluidStack.loadFluidStackFromNBT(nbt);
    }

    @Override
    protected void onConduitWrite(CompoundNBT compound) {
        FluidStack fs = new FluidStack(this.getGrid().getContent().getFluid().getFluid(), this.getComponent().getComponentData());
        fs.writeToNBT(compound);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));

        return super.getCapability(cap, side);
    }

    @Override
    public int getTanks() {
        if(getGrid() !=null)
            return getGrid().getContent().getTanks();
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        if(getGrid() !=null)
            return getGrid().getContent().getFluidInTank(tank);
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        if(getGrid() !=null)
            return getGrid().getContent().getTankCapacity(tank);
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        if(getGrid() !=null)
            return getGrid().getContent().isFluidValid(tank,stack);
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if(getGrid() !=null)
            return this.getGrid().fill(resource, action, this);
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if(getGrid() !=null) {
            if(resource.isEmpty() && action.execute())
                System.out.println("GRID="+getGrid().getID()+" drained by="+resource.getAmount() +" Conduit#1");
            return getGrid().drain(resource, action, this);
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if(getGrid() !=null) {
            if(maxDrain>0 && action.execute())
                System.out.println("GRID="+this.getGrid()+" drained by="+maxDrain +" Drain#1");
            return getGrid().drain(maxDrain, action, this.getComponent());
        }
        return FluidStack.EMPTY;
    }
}
