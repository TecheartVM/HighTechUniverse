package techeart.htu.objects.conduits.grids;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techeart.htu.objects.conduits.pipe.FluidConduit;
import techeart.htu.utils.ModUtils;
import techeart.htu.utils.world.HTUGridManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class FluidGrid extends Grid<FluidConduit> {
    protected FluidTank pipesContent = new FluidTank(0);
    protected boolean needFlow = false;

    public boolean canDD = true;
    UUID latestFiller =  null;

/*TODO: (Known bugs)
   1. Jumping fluid Level (Bug!)
   2. Mekanism pipes cooperation (Tank -> FluidConduit -> Pipe -> Tank)
   3. Columns aligning
   4. Horizontal machines (or only Mekanism) fluid exporting
   5. Fluid amount lower then was on world restart... sometimes...
 */

    public FluidGrid() {
        this.id = UUID.randomUUID();
        HTUGridManager.SELF.registerGrid(this);
        System.out.println("New GRID has been created! id="+id);
    }

    @Override
    public void HTUTick() {
        if(pipesContent.isEmpty()) return;

        drainDown();
        horizontalActions();
    }

    public void MergeWith(FluidGrid grid) {
        System.out.println(this.id + " starts merging with"+grid.id);
        for(GridComponent<FluidConduit> component : components)
            component.getConduit().setGrid(grid);


        if(!this.pipesContent.isEmpty() && grid.getContent().isFluidValid(this.pipesContent.getFluid())) {
            int filled = grid.fill(this.pipesContent.getFluid(), IFluidHandler.FluidAction.EXECUTE, null);
            System.out.println("\n\t\tFilled on MERGE!\nAmount="+filled);
        }
        else
            System.out.println("Merging: Current fluid not valid or grid isEmpty!"); //TODO: FIX!
        grid.setFlow();
        remove();
    }

    protected ArrayList<GridComponent<FluidConduit>> prepareNeighbours(GridComponent<FluidConduit> component) {
        ArrayList<GridComponent<FluidConduit>> tmp = new ArrayList<>();
        for (TileEntity te : component.getHorizontalNeighbours())
            if (te instanceof FluidConduit)
                tmp.add(getComponent((FluidConduit) te));
        tmp.removeIf(t -> t == null || t.componentData >= component.componentData);
        tmp.sort(Comparator.comparingInt(o -> o.componentData));
        return tmp;
    }

    protected void stopFlow()
    {
        int flowInt = pipesContent.getFluidAmount() / components.size();
        int drops = pipesContent.getFluidAmount() - (flowInt * components.size());

        for (GridComponent<FluidConduit> component : components) {
            component.componentData = flowInt;
            if (drops > 0) {
                component.componentData += 1;
                drops -= 1;
            }
        }
        needFlow = false;
    }

    protected void horizontalActions() {
        if (needFlow)
            for(GridComponent<FluidConduit> component: components) {
                if (flowFluid(component)) {
                    stopFlow();
                    break;
                }
            }

        components.forEach(component -> {
            for(int i=0; i<4;i++) {
                Direction dir = Direction.byHorizontalIndex(i);
                TileEntity te = component.getNeighbourByDirection(dir);
                if(te == null) continue;
                IFluidHandler fh = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,dir.getOpposite()).orElse(null);
                if(fh != null && !(te instanceof FluidConduit ))
                    drain(te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null).fill(drain(component, IFluidHandler.FluidAction.SIMULATE),IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE,component);
            }
        });
    }

    protected boolean flowFluid(GridComponent<FluidConduit> component) {

        ArrayList<GridComponent<FluidConduit>> horizontalNeighbours = prepareNeighbours(component);

        int tmp = 0;
        if (horizontalNeighbours.size() <= 0) {
            return component == components.get(components.size() - 1);
        }

        boolean needDrop = false;
        for (GridComponent<FluidConduit> p : horizontalNeighbours)
            tmp = tmp + (component.componentData - p.componentData);

        if (tmp / horizontalNeighbours.size() != Math.round((float) tmp / horizontalNeighbours.size()))
            needDrop = true;
        tmp = Math.round((float) tmp / horizontalNeighbours.size());
        if (tmp == 1)
            return true;


        if (tmp == horizontalNeighbours.size() || needDrop) {
            component.componentData -= 1;
            horizontalNeighbours.get(0).componentData += 1;
        }

        int tmp0 = tmp / (horizontalNeighbours.size() + 1);
        tmp = Math.min(tmp0, FluidConduit.TRANSFER_RATE);

        component.componentData -= (tmp * horizontalNeighbours.size());

        for (GridComponent<FluidConduit> p : horizontalNeighbours)
            p.componentData += tmp;
        return false;
    }

    protected void drainDown() {
        int totallyDrained=0;

        for(GridComponent<FluidConduit> component : components) {
            TileEntity belowNeighbour = component.neighbours.get(Direction.DOWN);

            if(belowNeighbour instanceof FluidConduit || belowNeighbour == null) continue;

            IFluidHandler fh = belowNeighbour.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,Direction.UP).orElse(null);
            if (pipesContent.getFluidAmount() < components.size())
                pipesContent.drain(fh.fill(pipesContent.getFluid(), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);

            FluidStack fs = drain(fh.fill(drain(component, IFluidHandler.FluidAction.SIMULATE),IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE,component);
            totallyDrained +=fs.getAmount();

            if(pipesContent.isEmpty()) return;
        }

        if(!canDD)
            return;

        for(GridComponent<FluidConduit> component : components){
            TileEntity belowNeighbour = component.neighbours.get(Direction.DOWN);

            if(belowNeighbour == null) continue;

            IFluidHandler fh = belowNeighbour.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,Direction.UP).orElse(null);
            if (pipesContent.getFluidAmount() < components.size())
                pipesContent.drain(fh.fill(pipesContent.getFluid(), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);

            FluidStack fs = drain(fh.fill(drain(component, IFluidHandler.FluidAction.SIMULATE),IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.EXECUTE,component);

            totallyDrained+=fs.getAmount();

            if(pipesContent.isEmpty()) return;
        }

        if(totallyDrained==0)
            this.canDD = false;
    }



    /*TankMethods*/
    public FluidTank getContent() {
        return pipesContent;
    }

    protected void recalculateTank()
    {
        this.pipesContent.setCapacity(FluidConduit.MAX_CAPACITY*components.size());
    }

    @Override
    protected void onComponentRemove(FluidConduit conduit,GridComponent<FluidConduit> component) {
        if(component.componentData !=0) {
            if (component.componentData == FluidConduit.MAX_CAPACITY)
                Objects.requireNonNull(conduit.getWorld()).setBlockState(conduit.getPos(), pipesContent.getFluid().getFluid().getDefaultState().getBlockState());

            this.pipesContent.drain(component.componentData, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    protected void onComponentsChanged(GridComponent<FluidConduit> component) {
        this.recalculateTank();
        this.setFlow();
    }

    @Override
    protected void onGridRemove() {
        HTUGridManager.SELF.unregisterGrid(this);
        System.out.println("grid "+this.id+ " unregistered");
    }

    public void setFlow() {
        this.needFlow = true;
    }

    public final void forceFill(FluidStack previousFS, FluidConduit fluidConduit) {
        getComponent(fluidConduit).componentData = pipesContent.fill(previousFS, IFluidHandler.FluidAction.EXECUTE);
        System.out.println("\n\t\tforceFill!\nAmount="+previousFS.getAmount()+"Conduit pos="+fluidConduit.getPos().getCoordinatesAsString());

    }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action, FluidConduit conduit) {
        if(!resource.isEmpty()) {
            Integer[] toCompare = new Integer[]{resource.getAmount(), FluidConduit.TRANSFER_RATE};
            if(conduit!= null && conduit.getSpace() > 0)
                toCompare = new Integer[]{resource.getAmount(), FluidConduit.TRANSFER_RATE, conduit.getSpace()};
            resource.setAmount(ModUtils.Min(toCompare));
        }

        int result = pipesContent.fill(resource, action);

        if(action.execute()) {
            if (conduit != null)
                conduit.getComponent().componentData += result;

            System.out.println("\n\t\tFILLED!\nAmount="+result+"\nID="+this.id);

            if(result !=0)
                this.setFlow();

            if(this.pipesContent.getCapacity()==0)
                this.canDD = false;

        }

        if(!resource.isEmpty()) {
            resource.setAmount(resource.getAmount() - result);
            if (!resource.isEmpty())
                result += onFill(resource, action, conduit);
        }

        return result;
    }


    protected int onFill(FluidStack resource, IFluidHandler.FluidAction action,FluidConduit conduit)
    {
        ArrayList<IFluidHandler> fhList = new ArrayList<>();
        ArrayList<FluidConduit> fgList = new ArrayList<>();

        for(GridComponent<FluidConduit> component : components)
        {
            TileEntity te = component.getNeighbourByDirection(Direction.UP);
            if (te == null) continue;

            if(te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.DOWN).isPresent())
                if(te instanceof FluidConduit)
                    fgList.add((FluidConduit)te);
                else
                    fhList.add(te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.DOWN).orElse(null));
        }

        int perObject;
        int result =0;

        if(!fhList.isEmpty()) {
            for(IFluidHandler fh : fhList)
            {
                perObject = resource.getAmount() / fhList.size();
                result += fh.fill(new FluidStack(resource.getFluid(),perObject),action);
            }
        }

        if(result == resource.getAmount()) return result;

        if(!fgList.isEmpty()){
            for(FluidConduit con : fgList)
            {
                perObject = resource.getAmount() / fgList.size();
                result += con.fill(new FluidStack(resource.getFluid(),perObject),action);
            }
        }

        String s = (action == IFluidHandler.FluidAction.EXECUTE) ? "EXECUTE" : "SIMULATE";
        System.out.println("\n\t\tON_FILLED!\nAmount="+result+"\t"+s+"\nID="+this.id);

        return result;
    }

    @Nonnull
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action,FluidConduit conduit) {
        if(pipesContent.isFluidValid(resource))
            return drain(resource.getAmount(), action, conduit.getComponent());

        return FluidStack.EMPTY;
    }

    @Nonnull
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action,GridComponent<FluidConduit> component)
    {
        FluidStack result = pipesContent.drain(ModUtils.Min(new Integer[]{component.componentData,maxDrain,FluidConduit.TRANSFER_RATE}),action);
        if(action.execute() && !result.isEmpty()) {
            component.componentData -=result.getAmount();
            this.setFlow();

            System.out.println("\n\t\tDRAINED!\nAmount="+result.getAmount()+"\nID="+this.id);

            updateNeighbourDD();
        }
        return result;
    }

    private void updateNeighbourDD(){
        for (GridComponent<FluidConduit> component: components) {
            TileEntity te = component.getNeighbourByDirection(Direction.UP);
            if(te instanceof FluidConduit)
                ((FluidConduit)te).getGrid().canDD = true;
        }
    }

    @Nonnull
    protected FluidStack drain(GridComponent<FluidConduit> component, IFluidHandler.FluidAction action) {
        return drain(Integer.MAX_VALUE,action,component);
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);
        pipesContent.fill(FluidStack.loadFluidStackFromNBT(nbt), IFluidHandler.FluidAction.EXECUTE);
        needFlow = nbt.getBoolean("needFlow");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        pipesContent.getFluid().writeToNBT(compound);
        compound.putBoolean("needFlow",needFlow);
        return super.write(compound);
    }
}
