package techeart.htu.objects.furnace;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityPrimitiveFurnace extends AbstractFurnaceTileEntity
{
    private Random random = new Random();

    /**Chance in percents to extinct the burning fire in furnace*/
    public static final int EXTINCTION_CHANCE = 5;

    //TODO: MOVE IT!
    private boolean ignited = false;
    private void setIgnited(boolean value)
    {
        if(ignited != value)
        {
            ignited = value;
            this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(AbstractFurnaceBlock.LIT, this.isBurning()), 3);
            markDirty();
        }
    }

    public TileEntityPrimitiveFurnace()
    {
        this(RegistryHandler.PRIMITIVE_FURNACE.getMainBlock().getMachineTile());
    }

    public TileEntityPrimitiveFurnace(TileEntityType<?> tileEntityTypeIn) { super(tileEntityTypeIn, IRecipeType.SMELTING); }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.furnace");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) { return new FurnaceContainer(id, player, this, this.furnaceData); }

    @Override
    public void tick()
    {
        boolean flag = false;
        if (this.isBurning()) this.furnaceData.set(0, furnaceData.get(0) - 1);

        if (!this.world.isRemote)
        {
            if(ignited && !isBurning())
            {
                if(random.nextInt(100) < 99 - EXTINCTION_CHANCE) ignite();
                else setIgnited(false);
                flag = true;
            }

            ItemStack fuel = this.items.get(1);
            if (this.isBurning() || !fuel.isEmpty() && !this.items.get(0).isEmpty())
            {
                IRecipe<?> irecipe = this.world.getRecipeManager().getRecipe((IRecipeType<AbstractCookingRecipe>)this.recipeType, this, this.world).orElse(null);

            //Removed furnace autoignition. Replaced with ignite() method

                if (this.isBurning() && this.canSmelt(irecipe)) {
                    this.furnaceData.set(2, furnaceData.get(2) + 1);
                    if (this.furnaceData.get(2) == this.furnaceData.get(3))
                    {
                        this.furnaceData.set(2, 0);
                        this.furnaceData.set(3, this.getCookTime());
                        this.smelt(irecipe);
                        flag = true;
                    }
                }
                else
                {
                    this.furnaceData.set(2, 0);
                }
            }
            else if (!this.isBurning() && this.furnaceData.get(2) > 0)
            {
                this.furnaceData.set(2, MathHelper.clamp(this.furnaceData.get(2) - 2, 0, this.furnaceData.get(3)));
            }
        }
        if (flag) this.markDirty();
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        ignited = nbt.getBoolean("Ignited");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound.putBoolean("Ignited", ignited);
        return super.write(compound);
    }

    protected void smelt(@Nullable IRecipe<?> recipe)
    {
        if (recipe != null && this.canSmelt(recipe))
        {
            ItemStack itemstack = this.items.get(0);
            ItemStack itemstack1 = recipe.getRecipeOutput();
            ItemStack itemstack2 = this.items.get(2);
            if (itemstack2.isEmpty()) this.items.set(2, itemstack1.copy());
            else if (itemstack2.getItem() == itemstack1.getItem()) itemstack2.grow(itemstack1.getCount());

            if (!this.world.isRemote) this.setRecipeUsed(recipe);

            if (itemstack.getItem() == Blocks.WET_SPONGE.asItem() && !this.items.get(1).isEmpty() && this.items.get(1).getItem() == Items.BUCKET)
            {
                this.items.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            itemstack.shrink(1);
        }
    }

    public boolean isBurning()
    {
        return this.furnaceData.get(0) > 0;
    }

    //TODO: (fix) once lited - forever lited!
    public void ignite()
    {
        if(!world.isRemote)
        {
            ItemStack fuel = this.items.get(1);
            if (!fuel.isEmpty())
            {
                if (!isBurning())
                {
                    this.furnaceData.set(0, getBurnTime(fuel));
                    this.furnaceData.set(1, this.furnaceData.get(0));
                    if (this.isBurning())
                    {
                        setIgnited(true);
                        if (fuel.hasContainerItem()) this.items.set(1, fuel.getContainerItem());
                        else
                        {
                            fuel.shrink(1);
                            if (fuel.isEmpty()) this.items.set(1, fuel.getContainerItem());
                        }

                        this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(AbstractFurnaceBlock.LIT, this.isBurning()), 3);
                        markDirty();
                    }
                }
            }
            else Extinguish();
        }
    }
    public void Extinguish()
    {
        setIgnited(false);
    }
}
