package techeart.htu.utils;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.ForgeHooks;

import java.util.Map;

public class FuelTemperatures
{
    private static final Map<Item, Integer> list = Maps.newHashMap();

    public static void init()
    {
        addItemBurnTime(Items.LAVA_BUCKET, 1200);
        addItemBurnTime(Items.BLAZE_ROD, 1200);
        addItemBurnTime(Blocks.COAL_BLOCK, 1150);
        addItemBurnTime(Items.COAL, 1150);
        addItemBurnTime(Items.CHARCOAL, 1150);
        addItemBurnTime(Blocks.DRIED_KELP_BLOCK, 700);
        addItemBurnTime(Blocks.BAMBOO, 700);
        addItemBurnTime(Blocks.DEAD_BUSH, 700);
        addItemTagBurnTime(ItemTags.LOGS, 950);
        addItemTagBurnTime(ItemTags.CARPETS, 600);
    }

    public static int getBurnTemperature(Item fuel)
    {
        if(list.containsKey(fuel))
            return list.get(fuel);

        if(!isFuel(fuel)) return 0;

        return 800;
    }

    public static int getBurnTemperature(ItemStack fuel) { return getBurnTemperature(fuel.getItem()); }

    private static void addItemTagBurnTime(ITag.INamedTag<Item> itemTag, int burnTimeIn)
    {
        for(Item item : itemTag.getAllElements())
        {
            list.put(item, burnTimeIn);
        }
    }

    private static void addItemBurnTime(IItemProvider itemProvider, int burnTimeIn)
    {
        list.put(itemProvider.asItem(), burnTimeIn);
    }

    private static boolean isFuel(Item item)
    {
        return ForgeHooks.getBurnTime(new ItemStack(item)) > 0;
    }
}
