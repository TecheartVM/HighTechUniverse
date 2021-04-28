package techeart.htu.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class AmbientTemperatureHandler
{
    /**
     * Returns the ambient temperature in biome at given pos ranged from -30 to 50 degrees for biomes default temperature range (~ from -0.5 to 2.0).
     * A '0' value corresponds to a ~0.15 in biome temperatures
     */
    public int getAmbientTemperature(World world, BlockPos pos)
    {
        if(world == null || pos == null) return getDefaultTemperature();
        Biome biomeAtPos = world.getBiome(pos);
        float temp = biomeAtPos.getTemperature(pos); // -0.5 : 2.0

//        temp = (float) Math.round(temp * 100) / 100;

                     //calculating nice temperature values with hardcoded curved graph formula
        return (int) (86.7f - Math.pow(Math.PI, -0.4d * (temp - 9.89f)));
    }

    public int getDefaultTemperature() { return 20; }
}
