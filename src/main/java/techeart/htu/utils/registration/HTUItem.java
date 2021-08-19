package techeart.htu.utils.registration;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import techeart.htu.CreativeTabs;

import javax.annotation.Nullable;

public class HTUItem extends Item
{
    public HTUItem(ItemGroup itemGroup)
    {
        super(new Item.Properties().group(itemGroup));
    }

    public HTUItem() { super(new Properties().group(CreativeTabs.STEAM_AGE)); }

    public HTUItem(Properties props)
    {
        super(props);
    }

    public static class Builder //TODO: Make this thing more useful
    {
        @Nullable
        private ItemGroup itemGroup;
        @Nullable
        private Block block;

        public HTUItem.Builder itemGroup(ItemGroup itemGroup)
        {
            this.itemGroup = itemGroup;
            return this;
        }
        public HTUItem.Builder block(Block block)
        {
            this.block = block;
            return this;
        }

        public Item build()
        {
            if(block == null)
            {
                if (itemGroup == null)
                    return new Item(new Properties().group(ItemGroup.MISC));
                return new Item(new Properties().group(itemGroup));
            }
            return new BlockItem(this.block,new Properties().group(itemGroup));
        }
    }
}
