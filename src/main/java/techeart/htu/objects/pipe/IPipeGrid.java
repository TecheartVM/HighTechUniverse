package techeart.htu.objects.pipe;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import techeart.htu.utils.ISaveable;

import java.util.UUID;

public interface IPipeGrid extends ISaveable
{
    boolean isEmpty();
    boolean isFull();

    boolean addPipe(IPipe pipe);

    void tick();

    void markDirty();
    boolean isDirty();
    CompoundNBT writeToNBT(CompoundNBT nbt);
    void readFromNBT(CompoundNBT nbt);

    UUID getId();

    FluidStack destroy();
}
