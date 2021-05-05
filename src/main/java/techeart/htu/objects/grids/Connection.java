package techeart.htu.objects.grids;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class Connection
{
    public final TileEntity tile;
    public final IFluidHandler fluidHandler;
    public final IFluidConduit parent;
    public final Direction side;

    public Connection(TileEntity tile, IFluidHandler fluidHandler, IFluidConduit parent, Direction side)
    {
        this.tile = tile;
        this.fluidHandler = fluidHandler;
        this.parent = parent;
        this.side = side;
    }

    public boolean isConduit() { return tile instanceof IFluidConduit; }
}
