package techeart.htu.utils.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import techeart.htu.objects.conduits.AbstractConduit;
import techeart.htu.objects.conduits.grids.FluidGrid;
import techeart.htu.objects.conduits.grids.Grid;
import techeart.htu.utils.interfaces.INeedLoad;
import techeart.htu.utils.interfaces.ITickable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HTUGridManager {

    public static final HTUGridManager SELF = new HTUGridManager();
    ArrayList<Grid<? extends AbstractConduit>> grids = new ArrayList<>();
    Set<INeedLoad> needLoads = new HashSet<>();
    private HTUSavedData data;

    private boolean isLoaded = false;
    private boolean isDirty = false;

    public void markDirty()
    {
        isDirty = true;
    }

    public boolean isDirty()
    {
        return isDirty;
    }

    public void registerGrid(Grid<? extends AbstractConduit> o)
    {
        grids.add(o);
        markDirty();
    }

    public void unregisterGrid(Grid<? extends AbstractConduit> o)
    {
        grids.remove(o);
        markDirty();
    }

    public void reset()
    {
        grids.clear();
        data = null;
        isLoaded = false;
    }

    public void addLoads(INeedLoad needLoad)
    {
        if(!isLoaded)
            needLoads.add(needLoad);
        else
            needLoad.load();
    }

    public void worldTick(){
        grids.forEach(ITickable::HTUTick);
    }

    public void serverTick() {
        if(isLoaded) return;
        isLoaded = LoC();
        System.out.println("ModManager is Loaded!");
    }

    private boolean LoC() { //Try to call on server start
        if(data!= null)
        {
            DimensionSavedDataManager storage = ServerLifecycleHooks.getCurrentServer().func_241755_D_().getSavedData();
            data = storage.getOrCreate(HTUSavedData::new,HTUSavedData.DATA_IDENTIFIER);
            data.setManager(this);
        }
        loadAll();
        return true;
    }

    private void loadAll()
    {
        needLoads.forEach(INeedLoad::load);
        needLoads.clear();
    }

    public void read(CompoundNBT nbt) {
        if(!nbt.contains("htu_grids_size"))
            return;
        grids = new ArrayList<>();
        int size = nbt.getInt("htu_grids_size");
        for(int i=size;i>=0;i--)
        {
            CompoundNBT gridNBT = nbt.getCompound(String.valueOf(i));
            Grid<? extends AbstractConduit> grid =null;

            if(gridNBT.contains("FluidName")) {
                grid = new FluidGrid();                     /*TODO: ~add new savable Grids~ */
            }



            if(grid!=null) {
                grid.read(gridNBT);
                grids.add(grid);
            }
        }
    }

    public CompoundNBT write(CompoundNBT nbt){
        isDirty = false;
        nbt.putInt("htu_grids_size",grids.size());
        for(int i=0; i< grids.size(); i++)
            nbt.put(String.valueOf(i),grids.get(i).write(new CompoundNBT()));
        return nbt;
    }
}
