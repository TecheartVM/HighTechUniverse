package techeart.htu.objects.conduits.grids;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import techeart.htu.objects.conduits.AbstractConduit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GridComponent<Conduit extends AbstractConduit> {
    public Map<Direction,TileEntity> neighbours = new HashMap<>(); //TODO: to make an interface...I guess... because neighbours are optional for different grids
    private final Conduit conduit;
    public int componentData=0;   //TODO: create a class "ComponentData"? Because grid can use different data (Ex. electricity (R,U,I) )

    public GridComponent(Conduit conduit) {
        this.conduit = conduit;
        updateNeighbours();
    }

    public TileEntity getNeighbourByDirection(Direction direction)
    {
        return neighbours.get(direction);
    }

    public Conduit getConduit() {
        return conduit;
    }

    public int getComponentData() {
        return componentData;
    }

    public void updateNeighbour(Direction dir,TileEntity t)
    {
        neighbours.remove(dir);

        if(t == null || t.isRemoved() || (t.getClass().isInstance(conduit) && ((AbstractConduit)t).getGrid() == conduit.getGrid()))
            return;

        this.conduit.getGrid().onComponentNeighbourChanged(dir,t);
        neighbours.put(dir, t);
    }

    public Grid<Conduit> getGrid()
    {
        return (Grid<Conduit>) conduit.getGrid();
    }

    public ArrayList<TileEntity> getHorizontalNeighbours()
    {
        ArrayList<TileEntity> horizontalNeighbours = new ArrayList<>();

        neighbours.forEach((key,value) -> {
            if(!(key == Direction.DOWN || key == Direction.UP))
                horizontalNeighbours.add(neighbours.get(key));
        });
        return horizontalNeighbours;
    }

    private void updateNeighbours()
    {
        if(conduit == null) return;

        for(int i =0; i<6; i++) {
            Direction dir = Direction.byIndex(i);
            TileEntity t = conduit.getWorld().getTileEntity(conduit.getPos().offset(dir));
            updateNeighbour(dir,t);
        }
    }

    public String getNeighboursASString()
    {
        StringBuilder result = new StringBuilder();
        for ( Direction key  : neighbours.keySet())
            result.append("DIR="+key+"\n");

        return result.toString();
    }
}