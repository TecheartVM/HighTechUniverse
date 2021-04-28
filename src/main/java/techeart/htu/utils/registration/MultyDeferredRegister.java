package techeart.htu.utils.registration;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import techeart.htu.MainClass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

//TODO: FIX BUG (size = 0 ?!)
public class MultyDeferredRegister {
    private Map<IForgeRegistry<?>, DeferredRegister<? extends IForgeRegistryEntry<?>>> registerMap;

    public MultyDeferredRegister(IForgeRegistry<?>[] type) {
        this.registerMap = new HashMap<>(type.length);
        for (int i = 0; i < type.length; i++)
            registerMap.put(type[i], DeferredRegister.create(type[i], MainClass.MODID));
    }

    public void register(IEventBus bus) {
        registerMap.forEach((key, value) -> value.register(bus));
    }

    //TODO: Remove type
    public <I extends T, T extends IForgeRegistryEntry<T>> RegistryObject<I> register(String name, Supplier<? extends I> supplier, IForgeRegistry<T> type) {
        return ((DeferredRegister<T>) registerMap.get(type)).register(name, supplier);
    }

    public DoubleRegisteredObject<Block, Item> register(String name, HTUBlock.Builder blockBuilder)
    {
        RegistryObject<Block> o1 = ((DeferredRegister<Block>) registerMap.get(ForgeRegistries.BLOCKS)).register(name,blockBuilder.build());

        if(blockBuilder.isNeedItem()) {
            if (blockBuilder.getGroup() == null)
                return new DoubleRegisteredObject<>(o1, ((DeferredRegister<Item>) registerMap.get(ForgeRegistries.ITEMS)).register(name, () -> new BlockItem(o1.get(), new Item.Properties().group(ItemGroup.MISC))));
            return new DoubleRegisteredObject<>(o1, ((DeferredRegister<Item>) registerMap.get(ForgeRegistries.ITEMS)).register(name, () -> new HTUItem.Builder().block(o1.get()).itemGroup(blockBuilder.getGroup()).build()));
        }
        return new DoubleRegisteredObject<>(o1,null);
    }
    public <A extends Item> RegistryObject<A> register(String name, Supplier<A> supplier)
    {
        DeferredRegister<Item> a = (DeferredRegister<Item>) registerMap.get(ForgeRegistries.ITEMS);
        return a.register(name,supplier);
    }

}