package techeart.htu.utils.registration.machine;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import techeart.htu.utils.registration.DoubleRegisteredObject;

public class MachineBlock<TILE extends TileEntity> {
        private final DoubleRegisteredObject<Block, Item> block;
        private final RegistryObject<TileEntityType<TILE>> machineTile;

        public MachineBlock(DoubleRegisteredObject<Block, Item> block, RegistryObject<TileEntityType<TILE>> machineTile) {
            this.block = block;
            this.machineTile = machineTile;
        }

        public Block getBlock() {
            return block.getPrimary();
        }
        public Item getItem(){return block.getSecondary();}
        public TileEntityType<TILE> getMachineTile() {
            return machineTile.get();
        }
}
