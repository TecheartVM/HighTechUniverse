package techeart.htu.objects;

import net.minecraft.fluid.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class HTUFluidTank extends FluidTank
{
    private Type type;

    public HTUFluidTank(int capacity, Type type)
    {
        super(capacity);
        this.type = type;
    }

    public HTUFluidTank(int capacity, Predicate<FluidStack> validator, Type type)
    {
        super(capacity, validator);
        this.type = type;
    }

    public HTUFluidTank(int capacity, Fluid acceptedFluid, Type type) { this(capacity, content -> content.getFluid().equals(acceptedFluid), type); }
    public HTUFluidTank(int capacity) { this(capacity, Type.DEFAULT); }
    public HTUFluidTank(int capacity, Predicate<FluidStack> validator) { this(capacity, validator, Type.DEFAULT); }
    public HTUFluidTank(int capacity, Fluid acceptedFluid) { this(capacity, content -> content.getFluid().equals(acceptedFluid), Type.DEFAULT); }

    public Type getType() { return type; }

    public void setType(Type type) { this.type = type; }

    public boolean isFull() { return fluid.getAmount() >= capacity; }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        if(type == Type.EJECT_ONLY) return 0;
        return super.fill(resource, action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        if(type == Type.INSERT_ONLY) return FluidStack.EMPTY;
        return super.drain(maxDrain, action);
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        if(type == Type.INSERT_ONLY) return FluidStack.EMPTY;
        return super.drain(resource, action);
    }

    /**Fills valid tank ignoring it's {@link techeart.htu.objects.HTUFluidTank.Type}*/
    public int forceFill(FluidStack resource, FluidAction action) { return super.fill(resource, action); }
    /**Drains valid tank ignoring it's {@link techeart.htu.objects.HTUFluidTank.Type}*/
    public FluidStack forceDrain(FluidStack resource, FluidAction action) { return super.drain(resource, action); }
    /**Drains valid tank ignoring it's {@link techeart.htu.objects.HTUFluidTank.Type}*/
    public FluidStack forceDrain(int maxDrain, FluidAction action) { return super.drain(maxDrain, action); }

    public enum Type
    {
        DEFAULT,
        INSERT_ONLY,
        EJECT_ONLY
    }
}
