package techeart.htu.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HTUHooks
{
    private static AmbientTemperatureHandler temperatureHandler = new AmbientTemperatureHandler();

    public static int getAmbientTemperature(World world, BlockPos pos)
    {
        return temperatureHandler.getAmbientTemperature(world, pos);
    }
}
