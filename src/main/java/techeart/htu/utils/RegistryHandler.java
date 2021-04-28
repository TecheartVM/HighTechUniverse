package techeart.htu.utils;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import techeart.htu.MainClass;
import techeart.htu.objects.BlockPropertyPatterns;
import techeart.htu.objects.HTUIconItem;
import techeart.htu.objects.boiler.*;
import techeart.htu.objects.fluids.FluidSteam;
import techeart.htu.objects.furnace.BlockPrimitiveFurnace;
import techeart.htu.objects.furnace.TileEntityPrimitiveFurnace;
import techeart.htu.objects.pipe.BlockPipeFluid;
import techeart.htu.objects.pipe.TileEntityPipeFluid;
import techeart.htu.objects.pump.BlockWaterPump;
import techeart.htu.objects.pump.TileEntityWaterPump;
import techeart.htu.objects.smeltery.BlockSmeltery;
import techeart.htu.objects.smeltery.ContainerSmeltery;
import techeart.htu.objects.smeltery.GuiSmeltery;
import techeart.htu.objects.smeltery.TileEntitySmeltery;
import techeart.htu.objects.tank.BlockFluidTank;
import techeart.htu.objects.tank.TileEntityFluidTank;
import techeart.htu.recipes.alloying.AlloyRecipe;
import techeart.htu.recipes.alloying.RecipeTypeAlloying;
import techeart.htu.utils.registration.DoubleRegisteredObject;
import techeart.htu.utils.registration.HTUBlock;
import techeart.htu.utils.registration.MultyDeferredRegister;
import techeart.htu.utils.registration.machine.HTUMachine;

public class RegistryHandler
{
    public static final MultyDeferredRegister MDR = new MultyDeferredRegister(new IForgeRegistry[]{
            ForgeRegistries.TILE_ENTITIES,
            ForgeRegistries.BLOCKS,
            ForgeRegistries.ITEMS,
            ForgeRegistries.FLUIDS,
            ForgeRegistries.CONTAINERS
    });

