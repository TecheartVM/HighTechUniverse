package techeart.htu.recipes;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.Map;

public class RecipesHelper
{
    public static Map<ResourceLocation, IRecipe<?>> getRecipes(IRecipeType<?> recipeType, RecipeManager manager)
    {
        Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipeMap = ObfuscationReflectionHelper.getPrivateValue(RecipeManager.class, manager, "field_199522_d");
        return recipeMap.get(recipeType);
    }
}
