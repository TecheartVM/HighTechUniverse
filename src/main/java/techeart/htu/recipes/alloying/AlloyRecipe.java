package techeart.htu.recipes.alloying;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import techeart.htu.MainClass;
import techeart.htu.utils.InventoryHelper;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class AlloyRecipe implements IRecipe<IInventory>
{
    public static final Serializer SERIALIZER = new Serializer();

    public final ResourceLocation id;

    public final ItemStack result;
    public final ArrayList<ItemStack> ingredientsList;
    public final int tier;
    public final int time;
    public final float experience;

    public AlloyRecipe(ResourceLocation id, ItemStack result, ArrayList<ItemStack> ingredientsList, int tier, int time, float experience)
    {
        this.id = id;
        this.result = result;
        this.ingredientsList = ingredientsList;
        this.tier = tier;
        this.time = time;
        this.experience = experience;
    }

    @Override
    public boolean isDynamic() { return true; } //If true, this recipe does not appear in the recipe book

    @Override
    public boolean matches(IInventory inv, World worldIn) { return false; }

    @Override
    public ItemStack getCraftingResult(IInventory inv) { return result.copy(); }

    @Override
    public boolean canFit(int width, int height) { return false; }

    @Override
    public ItemStack getRecipeOutput() { return result; }

    @Override
    public ResourceLocation getId() { return id; }

    @Override
    public IRecipeSerializer<?> getSerializer() { return SERIALIZER; }

    @Override
    public String toString()
    {
        return "AlloyRecipe {" +
                "id=" + id +
                ", result=" + result +
                ", ingredients: " + ingredientsList +
                ", tier=" + tier +
                ", time=" + time +
                ", experience=" + experience +
                '}';
    }

    @Override
    public IRecipeType<?> getType() { return RegistryHandler.RECIPE_TYPE_ALLOYING; }

    private static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<AlloyRecipe>
    {
        public Serializer() { setRegistryName(new ResourceLocation(MainClass.MODID, "alloying_recipe")); }

        @Override
        public AlloyRecipe read(ResourceLocation recipeId, JsonObject json)
        {
            ItemStack result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
            int tier = JSONUtils.getInt(json, "tier");
            int time = JSONUtils.getInt(json, "time");
            float exp = JSONUtils.getFloat(json, "experience");

            JsonElement element;
            ArrayList<ItemStack> ingredients = new ArrayList();
            if(JSONUtils.isJsonArray(json, "ingredients"))
            {
                element = JSONUtils.getJsonArray(json, "ingredients");
                for (JsonElement item : element.getAsJsonArray())
                    ingredients.add(ShapedRecipe.deserializeItem(item.getAsJsonObject()));
            }
            else
            {
                element = JSONUtils.getJsonObject(json, "ingredients");
                ingredients.add(ShapedRecipe.deserializeItem(element.getAsJsonObject()));
            }

            return new AlloyRecipe(recipeId, result, ingredients, tier, time, exp);
        }

        @Nullable
        @Override
        public AlloyRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
        {
            ArrayList<ItemStack> ingredients = new ArrayList<>();
            int listLength = buffer.readVarInt();
            for (int i = 0; i < listLength; i++)
                ingredients.add(buffer.readItemStack());

            ItemStack result = buffer.readItemStack();

            int tier = buffer.readVarInt();
            int time = buffer.readInt();
            float exp = buffer.readFloat();

            return new AlloyRecipe(recipeId, result, ingredients, tier, time, exp);
        }

        @Override
        public void write(PacketBuffer buffer, AlloyRecipe recipe)
        {
            buffer.writeVarInt(recipe.ingredientsList.size());
            for (ItemStack item: recipe.ingredientsList)
                buffer.writeItemStack(item);

            buffer.writeItemStack(recipe.result);

            buffer.writeVarInt(recipe.tier);
            buffer.writeInt(recipe.time);
            buffer.writeFloat(recipe.experience);
        }
    }
}
