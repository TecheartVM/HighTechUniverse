package techeart.htu.objects.inventory.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

public class HTUFurnaceFuelSlot extends Slot
{
    public HTUFurnaceFuelSlot(IInventory inventoryIn, int index, int xPosition, int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack item) { return ForgeHooks.getBurnTime(item) > 0; }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return super.getItemStackLimit(stack);
    }
}
