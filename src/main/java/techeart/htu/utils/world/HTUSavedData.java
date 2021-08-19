package techeart.htu.utils.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;
import techeart.htu.MainClass;

public class HTUSavedData extends WorldSavedData {
    public static final String DATA_IDENTIFIER = MainClass.MODID + "_data";
    HTUGridManager manager;

    public HTUSavedData()
    {
        super(DATA_IDENTIFIER);
    }

    @Override
    public boolean isDirty()
    {
        return manager.isDirty() || super.isDirty();
    }

    public void setManager(HTUGridManager manager) {
        this.manager = manager;
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        manager.read(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        return manager.write(nbt);
    }
}