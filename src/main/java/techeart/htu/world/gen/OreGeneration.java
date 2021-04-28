package techeart.htu.world.gen;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import techeart.htu.MainClass;
import techeart.htu.utils.RegistryHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class OreGeneration
{

    static List<ConfiguredFeature<?, ?>> OwerworldFeatureList = new ArrayList<>();

    public static void setupOreGenerator()
    {
        //add Features to the list
        OwerworldFeatureList.add(registerOreFeature("ore_copper",RegistryHandler.ORE_COPPER.getPrimary(),9,64,20));
        OwerworldFeatureList.add(registerOreFeature("ore_tin",RegistryHandler.ORE_TIN.getPrimary(),9,64,20));
        //success msg
        MainClass.LOGGER.info("OreFeature has been registered");

        StringBuilder biomesList= new StringBuilder();
        for (Map.Entry<RegistryKey<Biome>, Biome> biome : WorldGenRegistries.BIOME.getEntries()) {

            if (biome.getValue().getCategory().equals(Biome.Category.NETHER) || biome.getValue().getCategory().equals(Biome.Category.THEEND))
                return;
            for (ConfiguredFeature<?,?> feature: OwerworldFeatureList)
                addOres(biome.getValue(), feature);

            biomesList.append(biome.getValue().getCategory().getName()).append(" ");
        }
        //success msg (x2)
        MainClass.LOGGER.info("OreFeature(s) "+OwerworldFeatureList.toString() +" has been added to the " + biomesList.toString() + "biome(s)");
    }

    private static ConfiguredFeature<?, ?> registerOreFeature(String name_id, Block ore,int maxSize,int maxHeight,int veinsPerChunk)
    {
        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, name_id,
                Feature.ORE.withConfiguration(
                        new OreFeatureConfig(
                                OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD,
                                ore.getDefaultState(), maxSize)).range(maxHeight).square().chance(veinsPerChunk)); //TODO 'chance'???
    }

    //Thanks to https://github.com/cScotPlay/BasicNetherOres
    private static void addOres(Biome biome, ConfiguredFeature<?, ?> configuredFeature)
    {
        List<List<Supplier<ConfiguredFeature<?, ?>>>> biomeFeatures = new ArrayList<>(biome.getGenerationSettings().getFeatures());

        while (biomeFeatures.size() <=  GenerationStage.Decoration.UNDERGROUND_ORES.ordinal())
        {
            biomeFeatures.add(Lists.newArrayList());
        }
        List<Supplier<ConfiguredFeature<?, ?>>> features = new ArrayList<>(biomeFeatures.get( GenerationStage.Decoration.UNDERGROUND_ORES.ordinal()));

        features.add(() -> configuredFeature);

        biomeFeatures.set( GenerationStage.Decoration.UNDERGROUND_ORES.ordinal(), features);

        ObfuscationReflectionHelper.setPrivateValue(BiomeGenerationSettings.class, biome.getGenerationSettings(), biomeFeatures,
                "field_242484_f");
    }

}
