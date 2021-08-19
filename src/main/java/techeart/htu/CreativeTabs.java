package techeart.htu;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import techeart.htu.utils.registration.RegistryHandler;

public class CreativeTabs {

    public static final ItemGroup STEAM_AGE = new ItemGroup("htu.steam_creative_tab")
    {
        @Override
        public ItemStack createIcon() { return new ItemStack(RegistryHandler.STEAM_BOILER.getMainBlock().getItem()); }
    };

    public static final ItemGroup PRIMAL_AGE = new ItemGroup("htu.primal_creative_tab")
    {
        @Override
        public ItemStack createIcon() { return new ItemStack(RegistryHandler.PRIMITIVE_FURNACE.getMainBlock().getItem()); }
    };

}
