package techeart.htu.utils;

import net.minecraftforge.fluids.capability.templates.FluidTank;

public class DebugUtils {
    public static String getFluidInfo (FluidTank tank)
    {
        return "Fluid: "+ tank.getFluid().getTranslationKey() +"\t Amount: "+tank.getFluidAmount();
    }
}
