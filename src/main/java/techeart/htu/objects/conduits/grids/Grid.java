package techeart.htu.objects.conduits.grids;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import techeart.htu.MainClass;
import techeart.htu.objects.conduits.AbstractConduit;
import techeart.htu.utils.interfaces.ISaveable;
import techeart.htu.utils.interfaces.ITickable;

import java.util.ArrayList;
import java.util.UUID;

public abstract class Grid<Conduit extends AbstractConduit> implements ISaveable, ITickable {
    public UUID id;
    protected ArrayList<GridComponent<Conduit>> components = new ArrayList<>();

    public void addComponent(Conduit conduit)
    {
        GridComponent<Conduit> component= getComponent(conduit);
        if (component != null) {
            component.getConduit().nullGrid();
            this.components.remove(component);
        }
        this.components.add(new GridComponent<>(conduit));
        onComponentAdded(component);
        onComponentsChanged(component);
    }

    protected void onComponentAdded(GridComponent<Conduit> component) {
    }

    protected void onComponentNeighbourChanged(Direction dir, TileEntity tile){
    }

    public void removeComponent(AbstractConduit conduit) {
        GridComponent<Conduit> component = getComponent(conduit);
        if (component != null) {
            components.remove(component);
            onComponentRemove((Conduit) conduit,component);
            onComponentsChanged(component);
            if (components.isEmpty()) {
                remove();
            }
        }
    }

    protected void onComponentsChanged(GridComponent<Conduit> component) {
    }

    @Override
    public void HTUTick() {
        MainClass.LOGGER.error("You have to OVERRIDE 'TICK'(or remove 'super') method in your grid! ");
    }

    protected void remove() {
        this.components.clear();
        onGridRemove();
    }

    protected void onComponentRemove(Conduit conduit,GridComponent<Conduit> component) {
    }

    protected void onGridRemove() {
    }


    public UUID getID() {
        return id;
    }

    public GridComponent<Conduit> getComponent(AbstractConduit conduit)
    {
        GridComponent<Conduit> result = null;
        if( conduit != null)
            for(GridComponent<Conduit> component:components) {
                if (component.getConduit().getPos().equals(conduit.getPos()))
                    result = component;
            }

        return result;
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.id = nbt.getUniqueId("ID");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putUniqueId("ID",id);
        return compound;
    }

}
