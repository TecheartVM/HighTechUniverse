package techeart.htu.objects.sensors.temperature;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import techeart.htu.MainClass;
import techeart.htu.objects.HTUTileBlock;
import techeart.htu.utils.registration.RegistryHandler;

import javax.annotation.Nullable;
import java.util.Map;

public class BlockSensorTemperature extends HTUTileBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final Map<Direction, VoxelShape> SHAPES = Util.make(Maps.newHashMap(), (map) -> {
        final VoxelShape N = Block.makeCuboidShape(7.0D, 4.0D, 15.0D, 9.0D, 12.0D, 16.0D);
        final VoxelShape S = Block.makeCuboidShape(7.0D, 4.0D, 0.0D, 9.0D, 12.0D, 1.0D);
        final VoxelShape E = Block.makeCuboidShape(0.0D, 4.0D, 7.0D, 1.0D, 12.0D, 9.0D);
        final VoxelShape W = Block.makeCuboidShape(15.0D, 4.0D, 7.0D, 16.0D, 12.0D, 9.0D);

        map.put(Direction.NORTH, N);
        map.put(Direction.SOUTH, S);
        map.put(Direction.EAST, E);
        map.put(Direction.WEST, W);
    });

    public BlockSensorTemperature()
    {
        super(Block.Properties.create(Material.IRON)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(1.0f, 4.0f)
                .sound(SoundType.METAL)
        );
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) { return SHAPES.get(state.get(FACING)); }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        if (!context.replacingClickedOnBlock())
        {
            BlockState blockstate = context.getWorld().getBlockState(context.getPos().offset(context.getFace().getOpposite()));
            if (blockstate.matchesBlock(this) && blockstate.get(FACING) == context.getFace())
                return null;
        }

        BlockState blockstate = this.getDefaultState();
        IWorldReader world = context.getWorld();
        BlockPos pos = context.getPos();
        FluidState fluidstate = context.getWorld().getFluidState(context.getPos());

        for(Direction direction : context.getNearestLookingDirections())
        {
            if (direction.getAxis().isHorizontal())
            {
                blockstate = blockstate.with(FACING, direction.getOpposite());
                if (blockstate.isValidPosition(world, pos))
                    return blockstate.with(WATERLOGGED, fluidstate.getFluid() == Fluids.WATER);
            }
        }

        return null;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        if(facing == stateIn.get(FACING).getOpposite())
        {
            if(!this.isValidPosition(stateIn, worldIn, currentPos))
                return Blocks.AIR.getDefaultState();
            else
            {
                TileEntity te = worldIn.getTileEntity(currentPos);
                if(te instanceof TileEntitySensorTemperature)
                    ((TileEntitySensorTemperature)te).updateTarget();
                else MainClass.LOGGER.error("Invalid TileEntity at pos {} {} {}", currentPos.getX(), currentPos.getY(), currentPos.getZ());
            }
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
    {
        Direction wallDir = state.get(FACING).getOpposite();
        BlockState wall = worldIn.getBlockState(pos.offset(wallDir));
        if(wall.isAir()) return false;
        return wall.isSolidSide(worldIn, pos, state.get(FACING));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) { return state.rotate(mirrorIn.toRotation(state.get(FACING))); }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction) { return state.with(FACING, direction.rotate(state.get(FACING))); }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) { builder.add(FACING, WATERLOGGED); }

    @Override
    public FluidState getFluidState(BlockState state) { return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state); }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) { return RegistryHandler.SENSOR_TEMPERATURE_TE.get().create(); }

    /*redstone interaction*/
    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) { return true; }

    @Override
    public boolean canProvidePower(BlockState state) { return true; }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
    {
        TileEntity te = blockAccess.getTileEntity(pos);
        if(te instanceof TileEntitySensorTemperature)
            return ((TileEntitySensorTemperature)te).getPowerLevel();
        return super.getWeakPower(blockState, blockAccess, pos, side);
    }

    public void notifyNeighbours(World worldIn, BlockPos pos) { worldIn.notifyNeighborsOfStateChange(pos, this); }
}
