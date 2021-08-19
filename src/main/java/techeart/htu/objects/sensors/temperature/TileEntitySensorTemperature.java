package techeart.htu.objects.sensors.temperature;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import techeart.htu.objects.HTUTileEntity;
import techeart.htu.objects.wrench.IWrenchTarget;
import techeart.htu.utils.HTUHooks;
import techeart.htu.utils.LocalizedMessages;
import techeart.htu.utils.ModUtils;
import techeart.htu.utils.registration.RegistryHandler;
import techeart.htu.utils.temperature.ITemperatureHandler;

import javax.annotation.Nullable;

public class TileEntitySensorTemperature extends HTUTileEntity implements ITickableTileEntity, IWrenchTarget
{
    //max and min temperatures, supported by this sensor
    public static int MIN_TEMPERATURE = -39;
    public static int MAX_TEMPERATURE = 356;

    //current measurement limits
    private int minTemperature = -30;
    private int maxTemperature = 50;

    private ITemperatureHandler measurementTarget = null;
    private int power = 0;
    private int prevTemperature = 0;
    private float indicatorTargetHeight = 0;
    private float indicatorHeight = 0;

    public TileEntitySensorTemperature() { super(RegistryHandler.SENSOR_TEMPERATURE_TE.get()); }

    public boolean updateTarget()
    {
        if(world == null || world.isRemote) return false;

        Direction wallDir = getBlockState().get(BlockSensorTemperature.FACING).getOpposite();
        TileEntity te = world.getTileEntity(pos.offset(wallDir));
        if(te instanceof ITemperatureHandler)
        {
            measurementTarget = (ITemperatureHandler) te;
            return true;
        }

        measurementTarget = null;
        return false;
    }

    public float getLerpedIndicatorHeight()
    {
        float result = MathHelper.lerp(0.1f, indicatorHeight, indicatorTargetHeight);
        indicatorHeight = result;
        return result;
    }
    public void updateIndicator()
    {
        int minTemperature = getMinTemperature();
        indicatorTargetHeight = MathHelper.clamp((getTemperature() - minTemperature) / (float)(getMaxTemperature() - minTemperature), 0, 1);
        syncClient();   //TODO optimization
    }

    public int getPowerLevel() { return power; }
    public void updatePowerLevel()
    {
        if(measurementTarget == null)
        {
            power = 0;
            return;
        }
        int temperature = measurementTarget.getTemperature();
        int minTemperature = getMinTemperature();
        int maxTemperature = getMaxTemperature();
        power = MathHelper.clamp(Math.floorDiv((temperature - minTemperature) * 15, maxTemperature - minTemperature), 0, 15);
        ((BlockSensorTemperature)getBlockState().getBlock()).notifyNeighbours(world, pos);
    }

    public int getMeasured() { return MathHelper.clamp(getTemperature(), getMinTemperature(), getMaxTemperature()); }

    public int getMinTemperature() { return minTemperature; }
    public int getMaxTemperature() { return maxTemperature; }
    public int getTemperature()
    {
        return measurementTarget == null
                ? HTUHooks.getAmbientTemperature(world, pos.offset(getBlockState().get(BlockSensorTemperature.FACING).getOpposite()))
                : measurementTarget.getTemperature();
    }

    /*HTUTileEntity*/
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        super.onPlaced(world, pos, state, placer, stack);
        updateTarget();
        updatePowerLevel();
    }

    @Override
    public void onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
    {
        super.onBlockActivated(state, world, pos, player, hand, hit);

        if(player.getHeldItem(hand).isEmpty())
        {
            int temp = getMeasured();
            ModUtils.playerInfoMessage(LocalizedMessages.getInfoTemperature(temp), player);
        }
    }

    /*TileEntity*/
    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        int min = nbt.getInt("MinTemp");
        int max = nbt.getInt("MaxTemp");
        setMeasurementLimits(min, max);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        super.write(nbt);
        nbt.putInt("MinTemp", minTemperature);
        nbt.putInt("MaxTemp", maxTemperature);
        return nbt;
    }

    /*ITickableTileEntity*/
    @Override
    public void tick()
    {
        if(world.isRemote) return;

        int temperature = getTemperature();
        if(temperature != prevTemperature)
        {
            updatePowerLevel();
            updateIndicator();
            prevTemperature = temperature;
        }
    }

    /*network*/
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
        nbt.putFloat("IndicatorHeight", indicatorTargetHeight);
        nbt.putInt("MinTemp", minTemperature);
        nbt.putInt("MaxTemp", maxTemperature);
        return new SUpdateTileEntityPacket(this.pos, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        if(removed || world == null) return;
        CompoundNBT nbt = pkt.getNbtCompound();
        indicatorTargetHeight = nbt.getFloat("IndicatorHeight");
        int min = nbt.getInt("MinTemp");
        int max = nbt.getInt("MaxTemp");
        setMeasurementLimits(min, max);
    }

    public void setMeasurementLimits(int minTemp, int maxTemp)
    {
        minTemperature = MathHelper.clamp(minTemp, MIN_TEMPERATURE, MAX_TEMPERATURE - 1);
        maxTemperature = MathHelper.clamp(maxTemp, MIN_TEMPERATURE + 1, MAX_TEMPERATURE);
        if(world != null && !world.isRemote())
        {
            updateTarget();
            updatePowerLevel();
            updateIndicator();
            markDirty();
        }
    }

    @Override
    public void setWorldAndPos(World world, BlockPos pos)
    {
        super.setWorldAndPos(world, pos);
        if(!world.isRemote()) syncClient();
    }

    /*IWrenchTarget*/
    @Override
    public boolean onWrenchUsed(boolean sneak)
    {
        if(sneak) return false;

        if(world.isRemote())
            Minecraft.getInstance().displayGuiScreen(new GuiSensorTemperatureConfig(this));
        return true;
    }
}

