package techeart.htu.recipes.alloying;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import techeart.htu.recipes.RecipesHelper;
import techeart.htu.utils.InventoryHelper;
import techeart.htu.utils.RegistryHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class AlloyRecipes
{
    private static final ArrayList<AlloyRecipe> LIST = new ArrayList<>();

    public static ArrayList<AlloyRecipe> getList() { return LIST; }

    public static void init(RecipeManager recipeManager)
    {
        for(Map.Entry<ResourceLocation, IRecipe<?>> entry : RecipesHelper.getRecipes(RegistryHandler.RECIPE_TYPE_ALLOYING, recipeManager).entrySet())
            if(entry.getValue() instanceof AlloyRecipe)
                LIST.add((AlloyRecipe) entry.getValue());

        sortList();
    }

    public static AlloyRecipe getValidRecipe(int tier, NonNullList<ItemStack> inventory, int startIndex, int inputSlotsCount)
    {
        for(AlloyRecipe recipe : LIST)
        {
            if(tier < recipe.tier) break;
            if(InventoryHelper.takeItems(inventory, recipe.ingredientsList, false, startIndex, inputSlotsCount))
                return recipe;
        }
        return null;
    }

    /*recipes list sorting logic*/
    public static void sortList()
    {
        LIST.sort(byTier.thenComparing(bySameItemsCount.reversed()));
    }

    public static Comparator<AlloyRecipe> byTier = Comparator.comparingInt(o -> o.tier);

    public static Comparator<AlloyRecipe> bySameItemsCount = (o1, o2) -> {
        int result = 0;
        for (ItemStack input1 : o1.ingredientsList)
            for (ItemStack input2 : o2.ingredientsList)
                if(input1.isItemEqual(input2))
                    result += Integer.compare(input1.getCount(), input2.getCount());

        return Integer.compare(result, 0);
    };
}

