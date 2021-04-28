package techeart.htu.objects.pipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class PipeConnectionFluid
{
    public TileEntity tile;
    public IFluidHandler fluidHandler;
    public Direction side;

    public PipeConnectionFluid(TileEntity tile, IFluidHandler fluidHandler, Direction side)
    {
        this.tile = tile;
        this.fluidHandler = fluidHandler;
        this.side = side;
    }

    public boolean isPipe() { return tile instanceof IPipe; }
}
