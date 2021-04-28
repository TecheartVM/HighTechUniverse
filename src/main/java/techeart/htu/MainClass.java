package techeart.htu;

import com.google.common.collect.Maps;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.Util;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import techeart.htu.objects.pipe.IPipeGrid;
import techeart.htu.objects.tank.RendererFluidTank;
import techeart.htu.recipes.alloying.AlloyRecipes;
import techeart.htu.utils.FuelTemperatures;
import techeart.htu.utils.RegistryHandler;
import techeart.htu.utils.WorldGridsManager;
import techeart.htu.world.gen.OreGeneration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* GLOBAL TODO LIST:
           1. FISH!
           2. Remake GRID System (+pipes)
           3. Create Gas
           4. Clean up code
           5. Create GUI draw helper
           6. Pump animation + InputOutput system + New working algorithm
           7. Tank (Rotating + locking + stacking)
           8. Base classes
           9. Temperature system(s?)
           10. Universal bucket
           11. Wrench
*/
@Mod("htu")
@Mod.EventBusSubscriber
public class MainClass
{
    public static final String MODID = "htu";
    public static final String MOD_NAME = "High Tech Universe";

    public static final Logger LOGGER = LogManager.getLogger();

    public static final WorldGridsManager gridsManager = new WorldGridsManager();

    public MainClass()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(IRecipeSerializer.class, RegistryHandler::registerRecipeSerializers);

        RegistryHandler.init();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        OreGeneration.setupOreGenerator();
    }

    private void setupClient(final FMLClientSetupEvent event)
    {
        //register gui containers
        ScreenManager.registerFactory(RegistryHandler.SMELTERY.getContainer(), RegistryHandler.SMELTERY.getGui());
        ScreenManager.registerFactory(RegistryHandler.STEAM_BOILER.getContainer(), RegistryHandler.STEAM_BOILER.getGui());

        //register custom renderers
        RenderTypeLookup.setRenderLayer(RegistryHandler.BLOCK_FLUID_TANK.getPrimary(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(RegistryHandler.FLUID_TANK_TE.get(), RendererFluidTank::new);

        //register fluid render types
        final Map<Fluid, RenderType> FLUID_RENDER_TYPES = Util.make(Maps.newHashMap(), (map) -> {
            map.put(RegistryHandler.FLUID_STEAM.get(), RenderType.getTranslucent());
            map.put(RegistryHandler.FLUID_STEAM_FLOWING.get(), RenderType.getTranslucent());
        });

        FLUID_RENDER_TYPES.forEach(RenderTypeLookup::setRenderLayer);
    }

    @SubscribeEvent
    public void onRecipesUpdates(RecipesUpdatedEvent event)
    {
        AlloyRecipes.init(event.getRecipeManager());
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event)
    {
        FuelTemperatures.init();
    }
    @SubscribeEvent
    public void onServerStop(FMLServerStoppingEvent event)
    {
        gridsManager.reset();
    }
    @SubscribeEvent
    public void onWorldTick(FMLServerStartingEvent event) { gridsManager.onServerWorldTick(event.getServer().func_241755_D_()); }

    public static final ItemGroup CREATIVE_TAB_STEAM_AGE = new ItemGroup("htu.steam_creative_tab")
    {
        @Override
        public ItemStack createIcon() { return new ItemStack(RegistryHandler.STEAM_BOILER.getMainBlock().getItem()); }
    };

    public static final ItemGroup CREATIVE_TAB_PRIMAL_AGE = new ItemGroup("htu.primal_creative_tab")
    {
        @Override
        public ItemStack createIcon() { return new ItemStack(RegistryHandler.PRIMITIVE_FURNACE.getMainBlock().getItem()); }
    };

    private static final Set<IPipeGrid> worldPipeGrids = new HashSet<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        gridsManager.onServerTick();
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if(event.side.isServer() && event.phase == TickEvent.Phase.END)
        {
            gridsManager.onServerWorldTick(event.world);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(FMLServerStoppingEvent event)
    {
        gridsManager.reset();
    }
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~DebugZone~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

//    public static boolean registerPipeGrid(IPipeGrid grid) { return worldPipeGrids.add(grid); }
//    public static boolean unregisterPipeGrid(IPipeGrid grid) { return worldPipeGrids.remove(grid); }





//    @SubscribeEvent
//    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
//    {
//        System.out.println(HTUHooks.getAmbientTemperature(event.player.world, event.player.getPosition()));
//    }

//    @SubscribeEvent
//    public void onPlayerTick(TickEvent.PlayerTickEvent event)
//    {
//        if(!event.player.world.isRemote())
//        {
//            //System.out.println(((ServerChunkProvider)(event.player.world.getChunkProvider())).chunkManager.getLoadedChunkCount());
//        }
//
//        if(event.player.world.isRemote())
//        {
//            System.out.println(((ClientChunkProvider)(event.player.world.getChunkProvider())).getLoadedChunksCount());
//        }
//    }
}
