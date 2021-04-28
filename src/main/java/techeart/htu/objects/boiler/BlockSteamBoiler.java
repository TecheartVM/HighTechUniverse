package techeart.htu.objects.boiler;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
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
import net.minecraftforge.registries.ForgeRegistries;
import techeart.htu.utils.RegistryHandler;
import techeart.htu.utils.registration.HTUBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

public class BlockSteamBoiler extends HTUBlock implements ITileEntityProvider
{
    //TODO: make this thing ALIVE
    private static Random random = new Random();
    public static final int componentDropChance = 80;

    private static ArrayList<Item> ignitionTools = new ArrayList<>();

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public BlockSteamBoiler()
    {
        super(Block.Properties.create(Material.ROCK)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(4.0f, 7.0f)
                .sound(SoundType.STONE)
        );

        this.setDefaultState(this.getDefaultState().getBlockState().with(FACING, Direction.NORTH).with(LIT, false));

        ignitionTools.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:flint_and_steel")));
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
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction)
    {
        return state.with(FACING, direction.rotate(state.get(FACING)));
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        return state.get(LIT) ? super.getLightValue(state, world, pos) : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        BlockState blockAbove = context.getWorld().getBlockState(context.getPos().up());
        return blockAbove.isReplaceable(context) ? this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite()) : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        worldIn.setBlockState(pos.up(), RegistryHandler.STEAM_BOILER.getMachineBlock(1).getBlock().getDefaultState());
        if(stack.hasDisplayName())
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof TileEntitySteamBoiler)
            {
                ((TileEntitySteamBoiler)tileEntity).setCustomName(stack.getDisplayName());
            }
        }
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
    public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param)
    {
        if(id == 0)
        {
            worldIn.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D, 0.0D, 0.2D, 0.0D);
            return true;
        }
        return false;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
    {
//        if(world != null && !world.isRemote)
//        {
//            TileEntity tileEntity = world.getTileEntity(pos);
//            Item heldItem = player.getHeldItemMainhand().getItem();
//            if(tileEntity instanceof TileEntitySteamBoiler)
//            {
//                if(heldItem.equals(Items.WATER_BUCKET) && player.getHeldItemMainhand().getCount() == 1/*TODO:Need to fix?*/)
//                {
//                    if(((TileEntitySteamBoiler) world.getTileEntity(pos)).fill(new FluidStack(Fluids.WATER,1000), IFluidHandler.FluidAction.EXECUTE) != 0)
//                    {
//                        world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
//                        if(!player.isCreative()) player.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.BUCKET, 1));
//                    }
//                }
//                else
//                {
//                    for (Item tool : ignitionTools)
//                    {
//                        if(heldItem == tool)
//                        {
//                            world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
//                            ((TileEntitySteamBoiler) tileEntity).ignite();
//                            return ActionResultType.SUCCESS;
//                        }
//                    }
//
//                    NetworkHooks.openGui((ServerPlayerEntity)player, (INamedContainerProvider)tileEntity, pos);
//                }
//            }
//        }
        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return RegistryHandler.STEAM_BOILER.getMainBlock().getMachineTile().create(); }



    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(newState.getBlock() instanceof BlockSteamBoiler) return;

        //drop inventory items
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof TileEntitySteamBoiler)
            if(newState.getBlock() != state.getBlock())
                InventoryHelper.dropInventoryItems(world, pos, (TileEntitySteamBoiler)tileEntity);
        //remove tileentity
        if(state.hasTileEntity() && state.getBlock() != newState.getBlock())
            world.removeTileEntity(pos);
        //remove boiler top
        BlockState blockAbove = world.getBlockState(pos.up());
        if(blockAbove.getBlock() == RegistryHandler.STEAM_BOILER.getMainBlock().getBlock())
            world.setBlockState(pos.up(), Blocks.AIR.getDefaultState());
    }
}
