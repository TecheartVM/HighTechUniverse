package techeart.htu.objects.boiler;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import techeart.htu.objects.HTUFluidTank;
import techeart.htu.utils.HTUHooks;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntitySteamBoilerTank extends TileEntity implements ITickableTileEntity
{
    public static final int conversionTemperature = 100;

    //internal tanks volume constants in mB
    public static final int internalVolumeWater = 4000;
    public static final int internalVolumeSteam = 4000;

    //pressure constants
    public static final int maxPressure = 640;
    public static final int initialPressure = 40;
    public static final int ejectionPressure = 560;

    public static final int waterConsumptionRate = 1;
    public static final int conversionFactor = 3;

    //fluid constant links
    private static final Fluid WATER = Fluids.WATER;
    private static final Fluid STEAM = RegistryHandler.FLUID_STEAM.get();

    //internal tanks
    private final HTUFluidTank tankWater;
    private final HTUFluidTank tankSteam;

    private int temperature;
    private int pressure;

    public TileEntitySteamBoilerTank()
    {
        super(RegistryHandler.STEAM_BOILER.getMachineBlock(1).getMachineTile());

        tankWater = new HTUFluidTank(internalVolumeWater, WATER, HTUFluidTank.Type.INSERT_ONLY);
        int steamVolume = internalVolumeSteam + (Math.floorDiv(internalVolumeSteam, ejectionPressure) * (maxPressure - ejectionPressure));
        tankSteam = new HTUFluidTank(steamVolume, STEAM, HTUFluidTank.Type.EJECT_ONLY);

        pressure = initialPressure;
        temperature = HTUHooks.getAmbientTemperature(world, pos);
    }


    /*ITickableTileEntity*/
    @Override
    public void tick()
    {

    }

    /*TileEntity*/
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap)
    {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
             return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> tankWater));
        return super.getCapability(cap);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            if(side != Direction.UP) return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> tankWater));
            else return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> tankSteam));
        return super.getCapability(cap, side);
    }
}
