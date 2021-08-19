package techeart.htu.utils.network.packets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import techeart.htu.objects.sensors.temperature.TileEntitySensorTemperature;
import techeart.htu.utils.WorldUtils;

import java.util.function.Supplier;

public class PacketSensorTemperatureConfig
{
    private final BlockPos pos;
    private final int minTemp;
    private final int maxTemp;

    public PacketSensorTemperatureConfig(BlockPos pos, int minTemp, int maxTemp)
    {
        this.pos = pos;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public static void handle(PacketSensorTemperatureConfig pkt, Supplier<Context> context)
    {
        Context c = context.get();
        c.enqueueWork(() -> {
            PlayerEntity player = c.getSender();
            if(player != null)
            {
                TileEntity te = WorldUtils.getTileEntity(player.world, pkt.pos);
                if(te instanceof TileEntitySensorTemperature)
                {
                    ((TileEntitySensorTemperature) te).setMeasurementLimits(pkt.minTemp, pkt.maxTemp);
                    te.markDirty();
                }
            }
        });
        c.setPacketHandled(true);
    }

    public static void write(PacketSensorTemperatureConfig pkt, PacketBuffer buffer)
    {
        buffer.writeBlockPos(pkt.pos);
        buffer.writeInt(pkt.minTemp);
        buffer.writeInt(pkt.maxTemp);
    }

    public static PacketSensorTemperatureConfig read(PacketBuffer buffer)
    {
        return new PacketSensorTemperatureConfig(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
    }
}
