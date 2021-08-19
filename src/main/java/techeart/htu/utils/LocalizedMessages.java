package techeart.htu.utils;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class LocalizedMessages
{
    public static ITextComponent getInfoFluidLevel(int fluid)
    {
        return new TranslationTextComponent("htu.infomsg.fluid_level").appendString(fluid + " mB");
    }

    public static ITextComponent getInfoTemperature(int temperature)
    {
        return new TranslationTextComponent("htu.infomsg.temperature").appendString(temperature + " " + ("°").substring(1) + "C");
    }
}
