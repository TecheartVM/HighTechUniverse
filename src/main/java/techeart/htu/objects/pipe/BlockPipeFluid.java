package techeart.htu.objects.pipe;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import techeart.htu.utils.ModUtils;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nullable;

public class BlockPipeFluid extends SixWayBlock implements ITileEntityProvider
{
    public BlockPipeFluid()
    {
        super(0.3125f,
                Block.Properties.create(Material.IRON)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(3.0f, 6.0f)
                .sound(SoundType.METAL));
        this.setDefaultState(this.getStateContainer().getBaseState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false));
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
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity != null && tileEntity.getType() == RegistryHandler.FLUID_PIPE_TE.get())
        {
            //if(!worldIn.isRemote) ((TileEntityPipeFluid)tileEntity).createGrid();
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        //TODO optimization, I guess
        if(!worldIn.isRemote())
        {
            TileEntity tileEntity = worldIn.getTileEntity(currentPos);
            if(tileEntity != null && tileEntity.getType() == RegistryHandler.FLUID_PIPE_TE.get())
            {
                ((TileEntityPipeFluid)tileEntity).updateGrid();
            }
        }

        return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), isConnectable(worldIn, facingPos, facing.getOpposite()));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity != null && tileEntity.getType() == RegistryHandler.FLUID_PIPE_TE.get())
        {
            if(!worldIn.isRemote) //System.out.println("Fluid inside: " + ((TileEntityPipeFluid)tileEntity).getFluidInTank(0).getAmount());
                ModUtils.playerInfoMessage("Fluid inside: " + ((TileEntityPipeFluid)tileEntity).getFluidInTank(0).getAmount() + " mb", player);
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    private boolean isConnectable(IBlockReader world, BlockPos pos, Direction blockFace)
    {
        if(world.getBlockState(pos).getBlock() == RegistryHandler.BLOCK_PIPE.getPrimary()) return true;
        if(world.getBlockState(pos).getBlock() instanceof ITileEntityProvider)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity == null) return false;
            LazyOptional cap = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, blockFace);
            return cap != LazyOptional.empty();
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
    public TileEntity createNewTileEntity(IBlockReader worldIn)
    {
        return RegistryHandler.FLUID_PIPE_TE.get().create();
    }
}
