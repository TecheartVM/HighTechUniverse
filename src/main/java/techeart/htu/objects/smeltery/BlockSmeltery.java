package techeart.htu.objects.smeltery;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;
import techeart.htu.objects.TileEntityIgnitable;
import techeart.htu.utils.RegistryHandler;
import techeart.htu.utils.registration.HTUBlock;

import java.util.Random;

public class BlockSmeltery extends HTUBlock implements ITileEntityProvider
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public BlockSmeltery()
    {
        super(Block.Properties.create(Material.ROCK)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(4.0f, 7.0f)
                .sound(SoundType.STONE)
        );
        this.setDefaultState(this.getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    public static void setState(boolean active, World world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        TileEntity tileEntity = world.getTileEntity(pos);

        if(!(tileEntity instanceof TileEntitySmeltery)) return;

        world.setBlockState(pos, /*RegistryHandler.BLOCK_SMELTERY.getPrimary()*/ RegistryHandler.SMELTERY.getMainBlock().getBlock().getDefaultState().with(FACING, state.get(FACING)).with(LIT, active), 3);

        if(tileEntity != null)
        {
            tileEntity.validate();
            world.setTileEntity(pos, tileEntity);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        super.fillStateContainer(builder);
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) { return state.rotate(mirrorIn.toRotation(state.get(FACING))); }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction) { return state.with(FACING, direction.rotate(state.get(FACING))); }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        return state.get(LIT) ? 14 : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) { return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite()); }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if(stack.hasDisplayName())
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof TileEntitySmeltery)
            {

            }
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn)
    {
        return RegistryHandler.SMELTERY.getMainBlock().getMachineTile().create();
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state)
    {
        return false;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos)
    {
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if (stateIn.get(LIT))
        {
            double d0 = (double)pos.getX() + 0.5D;
            double d1 = pos.getY();
            double d2 = (double)pos.getZ() + 0.5D;
            if (rand.nextDouble() < 0.1D)
            {
                worldIn.playSound(d0, d1, d2, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = stateIn.get(FACING);
            Direction.Axis direction$axis = direction.getAxis();
            double d4 = rand.nextDouble() * 0.6D - 0.3D;
            double d5 = direction$axis == Direction.Axis.X ? (double)direction.getXOffset() * 0.52D : d4;
            double d6 = rand.nextDouble() * 6.0D / 16.0D;
            double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getZOffset() * 0.52D : d4;
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(ParticleTypes.FLAME, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if(worldIn != null && !worldIn.isRemote)
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof TileEntitySmeltery)
            {
                Item heldItem = player.getHeldItemMainhand().getItem();
                if(TileEntityIgnitable.isIgnitionTool(heldItem))
                {
                    ((TileEntitySmeltery)tileEntity).ignite();
                    worldIn.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResultType.SUCCESS;
                }

                NetworkHooks.openGui((ServerPlayerEntity)player, (INamedContainerProvider)tileEntity, pos);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntitySmeltery)
        {
            if(newState.getBlock() != state.getBlock())
                InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntitySmeltery)tileEntity);
        }

        if(state.hasTileEntity() && state.getBlock() != newState.getBlock())
        {
            worldIn.removeTileEntity(pos);
        }
    }
}
