package techeart.htu.objects;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

public class BlockPropertyPatterns
{
    public static Block.Properties blockMetal() { return blockMetal(2, 5.0f, 8.0f); }
    public static Block.Properties blockMetal(int harvestLevel) { return blockMetal(harvestLevel, 5.0f, 8.0f); }
    public static Block.Properties blockMetal(int harvestLevel, float hardness, float resistance)
    {
        return Block.Properties.create(Material.IRON).sound(SoundType.METAL)
                .harvestTool(ToolType.PICKAXE)
                .harvestLevel(harvestLevel)
                .hardnessAndResistance(hardness, resistance);
    }

    public static Block.Properties blockOre() { return blockMetal(2, 1.5f, 6.0f); }
    public static Block.Properties blockOre(int harvestLevel) { return blockMetal(harvestLevel, 1.5f, 6.0f); }
    public static Block.Properties blockOre(int harvestLevel, float hardness, float resistance)
    {
        return Block.Properties.create(Material.ROCK).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE)
                .harvestLevel(harvestLevel)
                .hardnessAndResistance(hardness, resistance);
    }
}
