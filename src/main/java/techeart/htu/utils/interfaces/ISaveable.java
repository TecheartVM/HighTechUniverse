package techeart.htu.utils.interfaces;

import net.minecraft.nbt.CompoundNBT;

public interface ISaveable
{
    CompoundNBT write(CompoundNBT nbt);
    void read(CompoundNBT nbt);
}
