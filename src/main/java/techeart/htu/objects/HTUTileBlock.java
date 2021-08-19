package techeart.htu.objects;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import techeart.htu.MainClass;
import techeart.htu.utils.registration.HTUBlock;

import javax.annotation.Nullable;

public abstract class HTUTileBlock extends HTUBlock
{
    public HTUTileBlock(Properties props) { super(props); }

    @Override
    public boolean hasTileEntity(BlockState state) { return true; }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof HTUTileEntity)
            ((HTUTileEntity) tileEntity).onPlaced(world, pos, state, placer, stack);
        else MainClass.LOGGER.error("Invalid TileEntity at pos {} {} {}", pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        super.onBlockAdded(state, world, pos, oldState, isMoving);

        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof HTUTileEntity)
            ((HTUTileEntity) tileEntity).onBlockAdded(state, world, pos, oldState);
        else MainClass.LOGGER.error("Invalid TileEntity at pos {} {} {}", pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof HTUTileEntity)
        {
            if (newState.getBlock() != state.getBlock())
                ((HTUTileEntity) tileEntity).onBlockRemoved(state, world, pos, newState);
        }
        else MainClass.LOGGER.error("Invalid TileEntity at pos {} {} {}", pos.getX(), pos.getY(), pos.getZ());

        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof HTUTileEntity)
            ((HTUTileEntity) tileEntity).onBlockActivated(state, world, pos, player, hand, hit);
        else MainClass.LOGGER.error("Invalid TileEntity at pos {} {} {}", pos.getX(), pos.getY(), pos.getZ());
        return ActionResultType.SUCCESS;
    }
}
