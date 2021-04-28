package techeart.htu.utils.registration;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemGroup;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class HTUBlock extends Block
{
    public HTUBlock(Properties props)
    {
        super(props);
    }

    public static class Builder
    {
        private Block.Properties properties = AbstractBlock.Properties.create(Material.AIR);

        @Nullable
        private Supplier<? extends Block> block;

        private boolean needItem;
        @Nullable
        private ItemGroup group;

        public HTUBlock.Builder withProperties(Block.Properties properties) {
            this.properties = properties;
            return this;
        }
        public HTUBlock.Builder setBlock(Supplier<? extends Block> block) {// HTUBlock.class
            this.block = block;
            return this;
        }
        public HTUBlock.Builder needItem(ItemGroup group) {
            this.needItem = true;
            this.group = group;
            return this;
        }

        public Properties getProperties() {
            return properties;
        }

        @Nullable
        public ItemGroup getGroup() {
            return group;
        }

        @Nullable
        public Supplier<? extends Block> getBlock() {
            return block;
        }

        public boolean isNeedItem() {
            return needItem;
        }

        //TODO: Make this thing more flexible
        public Supplier<? extends Block> build() {
            if(block == null)
                return () -> new Block(properties);
            return block;
        }
    }
}
