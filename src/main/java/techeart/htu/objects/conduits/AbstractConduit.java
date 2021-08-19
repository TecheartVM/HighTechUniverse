package techeart.htu.objects.conduits;


import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import techeart.htu.MainClass;
import techeart.htu.objects.conduits.grids.Grid;
import techeart.htu.objects.conduits.grids.GridComponent;
import techeart.htu.objects.conduits.pipe.FluidConduit;
import techeart.htu.utils.interfaces.INeedLoad;
import techeart.htu.utils.world.HTUGridManager;

import java.util.ArrayList;

public abstract class AbstractConduit extends TileEntity implements INeedLoad {
    protected Grid<? extends AbstractConduit> grid;
    protected final Boolean newTE;

    public AbstractConduit(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        newTE=false;
    }
    public AbstractConduit(TileEntityType<?> tileEntityTypeIn,boolean isNew)
    {
        super(tileEntityTypeIn);
        newTE=isNew;
    }

    public <Conduit extends AbstractConduit> void setGrid(Grid<Conduit> grid) {
        if(this.getGrid()!= null) this.getGrid().removeComponent(this);
        this.grid = grid;
        grid.addComponent((Conduit) this);
    }

    public Grid<? extends AbstractConduit> getGrid() {
        return grid;
    }

    @Override
    public void remove() {
        if(!world.isRemote()) {
            if(this.getGrid() !=null) {
                this.getGrid().removeComponent(this);

                try {
                    splitGrid(this.getGrid().getClass());
                } catch (Exception e) {                     //TODO: remove TryCatch
                    e.printStackTrace();
                }

                onConduitRemove();
            }
            else
                MainClass.LOGGER.error("Conduit with pos("+this.getPos().getCoordinatesAsString()+") cant leave GRID because it absent!");
        }
        super.remove();

    }
    protected <Conduit extends AbstractConduit> void splitGrid(Class<? extends Grid> grid) throws InstantiationException, IllegalAccessException {
        ArrayList<Conduit> neighbours = getNeighbours();
        if (neighbours.size() > 1)
            for(Conduit conduit : neighbours)
                conduit.spreadGrid(grid.newInstance());
    }

    public <Conduit extends AbstractConduit,GridObject extends Grid<Conduit>> void spreadGrid(GridObject grid) {
        if (this.getGrid() == grid) return;
        Grid<? extends AbstractConduit> previousGrid = this.getGrid();
        this.setGrid(grid);
        onReplacinGrid(previousGrid,grid,this);
        ArrayList<FluidConduit> neighbours = getNeighbours();
        for (FluidConduit conduit:neighbours) {
            conduit.spreadGrid(grid);
        }
    }

    protected  <Conduit extends AbstractConduit,GridObject extends Grid<Conduit>> void onReplacinGrid(Grid<? extends AbstractConduit> currentGrid, GridObject newGrid, AbstractConduit conduit) {
    }

    protected <Conduit extends AbstractConduit> ArrayList<Conduit> getNeighbours()
    {
        ArrayList<Conduit> tmp = new ArrayList<>();
        for(int i =0; i<4; i++) {
            TileEntity t = world.getTileEntity(pos.offset(Direction.byHorizontalIndex(i)));
            if(t != null && !t.isRemoved() && getClass().isInstance(t))
                tmp.add((Conduit)t);
        }
        return tmp;
    }

    protected void onConduitRemove() {
    }

    public GridComponent<? extends AbstractConduit> getComponent()
    {
        return this.getGrid().getComponent(this);
    }

    public void nullGrid()
    {
        this.grid = null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if( !world.isRemote() && this.newTE)
            newGrid();
    }

    public void newGrid() {
        System.out.println("'newGrid' method is empty! You need to override it");
    }

    @Override
    public void load() {
//        System.out.println("load"); // Попробовать сохранить соседей?! | Спросить когда при запуске сервера происходит коннект  6-сторонних блоков. Мб так привязаться :D IDK
        newGrid();
        System.out.println("LOADED POS="+pos.getCoordinatesAsString());
        onConduitLoad();
    }

    protected void onConduitLoad() {
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        onConduitRead(nbt);
//        MainClass.gridsManager.addLoads(this);
        HTUGridManager.SELF.addLoads(this);
    }

    protected void onConduitRead(CompoundNBT nbt) {
        System.out.println("'onConduitLoad' method is empty! You need to override it");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if(!world.isRemote()) {
            onConduitWrite(compound);
        }
        return compound;
    }

    protected void onConduitWrite(CompoundNBT compound) {
        System.out.println("'onConduitWrite' method is empty! You need to override it");
    }

}