package techeart.htu.objects.fluids;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import techeart.htu.MainClass;
import techeart.htu.utils.RegistryHandler;

public abstract class FluidSteam
{
    private static final FluidAttributes.Builder ATTRS = FluidAttributes.builder(
            new ResourceLocation(MainClass.MODID, "blocks/fluid_steam_still"),
            new ResourceLocation(MainClass.MODID, "blocks/fluid_steam_flow"))
            .viscosity(20).temperature(1000).density(-1000)
            .color(0x22FFFFFF)
            .translationKey("block." + MainClass.MODID + "steam");

    private static final ForgeFlowingFluid.Properties PROPS = new ForgeFlowingFluid.Properties(
            RegistryHandler.FLUID_STEAM,
            RegistryHandler.FLUID_STEAM_FLOWING,
            ATTRS)/*.block(RegistryHandler.BLOCK_FLUID_STEAM)*/;

    public static final FluidSteam.Source STEAM = new Source();
    public static final FluidSteam.Flowing STEAM_FLOWING = new Flowing();

    public static class Source extends ForgeFlowingFluid.Source
    {
        public Source()
        {
            super(PROPS);
        }

        @Override
        public int getTickRate(IWorldReader world) { return 2; }

        public static FluidAttributes getAttributesForGui()
        {
            return ATTRS.color(0xFFFFFFFF).build(STEAM);
        }

    }

    public static class Flowing extends ForgeFlowingFluid.Flowing
    {
        public Flowing()
        {
            super(PROPS);
        }
        @Override
        protected boolean canDisplace(FluidState state, IBlockReader world, BlockPos pos, Fluid fluidIn, Direction direction)
        {
            return direction == Direction.UP && !isEquivalentTo(fluidIn);
        }

    }
}
