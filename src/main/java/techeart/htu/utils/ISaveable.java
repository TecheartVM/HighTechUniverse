package techeart.htu.utils;

import net.minecraft.nbt.CompoundNBT;

public interface ISaveable
{
    boolean isDirty();
    CompoundNBT writeToNBT(CompoundNBT nbt);
    void readFromNBT(CompoundNBT nbt);
}
