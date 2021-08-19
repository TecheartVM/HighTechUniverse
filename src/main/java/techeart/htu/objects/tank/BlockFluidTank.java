package techeart.htu.objects.tank;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import techeart.htu.utils.ColorConstants;
import techeart.htu.utils.FluidUtils;
import techeart.htu.utils.KeyboardHelper;
import techeart.htu.utils.ModUtils;
import techeart.htu.utils.registration.HTUBlock;
import techeart.htu.utils.registration.RegistryHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockFluidTank extends HTUBlock implements ITileEntityProvider
{
    private TileEntityFluidTank tileEntity;
    private FluidStack fluidInTank;

    public BlockFluidTank()
    {
        super(Block.Properties.create(Material.IRON)
                .harvestTool(ToolType.PICKAXE)
                .setRequiresTool()
                .hardnessAndResistance(4.0f, 7.0f)
                .sound(SoundType.METAL)
                .notSolid()
                .setOpaque((state, reader, pos) -> false)
                .setBlocksVision((state, reader, pos) -> false)
        );
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {
        List<ItemStack> drops = new ArrayList<>();
        ItemStack item = new ItemStack(this.getBlock().asItem(), 1);
        drops.add(item);
        if(builder.getWorld().isRemote) return drops;
        if(fluidInTank == null || fluidInTank.isEmpty()) return drops;

        CompoundNBT data = item.getOrCreateTag();
        fluidInTank.writeToNBT(data);
        return drops;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(!worldIn.isRemote)
            fluidInTank = ((TileEntityFluidTank)worldIn.getTileEntity(pos)).getFluid();
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        if(KeyboardHelper.isHoldingShift())
        {
            CompoundNBT data = stack.getTag();
            if (data != null)
            {
                Fluid fluid = FluidStack.loadFluidStackFromNBT(data).getFluid();
                Color textColor;
                int color = ModUtils.getFluidTextureColor(fluid, 0, 0);
                if(color == 0) textColor = ColorConstants.DESCRIPTION_FLUID_NAME_DEFAULT;
                else textColor = Color.fromInt(color);

                tooltip.add(new StringTextComponent("Fluid: ")
                        .appendSibling(new TranslationTextComponent(ModUtils.getFluidName(data)).setStyle(Style.EMPTY.setColor(textColor)))
                        .appendSibling(new StringTextComponent(", " + data.getInt("Amount") + "mB")).setStyle(Style.EMPTY.setItalic(true)));
            }
            else tooltip.add(new TranslationTextComponent("htu.fluidtank.tooltip").setStyle(Style.EMPTY.setColor(ColorConstants.DESCRIPTION_TEXT_NORMAL)));
        }
        else tooltip.add(new TranslationTextComponent("htu.moreinfo.tooltip"));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        if(!(te instanceof TileEntityFluidTank)) return 0;
        TileEntityFluidTank tankTile = ((TileEntityFluidTank)te);
        FluidStack fluid = tankTile.getFluid();
        if(fluid.isEmpty()) return 0;
        return Math.max(fluid.getFluid().getAttributes().getLuminosity(), super.getLightValue(state, world, pos));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return Block.makeCuboidShape(2,0 ,2, 14, 16, 14);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        //TODO add vanilla bottles support..?
        if(world.isRemote || player.isSneaking()) return  ActionResultType.CONSUME;
        TileEntityFluidTank tankTile = ((TileEntityFluidTank) world.getTileEntity(pos));
        if(tankTile == null) return ActionResultType.PASS;

        ItemStack heldItem = player.getHeldItem(handIn);
        if (heldItem.isEmpty())
            ModUtils.playerInfoMessage("Fluid in tank: " + tankTile.getFluid().getAmount()+"mB",player);

        /*~~~~~ACHIEVEMENT~~~~~*/
        if(heldItem.getItem() == Items.PUFFERFISH_BUCKET
                || heldItem.getItem() == Items.COD_BUCKET
                || heldItem.getItem() == Items.SALMON_BUCKET
                || heldItem.getItem() == Items.TROPICAL_FISH_BUCKET
        ) ModUtils.unlockAdvancement(player,"secret/wip");
        /*~~~~~END ACHIEVEMENT~~~~~*/

        //handling tank interaction
        LazyOptional<IFluidHandler> lo = tankTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        if(lo.isPresent() && FluidUtils.interactWithTank(player, handIn, heldItem, lo.orElse(null), 0).updated())
            updateLight(world, pos);

        return ActionResultType.SUCCESS;
    }

    protected void updateLight(World world, BlockPos pos)
    {
        world.getChunkProvider().getLightManager().checkBlock(pos);
        world.addBlockEvent(pos, this, 1, 1);
    }

    @Override
    public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param)
    {
        if(id == 1)
        {
            //updates client light
            if(worldIn.isRemote)
                ((ClientWorld)worldIn).getChunkProvider().getLightManager().checkBlock(pos);
            return true;
        }
        return super.eventReceived(state, worldIn, pos, id, param);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if(worldIn.isRemote) return;

        if(stack.getTag() != null && tileEntity != null)
        {
            FluidStack f = FluidStack.loadFluidStackFromNBT(stack.getTag());
            tileEntity.fill(f, FluidAction.EXECUTE);
        }
        if(tileEntity != null && !tileEntity.isEmpty()) updateLight(worldIn, pos);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn)
    {
        TileEntity te = RegistryHandler.FLUID_TANK_TE.get().create();
        tileEntity = (TileEntityFluidTank) te;
        return te;
    }
}