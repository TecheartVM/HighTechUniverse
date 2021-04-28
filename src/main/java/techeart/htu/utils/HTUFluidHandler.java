package techeart.htu.utils;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techeart.htu.objects.HTUFluidTank;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

public class HTUFluidHandler implements IFluidHandler
{
    ArrayList<FluidTank> tanks = new ArrayList<>();

    public HTUFluidHandler(FluidTank... internalTanks) { tanks.addAll(Arrays.asList(internalTanks)); }

    public void read(CompoundNBT compound)
    {
        for (int i = 0; i < tanks.size(); i++)
            tanks.get(i).readFromNBT(compound.getCompound("Tank" + i));
    }

    public CompoundNBT write(CompoundNBT compound)
    {
        for (int i = 0; i < tanks.size(); i++)
            compound.put("Tank" + i, tanks.get(i).writeToNBT(new CompoundNBT()));
        return compound;
    }

    @Override
    public int getTanks() { return tanks.size(); }

    public FluidTank getTank(int tank) { return tanks.get(tank); }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return tanks.get(tank).getFluid(); }

    @Override
    public int getTankCapacity(int tank) { return tanks.get(tank).getCapacity(); }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return tanks.get(tank).isFluidValid(stack); }

    @Override
    public int fill(FluidStack resource, FluidAction action) { return fill(resource, action, false); }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) { return drain(resource, action, false); }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        FluidStack result = FluidStack.EMPTY;
        for (FluidTank tank : tanks)
        {
            if(result.isEmpty() || tank.getFluid().getAmount() > result.getAmount())
                result = tank.drain(maxDrain, action);
            if(result.getAmount() >= maxDrain) return result;
        }
        return result;
    }

    public FluidStack drain(Fluid resource, int maxDrain, FluidAction action) { return drain(new FluidStack(resource, maxDrain), action); }

    /**Fills valid tank ignoring it's {@link techeart.htu.objects.HTUFluidTank.Type}*/
    public int forceFill(FluidStack resource, FluidAction action) { return fill(resource, action, true); }
    /**Drains valid tank ignoring it's {@link techeart.htu.objects.HTUFluidTank.Type}*/
    public FluidStack forceDrain(FluidStack resource, FluidAction action) { return drain(resource, action, true); }

    protected int fill(FluidStack resource, FluidAction action, boolean force)
    {
        int canFill = resource.getAmount();
        int filled = 0;
        for (FluidTank tank : tanks)
            if(tank.isFluidValid(resource))
            {
                int i;
                if(force && tank instanceof HTUFluidTank) i = ((HTUFluidTank)tank).forceFill(new FluidStack(resource, canFill), action);
                else i = tank.fill(new FluidStack(resource, canFill), action);

                filled += i;
                canFill -= i;
                if(canFill <= 0) break;
            }
        return Math.min(filled, resource.getAmount());
    }

    protected FluidStack drain(FluidStack resource, FluidAction action, boolean force)
    {
        int canDrain = resource.getAmount();
        int drained = 0;
        for (FluidTank tank : tanks)
            if(tank.isFluidValid(resource) && !tank.isEmpty())
            {
                int i;
                if(force && tank instanceof HTUFluidTank) i = ((HTUFluidTank)tank).forceDrain(new FluidStack(resource, canDrain), action).getAmount();
                else i = tank.drain(new FluidStack(resource, canDrain), action).getAmount();

                drained += i;
                canDrain -= i;
                if(canDrain <= 0) break;
            }
        if(drained == 0)
            return FluidStack.EMPTY;
        return new FluidStack(resource, drained);
    }
}
