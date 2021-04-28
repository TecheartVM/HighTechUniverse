package techeart.htu.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;

public class InventoryHelper
{
    public static ArrayList<ItemStack> getAsArrayList(NonNullList<ItemStack> inventory) { return getAsArrayList(inventory, 0, inventory.size()); }
    public static ArrayList<ItemStack> getAsArrayList(NonNullList<ItemStack> inventory, int firstIndex, int slotsCount)
    {
        ArrayList<ItemStack> result = new ArrayList<>();
        for (int i = firstIndex; i < firstIndex + slotsCount; i++)
        {
            result.add(inventory.get(i));
        }
        return result;
    }

    public static int getItemAmount(NonNullList<ItemStack> inventory, Item item) { return getItemAmount(inventory, item, 0, inventory.size()); }
    public static int getItemAmount(NonNullList<ItemStack> inventory, Item item, int firstIndex, int slotsCount)
    {
        int result = 0;
        for (int i = firstIndex; i < firstIndex + slotsCount; i++)
        {
            if(inventory.get(i).getItem() == item) result += inventory.get(i).getCount();
        }
        return result;
    }

    public static boolean takeStack(NonNullList<ItemStack> inventory, ItemStack stack, boolean consume) { return takeStack(inventory, stack, consume, 0, inventory.size()); }
    public static boolean takeStack(NonNullList<ItemStack> inventory, ItemStack stack, boolean consume, int firstIndex, int slotsCount)
    {
        ArrayList<ItemStack> list = new ArrayList<>();
        list.add(stack);
        return takeItems(inventory, list, consume, firstIndex, slotsCount);
    }

    public static boolean takeItems(NonNullList<ItemStack> inventory, ArrayList<ItemStack> items, boolean consume) { return takeItems(inventory, items, consume, 0, inventory.size()); }
    public static boolean takeItems(NonNullList<ItemStack> inventory, ArrayList<ItemStack> items, boolean consume, int firstIndex, int slotsCount)
    {
        if(items.isEmpty()) return false;

        NonNullList<ItemStack> newList = NonNullList.create();
        inventory.forEach(item -> newList.add(item));
        int itemCount;
        boolean notFound;
        for (int j = 0; j < items.size(); j++)
        {
            if(items.get(j).isEmpty()) continue;

            notFound = true;
            itemCount = items.get(j).getCount();
            for (int i = firstIndex; i < firstIndex + slotsCount; i++)
            {
                if(inventory.get(i).isItemEqual(items.get(j)))
                {
                    itemCount = inventory.get(i).getCount() - itemCount;
                    if(itemCount < 0)
                    {
                        itemCount = Math.abs(itemCount);
                        newList.set(i, ItemStack.EMPTY);
                    }
                    else if(itemCount >= 0)
                    {
                        notFound = false;
                        newList.set(i, itemCount == 0 ? ItemStack.EMPTY : new ItemStack (inventory.get(i).getItem(), itemCount));
                        break;
                    }
                }
            }
            if(notFound) return false;
        }

        if(consume)
        {
            System.out.println(newList);
            for (int i = firstIndex; i < firstIndex + slotsCount; i++)
            {
                inventory.set(i, newList.get(i));
            }
        }

        return true;
    }
}
