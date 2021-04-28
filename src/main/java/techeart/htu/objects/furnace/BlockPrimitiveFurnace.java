package techeart.htu.objects.furnace;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import techeart.htu.objects.TileEntityIgnitable;
import techeart.htu.utils.RegistryHandler;

import java.util.Random;

public class BlockPrimitiveFurnace extends AbstractFurnaceBlock
{
//    private static ArrayList<Item> ignitionTools = new ArrayList<>();

    public BlockPrimitiveFurnace()
    {
        super(Block.Properties.create(Material.ROCK)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(2.0f, 5.0f)
                .sound(SoundType.STONE)
        );

//        ignitionTools.add(Items.FLINT_AND_STEEL);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        super.onReplaced(state, worldIn, pos, newState, isMoving);
        if(newState.getBlock().isAir(newState, worldIn, pos) && state.get(AbstractFurnaceBlock.LIT))
            worldIn.setBlockState(pos, Blocks.FIRE.getDefaultState());
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
    {
        if (!world.isRemote)
        {
            Item heldItem = player.getHeldItem(hand).getItem();
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityPrimitiveFurnace)
            {
                if(heldItem == Items.WATER_BUCKET)
                {
                    ((TileEntityPrimitiveFurnace) tileEntity).Extinguish();
                    return ActionResultType.SUCCESS;
                }
                for (Item tool : TileEntityIgnitable.IGNITION_TOOLS)
                {
                    if(heldItem == tool)
                    {
                        world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        ((TileEntityPrimitiveFurnace) tileEntity).ignite();
                        return ActionResultType.SUCCESS;
                    }
                }

                this.interactWith(world, pos, player);
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    protected void interactWith(World worldIn, BlockPos pos, PlayerEntity player)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntityPrimitiveFurnace)
        {
            player.openContainer((INamedContainerProvider)tileentity);
            player.addStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return RegistryHandler.PRIMITIVE_FURNACE.getMainBlock().getMachineTile().create(); }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.get(LIT)) {
            double d0 = (double)pos.getX() + 0.5D;
            double d1 = (double)pos.getY();
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
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) { return state.get(LIT) ? 14 : 0; }
}
