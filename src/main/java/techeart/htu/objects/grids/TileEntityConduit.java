package techeart.htu.objects.grids;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction8;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techeart.htu.MainClass;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileEntityConduit extends TileEntity implements IFluidConduit, IFluidHandler//, ITickableTileEntity
{
    public static int CAPACITY = FluidAttributes.BUCKET_VOLUME;
    public static int TRANSFER_RATE = 125;

    private final FluidTank tank = new FluidTank(CAPACITY);

    protected UUID gridId;
    protected Grid grid;

    public int fluidAmount = 0;

    public TileEntityConduit() { this(RegistryHandler.FLUID_PIPE_TE.get()); }

    public TileEntityConduit(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
        tank.setValidator((fluid) -> tank.isEmpty() || fluid.isFluidEqual(tank.getFluid()));
    }

    /*grid interaction*/
    public void connectOrCreateGrid()
    {
        //check all neighbour tiles
        Set<Connection> peripherals = new HashSet<>();
        for (Connection c : connections.values())
        {
            if(!c.isConduit())
            {
                peripherals.add(c);
                continue;
            }
            TileEntityConduit conduit = (TileEntityConduit) c.tile;
            if(grid == null)
            {
                if (conduit.grid.addConduit(this))
                    setGrid(conduit.grid);
            }
            else grid.mergeWith(conduit.grid);
        }

        //if grid doesn't exist, create it
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

        updateConnections();
        Set<Connection> peripherals = new HashSet<>();
        for (Connection c : connections.values())
        {
            if(!c.isConduit())
            {
                peripherals.add(c);
                continue;
            }
            if(((TileEntityConduit) c.tile).grid != newGrid)
                ((TileEntityConduit) c.tile).onGridReconstruction(newGrid);
        }
        grid.addPeripherals(peripherals);
    }

    public void setGrid(Grid grid)
    {
        this.grid = grid;
        gridId = grid.getId();
    }

    public Grid getGrid() { return grid; }

    protected EnumMap<Direction, Connection> connections = new EnumMap(Direction.class);
    public Set<Connection> getConnections()
    {
        Set<Connection> s = new HashSet<>();
        s.addAll(connections.values());
        return s;
    }

    public void updateConnections()
    {
        connections.clear();
        for (Direction dir : Direction.values())
        {
            TileEntity te = world.getTileEntity(pos.offset(dir));
            if (te == null || te.isRemoved()) continue;

            LazyOptional<IFluidHandler> lo = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
            if (!lo.isPresent()) continue;

            IFluidHandler fh = lo.orElse(null);

            connections.put(dir, new Connection(te, fh, this, dir));
        }
    }

    int timer = 0;
    protected Map<Direction, Integer> dirsToSources = new HashMap<>();
    protected int transferToNeighbours()
    {
        int fluidAvailable = tank.getFluidAmount();
        int averageAmount = 0;

        Set<Connection> conduits = new HashSet<>();
        Set<Connection> externals = new HashSet<>();
        Connection bottomConnection = null;
        for (Connection c : connections.values())
        {
            if(c.side == Direction.UP) continue;
            if(c.side == Direction.DOWN)
            {
                bottomConnection = c;
                continue;
            }
            if(!world.getChunkProvider().canTick(c.tile.getPos()))
            {
                MainClass.LOGGER.warn("Found a conduit in unloaded chunk at position " + c.tile.getPos().getX() + " " + c.tile.getPos().getY() + " " + c.tile.getPos().getZ());
                continue;
            }

            if(c.isConduit())
            {
                //accepting only horizontal conduit connections with free space for required fluid in internal tank
                if(c.fluidHandler.fill(new FluidStack(tank.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE) > 0)
                {
                    conduits.add(c);
                    averageAmount += c.fluidHandler.drain(new FluidStack(tank.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE).getAmount();
                }
            }
            else
            {
                externals.add(c);
            }
        }

        if(bottomConnection != null)
        {
            int toFill = Math.min(TRANSFER_RATE, fluidAvailable);
            int filled;
            if(bottomConnection.isConduit()) filled = ((TileEntityConduit)bottomConnection.tile).fill(new FluidStack(tank.getFluid(), toFill), FluidAction.EXECUTE, Direction.UP);
            else filled = bottomConnection.fluidHandler.fill(new FluidStack(tank.getFluid(), toFill), FluidAction.EXECUTE);
            fluidAvailable -= filled;
            //System.out.println("Transferred to bottom tile: " + (tank.getFluidAmount() - fluidAvailable));
            if(fluidAvailable <= 0) return tank.getFluidAmount();
        }

        if(!externals.isEmpty())
        {
            int toExternals = Math.min(Math.floorDiv(fluidAvailable, externals.size()), TRANSFER_RATE);
            //transferring fluid to non-conduit tiles
            for (Connection c : externals)
            {
                int filled = c.fluidHandler.fill(new FluidStack(tank.getFluid(), toExternals), FluidAction.EXECUTE);
                fluidAvailable -= filled;
                if(fluidAvailable <= 0) return tank.getFluidAmount();
            }
        }

        //trying to equalize fluid amount in connected conduits
        if(!conduits.isEmpty())
        {
            averageAmount += fluidAvailable;
            averageAmount = Math.floorDiv(averageAmount, conduits.size() + 1);

            //System.out.println(averageAmount);

            for (Connection c : conduits)
            {
                //operating connection only if it is not at source direction
                if(!dirsToSources.containsKey(c.side))
                {
                    //getting fluid amount in connected conduit
                    int toFill = c.fluidHandler.drain(new FluidStack(tank.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE).getAmount();

                    //System.out.println("Fluid in neighbour: " + toFill + " In this pipe: " + tank.getFluid().getAmount());
                    //System.out.println("Average amount: " + averageAmount);

                    //getting amount of fluid we need to send to reach the average amount
                    toFill = averageAmount - toFill;
                    if(toFill > 0)
                    {
                        //getting amount, restricted by transfer rate
                        toFill = Math.min(toFill, TRANSFER_RATE);
                        //toFill = Math.min(toFill, fluidAvailable);
                        //using special conduit fill method to provide information about fluid source location
                        int filled = ((TileEntityConduit)c.tile).fill(new FluidStack(tank.getFluid(), toFill), FluidAction.EXECUTE, c.side.getOpposite());
                        fluidAvailable -= filled;
                        if(fluidAvailable <= 0) return tank.getFluidAmount();
                    }
                }
                //else System.out.println("Can't fill source!");
            }
        }

        //System.out.println("Transferred " + (tank.getFluidAmount() - fluidAvailable) + " of " + tank.getFluidAmount());
        return tank.getFluidAmount() - fluidAvailable;
    }

    protected int transferUp(FluidStack resource, FluidAction action)
    {
        if(resource.isEmpty() || !tank.isFluidValid(resource)) return 0;
        if(!connections.containsKey(Direction.UP)) return 0;
        if(dirsToSources.containsKey(Direction.UP)) return 0;

        return connections.get(Direction.UP).fluidHandler.fill(resource, action);
    }

    protected int fill(FluidStack resource, FluidAction action, Direction dirToSource)
    {
        //if map has such direction, resetting the last update time
        //else put new direction
        dirsToSources.put(dirToSource, 0);

        return tank.fill(resource, action);
    }

    /*TileEntity*/
    public void tick()
    {
        if(getWorld().isRemote) return;

       // System.out.println("Fluid amount: " + fluidAmount);

//        if(timer < 2)
//        {
//            timer++;
//            return;
//        }
//        else timer = 0;

        //removing all inactive source directions
        if(!dirsToSources.isEmpty())
        {
            Set<Direction> toRemove = new HashSet<>();
            for (Map.Entry<Direction, Integer> e : dirsToSources.entrySet())
            {
                e.setValue(e.getValue() + 1);
                if (e.getValue() > 1) toRemove.add(e.getKey());
            }
            toRemove.forEach(dir -> dirsToSources.remove(dir));
        }
        //else System.out.println("No sources in system");

        if(tank.isEmpty()) return;

        //System.out.println("Fluid in pipe: " + tank.getFluidAmount());

        int toDrain = 0;
        //System.out.println("Transferring down");
        //toDrain += transferDown();
        //System.out.println("Transferring down END");
//        if(toDrain < tank.getFluidAmount())
//        {
//            //System.out.println("Transferring side");
//            toDrain += transfer();
//            //System.out.println("Transferring side END");
//        }

        toDrain += transferToNeighbours();
        //System.out.println("Transferred: " + toDrain);

        if(toDrain > 0)
            drain(toDrain, FluidAction.EXECUTE);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        super.write(nbt);
        tank.writeToNBT(nbt);
        //nbt.putUniqueId("GridId", gridId);
        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        tank.readFromNBT(nbt);
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
        if(grid != null) grid.removeConduit(this);
        super.remove();
        //resetting grid for all of connected conduits
        for (Connection c : connections.values())
        {
            if(!c.isConduit()) continue;
            ((TileEntityConduit)c.tile).onGridReconstruction(new Grid());
        }
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
        //System.out.println("Filling");

        //int result = tank.fill(resource, action);
        int result = Math.min(resource.getAmount(), CAPACITY - fluidAmount);
        fluidAmount += result;

        //System.out.println("Filled");
        //if(result <= 0) result = transferUp(resource, action);
        //syncClient();
        return result;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        //return grid == null ? FluidStack.EMPTY : grid.drain(resource, action);
        //syncClient();
        return tank.drain(resource, action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        //return grid == null ? FluidStack.EMPTY : grid.drain(maxDrain, action);
        //syncClient();
        return tank.drain(maxDrain, action);
    }

    /*temp*/
    public void syncClient()
    {
        if(this.removed) return;
        if(getWorld() == null) return;
        if(getWorld().isRemote) return;

        SUpdateTileEntityPacket pkt = getUpdatePacket();
        ((ServerWorld) getWorld()).getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false).forEach(p -> {
            if(pkt != null)
                p.connection.sendPacket(pkt);
        });
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
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        if(removed || world == null) return;
        read(this.world.getBlockState(pkt.getPos()), pkt.getNbtCompound());
    }
}
