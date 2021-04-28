package techeart.htu.objects.smeltery;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotSmelteryFuel extends Slot
{
    public SlotSmelteryFuel(IInventory inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack item) {
        return TileEntitySmeltery.isItemFuel(item);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return super.getItemStackLimit(stack);
    }
}