    public static void init()
    {
        MDR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    //ores
    public static final DoubleRegisteredObject<Block,Item> ORE_COPPER = MDR.register("block_copper_ore",new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).needItem(MainClass.CREATIVE_TAB_STEAM_AGE));
    public static final DoubleRegisteredObject<Block,Item> ORE_TIN = MDR.register("block_tin_ore",new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockOre()).needItem(MainClass.CREATIVE_TAB_STEAM_AGE));


    //fluids
    public static final RegistryObject<Fluid> FLUID_STEAM = MDR.register("fluid_steam", FluidSteam.Source::new,ForgeRegistries.FLUIDS);
    public static final RegistryObject<Fluid> FLUID_STEAM_FLOWING = MDR.register("fluid_steam_flowing", FluidSteam.Flowing::new,ForgeRegistries.FLUIDS);



    /*~~~~~~~~~~~~~~~~~~~~~~~~~MACHINES~AREA~~~~~~~~~~~~~~~~~~~~~~~~~*/
    //primitive furnace
     public static final HTUMachine<TileEntityPrimitiveFurnace,?> PRIMITIVE_FURNACE = new HTUMachine.Builder("furnace").addBlock(new HTUBlock.Builder().setBlock(BlockPrimitiveFurnace::new).needItem(MainClass.CREATIVE_TAB_PRIMAL_AGE),TileEntityPrimitiveFurnace::new).build();

    //smeltery
    public static final HTUMachine<TileEntitySmeltery,ContainerSmeltery> SMELTERY = new HTUMachine.Builder("smeltery").addBlock(new HTUBlock.Builder().setBlock(BlockSmeltery::new).needItem(MainClass.CREATIVE_TAB_STEAM_AGE),TileEntitySmeltery::new).needContainer(ContainerSmeltery::new).needGUI(GuiSmeltery::new).build();

    //steam boiler
    public static final HTUMachine<TileEntitySteamBoiler,ContainerSteamBoiler> STEAM_BOILER = new HTUMachine.Builder("steam_boiler").addBlock(new HTUBlock.Builder().setBlock(BlockSteamBoiler::new).needItem(MainClass.CREATIVE_TAB_STEAM_AGE),TileEntitySteamBoiler::new).addBlock(new HTUBlock.Builder().setBlock(BlockSteamBoilerTop::new),null).needContainer(ContainerSteamBoiler::new).needGUI(GuiSteamBoiler::new).build();

    //pump
    public static final HTUMachine<TileEntityWaterPump,?> WATER_PUMP = new HTUMachine.Builder("water_pump").addBlock(new HTUBlock.Builder().setBlock(BlockWaterPump::new).needItem(MainClass.CREATIVE_TAB_STEAM_AGE),TileEntityWaterPump::new).build();

            /*~~~~~~~~~~~~~~~~~~~~~CustomObjects~~~~~~~~~~~~~~~~~~*/
    //tank
    public static final DoubleRegisteredObject<Block,Item> BLOCK_FLUID_TANK = MDR.register("block_fluid_tank",new HTUBlock.Builder().setBlock(BlockFluidTank::new).needItem(MainClass.CREATIVE_TAB_STEAM_AGE));
    public static final RegistryObject<TileEntityType<TileEntityFluidTank>> FLUID_TANK_TE = MDR.register("fluid_tank", () -> TileEntityType.Builder.create(TileEntityFluidTank::new, RegistryHandler.BLOCK_FLUID_TANK.getPrimary()).build(null),ForgeRegistries.TILE_ENTITIES); //TODO: RegistryObject<TileEntityType<TileEntityFluidTank>>

    //pipe
    public static final DoubleRegisteredObject<Block,Item> BLOCK_PIPE = MDR.register("block_pipe",new HTUBlock.Builder().setBlock(BlockPipeFluid::new).needItem(MainClass.CREATIVE_TAB_STEAM_AGE));
    public static final RegistryObject<TileEntityType<TileEntityPipeFluid>> FLUID_PIPE_TE = MDR.register("pipe_fluid", () -> TileEntityType.Builder.create(TileEntityPipeFluid::new, RegistryHandler.BLOCK_PIPE.getPrimary()).build(null),ForgeRegistries.TILE_ENTITIES);

            /*~~~~~~~~~~~~~~~~~~~~~MISC~~~~~~~~~~~~~~~~~~*/
    public static final IRecipeType<AlloyRecipe> RECIPE_TYPE_ALLOYING = new RecipeTypeAlloying();
    public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(RECIPE_TYPE_ALLOYING.toString()), RECIPE_TYPE_ALLOYING);
        event.getRegistry().register(AlloyRecipe.SERIALIZER);
    }


    static
    {
        //icons
        final RegistryObject<Item> ICON_SMORC = MDR.register("icon_smorc",HTUIconItem::new);

        //items
        final RegistryObject<Item> INGOT_COPPER = MDR.register("ingot_copper", () -> new Item(new Item.Properties().group(MainClass.CREATIVE_TAB_STEAM_AGE)), ForgeRegistries.ITEMS);
        final RegistryObject<Item> INGOT_TIN = MDR.register("ingot_tin", () -> new Item(new Item.Properties().group(MainClass.CREATIVE_TAB_STEAM_AGE)), ForgeRegistries.ITEMS);
        final RegistryObject<Item> INGOT_BRONZE = MDR.register("ingot_bronze", () -> new Item(new Item.Properties().group(MainClass.CREATIVE_TAB_STEAM_AGE)), ForgeRegistries.ITEMS);

        //blocks
        final DoubleRegisteredObject<Block,Item> BLOCK_COPPER = MDR.register("block_copper", new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).needItem(MainClass.CREATIVE_TAB_STEAM_AGE));
        final DoubleRegisteredObject<Block,Item> BLOCK_TIN = MDR.register("block_tin", new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).needItem(MainClass.CREATIVE_TAB_STEAM_AGE));
        final DoubleRegisteredObject<Block,Item> BLOCK_BRONZE = MDR.register("block_bronze", new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).needItem(MainClass.CREATIVE_TAB_STEAM_AGE));

    }
}
