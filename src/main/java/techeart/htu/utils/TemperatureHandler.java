package techeart.htu.utils;

import com.google.common.collect.Maps;
import net.minecraft.block.material.Material;
import net.minecraft.util.Util;

import java.util.Map;

public class TemperatureHandler
{
    public void v()
    {

    }

    public static final Map<Material, Float> MATERIAL_TEMPERATURE_COEFFS = Util.make(Maps.newHashMap(), (map) -> {
        map.put(Material.IRON, 0.9f);
        map.put(Material.AIR, 0.25f);
        map.put(Material.CLAY, 0.65f);
        map.put(Material.EARTH, 0.6f);
        map.put(Material.GLASS, 0.5f);
        map.put(Material.ROCK, 0.75f);
        map.put(Material.SAND, 0.7f);
        map.put(Material.WATER, 0.7f);
        map.put(Material.WOOD, 0.7f);
        map.put(Material.WOOL, 0.3f);

        map.put(Material.ICE, -0.7f);
        map.put(Material.PACKED_ICE, -0.8f);
        map.put(Material.SNOW, -0.5f);
        map.put(Material.SNOW_BLOCK, -0.5f);
    });


}
