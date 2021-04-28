package techeart.htu.objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import techeart.htu.objects.boiler.TileEntitySteamBoiler;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class TileEntityIgnitable extends TileEntity
{
    public static final Set<Item> IGNITION_TOOLS = new HashSet<>();
    static
    {
        IGNITION_TOOLS.add(Items.FLINT_AND_STEEL);
    }
    public static boolean isIgnitionTool(Item item) { return IGNITION_TOOLS.contains(item); }

    public static boolean interactWithIgnitable(TileEntityIgnitable tile, ItemStack heldItem)
    {
        if(TileEntityIgnitable.isIgnitionTool(heldItem.getItem()))
        {
            tile.world.playSound(null, tile.pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            tile.ignite();
            return true;
        }
        return false;
    }

    public TileEntityIgnitable(TileEntityType<?> tileEntityTypeIn) { super(tileEntityTypeIn); }

    /**Chance in percents to extinct the burning fire in furnace*/
    public static final int EXTINCTION_CHANCE = 3;
    protected final Random random = new Random();

    private boolean ignited = false;
    protected boolean setIgnited(boolean value)
    {
        if(ignited != value)
        {
            ignited = value;
            if(value) onIgnited();
            else onExtinguished();
            return true;
        }
        return false;
    }

    public boolean isIgnited() { return ignited; }

    public abstract void ignite();

    protected abstract void onIgnited();

    protected abstract void tickIgnition();

    public void extinguish()
    {
        setIgnited(false);
    }

    protected abstract void onExtinguished();
}
