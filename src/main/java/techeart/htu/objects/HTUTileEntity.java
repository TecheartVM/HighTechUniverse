package techeart.htu.objects;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public abstract class HTUTileEntity extends TileEntity
{
    public HTUTileEntity(TileEntityType<?> tileEntityTypeIn) { super(tileEntityTypeIn); }

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {}

    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState) {}

    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState)
    {
        if(this instanceof IInventory) InventoryHelper.dropInventoryItems(world, pos, (IInventory) this);
    }

    public void onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {}
}
