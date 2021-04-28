package techeart.htu.utils.registration;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

//TODO: Complete this
public class HTUMetal {
    RegistryObject<Item> ingot;
    RegistryObject<Item> plate;
    RegistryObject<Item> rod;
    RegistryObject<Item> dust;
    RegistryObject<Item> nugget;

    public static class Builder{
        private boolean needIngot;
        private boolean needPlate;
        private boolean needRod;
        private boolean needDust;
        private boolean needNugget;
        public HTUMetal.Builder needIngot()
        {
            needIngot = true;
            return this;
        }
        public HTUMetal.Builder needPlate()
        {
            needPlate = true;
            return this;
        }
        public HTUMetal.Builder needRod()
        {
            needRod = true;
            return this;
        }
        public HTUMetal.Builder needDust()
        {
            needDust = true;
            return this;
        }
        public HTUMetal.Builder needNugget()
        {
            needNugget = true;
            return this;
        }
        public  HTUMetal.Builder needALL()
        {
            needIngot = true;
            needDust = true;
            needNugget = true;
            needPlate = true;
            needRod = true;
            return this;
        }



    }
}
