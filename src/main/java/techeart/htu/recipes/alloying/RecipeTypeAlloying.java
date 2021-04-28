package techeart.htu.recipes.alloying;

import net.minecraft.item.crafting.IRecipeType;
import techeart.htu.MainClass;

public class RecipeTypeAlloying implements IRecipeType<AlloyRecipe>
{
    @Override
    public String toString()
    {
        return MainClass.MODID + ":alloying_recipe";
    }
}
