package techeart.htu.utils.registration;

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
import techeart.htu.CreativeTabs;
import techeart.htu.objects.BlockPropertyPatterns;
import techeart.htu.objects.HTUIconItem;
import techeart.htu.objects.boiler.*;
import techeart.htu.objects.conduits.pipe.BlockFluidConduit;
import techeart.htu.objects.conduits.pipe.FluidConduit;
import techeart.htu.objects.fluids.FluidSteam;
import techeart.htu.objects.furnace.BlockPrimitiveFurnace;
import techeart.htu.objects.furnace.TileEntityPrimitiveFurnace;
import techeart.htu.objects.pump.BlockWaterPump;
import techeart.htu.objects.pump.TileEntityWaterPump;
import techeart.htu.objects.sensors.level.BlockSensorFluidLevel;
import techeart.htu.objects.sensors.level.TileEntitySensorFluidLevel;
import techeart.htu.objects.sensors.temperature.BlockSensorTemperature;
import techeart.htu.objects.sensors.temperature.TileEntitySensorTemperature;
import techeart.htu.objects.smeltery.BlockSmeltery;
import techeart.htu.objects.smeltery.ContainerSmeltery;
import techeart.htu.objects.smeltery.GuiSmeltery;
import techeart.htu.objects.smeltery.TileEntitySmeltery;
import techeart.htu.objects.tank.BlockFluidTank;
import techeart.htu.objects.tank.TileEntityFluidTank;
import techeart.htu.objects.wrench.ItemWrench;
import techeart.htu.recipes.alloying.AlloyRecipe;
import techeart.htu.recipes.alloying.RecipeTypeAlloying;
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
    public static final DoubleRegisteredObject<Block,Item> ORE_COPPER = MDR.register("block_copper_ore",new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).needItem(CreativeTabs.STEAM_AGE));
    public static final DoubleRegisteredObject<Block,Item> ORE_TIN = MDR.register("block_tin_ore",new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockOre()).needItem(CreativeTabs.STEAM_AGE));


    //fluids
    public static final RegistryObject<Fluid> FLUID_STEAM = MDR.register("fluid_steam", FluidSteam.Source::new,ForgeRegistries.FLUIDS);
    public static final RegistryObject<Fluid> FLUID_STEAM_FLOWING = MDR.register("fluid_steam_flowing", FluidSteam.Flowing::new,ForgeRegistries.FLUIDS);



    /*~~~~~~~~~~~~~~~~~~~~~~~~~MACHINES~AREA~~~~~~~~~~~~~~~~~~~~~~~~~*/
    //primitive furnace
     public static final HTUMachine<TileEntityPrimitiveFurnace,?> PRIMITIVE_FURNACE = new HTUMachine.Builder("furnace").addBlock(new HTUBlock.Builder().setBlock(BlockPrimitiveFurnace::new).needItem(CreativeTabs.PRIMAL_AGE),TileEntityPrimitiveFurnace::new).build();

    //smeltery
    public static final HTUMachine<TileEntitySmeltery,ContainerSmeltery> SMELTERY = new HTUMachine.Builder("smeltery").addBlock(new HTUBlock.Builder().setBlock(BlockSmeltery::new).needItem(CreativeTabs.STEAM_AGE),TileEntitySmeltery::new).needContainer(ContainerSmeltery::new).needGUI(GuiSmeltery::new).build();

    //steam boiler
    public static final HTUMachine<TileEntitySteamBoiler,ContainerSteamBoiler> STEAM_BOILER = new HTUMachine.Builder("steam_boiler").addBlock(new HTUBlock.Builder().setBlock(BlockSteamBoiler::new).needItem(CreativeTabs.STEAM_AGE),TileEntitySteamBoiler::new).addBlock(new HTUBlock.Builder().setBlock(BlockSteamBoilerTop::new),TileEntitySteamBoilerTank::new).needContainer(ContainerSteamBoiler::new).needGUI(GuiSteamBoiler::new).build();

    //pump
    public static final HTUMachine<TileEntityWaterPump,?> WATER_PUMP = new HTUMachine.Builder("water_pump").addBlock(new HTUBlock.Builder().setBlock(BlockWaterPump::new).needItem(CreativeTabs.STEAM_AGE),TileEntityWaterPump::new).build();

            /*~~~~~~~~~~~~~~~~~~~~~CustomObjects~~~~~~~~~~~~~~~~~~*/
    //tank
    public static final DoubleRegisteredObject<Block,Item> BLOCK_FLUID_TANK = MDR.register("block_fluid_tank",new HTUBlock.Builder().setBlock(BlockFluidTank::new).needItem(CreativeTabs.STEAM_AGE));
    public static final RegistryObject<TileEntityType<TileEntityFluidTank>> FLUID_TANK_TE = MDR.register("fluid_tank", () -> TileEntityType.Builder.create(TileEntityFluidTank::new, RegistryHandler.BLOCK_FLUID_TANK.getPrimary()).build(null),ForgeRegistries.TILE_ENTITIES); //TODO: RegistryObject<TileEntityType<TileEntityFluidTank>>

    //pipe
    public static final DoubleRegisteredObject<Block,Item> FLUID_PIPE = MDR.register("block_pipe",new HTUBlock.Builder().setBlock(BlockFluidConduit::new).needItem(CreativeTabs.STEAM_AGE));
    public static final RegistryObject<TileEntityType<FluidConduit>> FLUID_PIPE_TE = MDR.register("pipe_fluid", () -> TileEntityType.Builder.create(FluidConduit::new, RegistryHandler.FLUID_PIPE.getPrimary()).build(null),ForgeRegistries.TILE_ENTITIES);



    //sensor fluid level
    public static final DoubleRegisteredObject<Block,Item> BLOCK_SENSOR_FLUID_LEVEL = MDR.register("block_sensor_fluid_level",new HTUBlock.Builder().setBlock(BlockSensorFluidLevel::new).needItem(CreativeTabs.STEAM_AGE));
    public static final RegistryObject<TileEntityType<TileEntitySensorFluidLevel>> SENSOR_FLUID_LEVEL_TE = MDR.register("sensor_fluid_level", () -> TileEntityType.Builder.create(TileEntitySensorFluidLevel::new, RegistryHandler.BLOCK_SENSOR_FLUID_LEVEL.getPrimary()).build(null),ForgeRegistries.TILE_ENTITIES);

    //sensor temperature
    public static final DoubleRegisteredObject<Block,Item> BLOCK_SENSOR_TEMPERATURE = MDR.register("block_sensor_temperature",new HTUBlock.Builder().setBlock(BlockSensorTemperature::new).needItem(CreativeTabs.STEAM_AGE));
    public static final RegistryObject<TileEntityType<TileEntitySensorTemperature>> SENSOR_TEMPERATURE_TE = MDR.register("sensor_temperature", () -> TileEntityType.Builder.create(TileEntitySensorTemperature::new, RegistryHandler.BLOCK_SENSOR_TEMPERATURE.getPrimary()).build(null),ForgeRegistries.TILE_ENTITIES);

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
        final RegistryObject<Item> INGOT_COPPER = MDR.register("ingot_copper", () -> new Item(new Item.Properties().group(CreativeTabs.STEAM_AGE)), ForgeRegistries.ITEMS);
        final RegistryObject<Item> INGOT_TIN = MDR.register("ingot_tin", () -> new Item(new Item.Properties().group(CreativeTabs.STEAM_AGE)), ForgeRegistries.ITEMS);
        final RegistryObject<Item> INGOT_BRONZE = MDR.register("ingot_bronze", () -> new Item(new Item.Properties().group(CreativeTabs.STEAM_AGE)), ForgeRegistries.ITEMS);

        final RegistryObject<Item> ITEM_WRENCH = MDR.register("wrench_copper", () -> new ItemWrench(250, 3, 0.7f), ForgeRegistries.ITEMS);

        //blocks
        final DoubleRegisteredObject<Block,Item> BLOCK_COPPER = MDR.register("block_copper", new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).needItem(CreativeTabs.STEAM_AGE));
        final DoubleRegisteredObject<Block,Item> BLOCK_TIN = MDR.register("block_tin", new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).needItem(CreativeTabs.STEAM_AGE));
        final DoubleRegisteredObject<Block,Item> BLOCK_BRONZE = MDR.register("block_bronze", new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).needItem(CreativeTabs.STEAM_AGE));

    }
}
