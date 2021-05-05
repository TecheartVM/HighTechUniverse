package techeart.htu.objects.grids;

import net.minecraft.nbt.CompoundNBT;

import java.util.UUID;

public interface IGrid
{
    UUID getId();
    void tick();
    boolean isDirty();
    void readFromNBT(CompoundNBT nbt);
    CompoundNBT writeToNBT(CompoundNBT nbt);
}
