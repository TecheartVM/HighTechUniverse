package techeart.htu.objects.sensors;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nullable;

public class TileEntitySensorFluidLevel extends TileEntity implements ITickableTileEntity
{
    private IFluidHandler measurementTarget = null;
    private int currentTankIndex = 0;

    private int prevFluidAmount = 0;

    private int power = 0;
    private int dialPhase = 0;

    public TileEntitySensorFluidLevel()
    {
        super(RegistryHandler.SENSOR_FLUID_LEVEL_TE.get());
    }

    public boolean updateTarget()
    {
        if(world == null || world.isRemote) return false;

        //TODO: replace this ?
        try
        {
            TileEntity te = world.getTileEntity(pos.down());
            LazyOptional<IFluidHandler> lo = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
            measurementTarget = lo.orElse(null);
            return true;
        }
        catch(NullPointerException e)
        {
            measurementTarget = null;
            return false;
        }
    }

    public int getFluidInTarget()
    {
        if(measurementTarget == null) return 0;
        //TODO add measuring tank switch ability
        return measurementTarget.getFluidInTank(currentTankIndex).getAmount();
    }

    public int getPowerLevel() { return power; }
    public void updatePowerLevel()
    {
        if(measurementTarget == null) power = 0;
        int fluid = measurementTarget.getFluidInTank(currentTankIndex).getAmount();
        int capacity = measurementTarget.getTankCapacity(currentTankIndex);
        power = (int) Math.ceil(fluid * 15 / (float)capacity);
    }

    public int getDialPhase() { return dialPhase; }
    public void updateDialPhase()
    {
        int prevPhase = dialPhase;

        if(measurementTarget == null) dialPhase = 0;
        int fluid = measurementTarget.getFluidInTank(currentTankIndex).getAmount();
        int capacity = measurementTarget.getTankCapacity(currentTankIndex);
        dialPhase = (int) Math.ceil(fluid * 5 / (float)capacity);

        if(prevPhase != dialPhase) syncClient();
    }

    @Override
    public void tick()
    {
        if(world.isRemote || measurementTarget == null) return;

        int fluidLevel = measurementTarget.getFluidInTank(currentTankIndex).getAmount();
        if(fluidLevel != prevFluidAmount)
        {
            updatePowerLevel();
            updateDialPhase();
            ((BlockSensorFluidLevel)getBlockState().getBlock()).notifyNeighbours(world, pos);
            prevFluidAmount = fluidLevel;
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        updateTarget();
    }

    public void syncClient()
    {
        if(this.removed) return;
        if(getWorld() == null || getWorld().isRemote) return;

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
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("DialPhase", dialPhase);
        return new SUpdateTileEntityPacket(this.pos, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        if(removed || world == null) return;
        CompoundNBT nbt = pkt.getNbtCompound();
        dialPhase = nbt.getInt("DialPhase");
    }
}
