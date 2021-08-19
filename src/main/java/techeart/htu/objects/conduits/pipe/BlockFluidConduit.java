package techeart.htu.objects.conduits.pipe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import techeart.htu.utils.ModUtils;

import javax.annotation.Nullable;

public class BlockFluidConduit<Conduit extends FluidConduit> extends SixWayBlock
{
    public BlockFluidConduit()
    {
        super(0.3125f,
                Block.Properties.create(Material.IRON)
                        .harvestTool(ToolType.PICKAXE)
                        .hardnessAndResistance(3.0f, 6.0f)
                        .sound(SoundType.METAL));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!worldIn.isRemote) {
            if (player.getHeldItem(Hand.MAIN_HAND) == ItemStack.EMPTY) {
                ModUtils.playerInfoMessage("Conduit info: " + ((Conduit) worldIn.getTileEntity(pos)).getComponent().getComponentData() + " Grid amount: " + ((Conduit) worldIn.getTileEntity(pos)).getGrid().getContent().getFluidAmount() + "/" + ((Conduit) worldIn.getTileEntity(pos)).getGrid().getContent().getCapacity(), player);
                ModUtils.playerChatMessage(((Conduit) worldIn.getTileEntity(pos)).getComponent().getNeighboursASString(), player);
            }
            if(player.getHeldItem(handIn).getItem() == Items.WATER_BUCKET){
                worldIn.getTileEntity(pos).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        IBlockReader world = context.getWorld();
        BlockPos pos = context.getPos();
        return this.getDefaultState()
                .with(DOWN, isConnectable(world, pos.down(), Direction.UP))
                .with(UP, isConnectable(world, pos.up(), Direction.DOWN))
                .with(NORTH, isConnectable(world, pos.north(), Direction.SOUTH))
                .with(EAST, isConnectable(world, pos.east(), Direction.WEST))
                .with(SOUTH, isConnectable(world, pos.south(), Direction.NORTH))
                .with(WEST, isConnectable(world, pos.west(), Direction.EAST));
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        if(!worldIn.isRemote())
            ((Conduit) worldIn.getTileEntity(pos)).getComponent().updateNeighbour(ModUtils.getDirByCoords(pos, fromPos), worldIn.getTileEntity(fromPos));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), isConnectable(worldIn, facingPos, facing.getOpposite()));
    }

    private boolean isConnectable(IBlockReader world, BlockPos pos, Direction blockFace)
    {
        if(world.getTileEntity(pos) instanceof FluidConduit) return true;

        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity != null)
        {
            LazyOptional<IFluidHandler> cap = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, blockFace);
            return cap.isPresent();
        }
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) { builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN); }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FluidConduit(true);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}
