package techeart.htu.objects.sensors;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import techeart.htu.MainClass;
import techeart.htu.utils.ModUtils;
import techeart.htu.utils.RegistryHandler;
import techeart.htu.utils.registration.HTUBlock;

import javax.annotation.Nullable;

public class BlockSensorFluidLevel extends HTUBlock
{
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_0_15;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape SHAPE = Block.makeCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D);

    public BlockSensorFluidLevel()
    {
        super(Block.Properties.create(Material.IRON)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(1.0f, 4.0f)
                .sound(SoundType.METAL)
        );
        this.setDefaultState(this.stateContainer.getBaseState().with(ROTATION, 0).with(WATERLOGGED, false));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if(!worldIn.isRemote() && player.getHeldItem(handIn).isEmpty())
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntitySensorFluidLevel)
            {
                int measured = ((TileEntitySensorFluidLevel) te).getFluidInTarget();
                ModUtils.playerInfoMessage("Fluid level: " + measured + "mB", player);
            }
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) { return SHAPE; }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        FluidState fluidstate = context.getWorld().getFluidState(context.getPos());
        return this.getDefaultState()
                .with(ROTATION, MathHelper.floor((double) ((180.0F + context.getPlacementYaw()) * 16.0F / 360.0F) + 0.5D) & 15)
                .with(WATERLOGGED, fluidstate.getFluid() == Fluids.WATER);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof TileEntitySensorFluidLevel)
            ((TileEntitySensorFluidLevel)te).updateTarget();
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        if(facing == Direction.DOWN)
        {
            if(!this.isValidPosition(stateIn, worldIn, currentPos))
                return Blocks.AIR.getDefaultState();
            else
            {
                TileEntity te = worldIn.getTileEntity(currentPos);
                if(te instanceof TileEntitySensorFluidLevel)
                    ((TileEntitySensorFluidLevel)te).updateTarget();
                else MainClass.LOGGER.error("Invalid TileEntity at pos {} {} {}", currentPos.getX(), currentPos.getY(), currentPos.getZ());
            }
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) { return worldIn.getBlockState(pos.down()).getBlock() != Blocks.AIR; }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) { return state.with(ROTATION, rot.rotate(state.get(ROTATION), 16)); }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) { return state.with(ROTATION, mirrorIn.mirrorRotation(state.get(ROTATION), 16)); }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) { builder.add(ROTATION, WATERLOGGED); }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public boolean hasTileEntity(BlockState state) { return true; }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) { return RegistryHandler.SENSOR_FLUID_LEVEL_TE.get().create(); }

//    @Nullable
//    @Override
//    public TileEntity createNewTileEntity(IBlockReader worldIn)
//    {
//        return null;
//    }

    /*redstone interaction*/
    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) { return true; }

    @Override
    public boolean canProvidePower(BlockState state) { return true; }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
    {
        TileEntity te = blockAccess.getTileEntity(pos);
        if(te instanceof TileEntitySensorFluidLevel)
            return ((TileEntitySensorFluidLevel)te).getPowerLevel();
        return super.getWeakPower(blockState, blockAccess, pos, side);
    }

    public void notifyNeighbours(World worldIn, BlockPos pos) { worldIn.notifyNeighborsOfStateChange(pos, this); }
}
