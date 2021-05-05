package techeart.htu.objects.grids;

import net.minecraft.util.Direction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GridPeripheralsHolder
{
    public final Set<Connection> bottom = new HashSet<>();
    public final Set<Connection> side = new HashSet<>();
    public final Set<Connection> top = new HashSet<>();

    public boolean add(Collection<Connection> peripherals)
    {
        boolean flag = false;
        for (Connection p : peripherals)
        {
            if(p.side.getAxis().isHorizontal()) flag |= side.add(p);
            else if(p.side == Direction.UP) flag |= top.add(p);
            else flag |= bottom.add(p);
        }
        return flag;
    }

    public boolean mergeWith(GridPeripheralsHolder holder)
    {
        boolean flag;
        flag = bottom.addAll(holder.bottom);
        flag |= side.addAll(holder.side);
        flag |= top.addAll(holder.top);
        return flag;
    }

    public boolean remove(Connection peripheral)
    {
        boolean flag;
        if(peripheral.side.getAxis().isHorizontal()) flag = side.remove(peripheral);
        else if(peripheral.side == Direction.UP) flag = top.remove(peripheral);
        else flag = bottom.remove(peripheral);
        return flag;
    }

    public boolean removeByParent(IFluidConduit parent)
    {
        boolean flag;
        flag = bottom.removeIf(c -> c.parent == parent);
        flag |= side.removeIf(c -> c.parent == parent);
        flag |= top.removeIf(c -> c.parent == parent);
        return flag;
    }

    public void clear()
    {
        bottom.clear();
        side.clear();
        top.clear();
    }
}
