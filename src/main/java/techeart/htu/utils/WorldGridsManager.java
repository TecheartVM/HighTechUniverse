package techeart.htu.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import techeart.htu.MainClass;
import techeart.htu.objects.pipe.HorizontalPipeGrid;
import techeart.htu.objects.pipe.IPipeGrid;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WorldGridsManager
{
    private static final String DATA_IDENTIFIER = MainClass.MODID + "_grids_nbt";

    private final Set<IPipeGrid> worldGrids = new HashSet<>();
    private boolean isDirty = false;

    private GridsDataHandler dataHandler;
    private boolean isLoaded = false;

    /*grids management*/
    public void registerGrid(IPipeGrid grid)
    {
        worldGrids.add(grid);
        markDirty();
    }

    public void unregisterGrid(IPipeGrid grid)
    {
        worldGrids.remove(grid);
        markDirty();
    }

    public void markDirty() { isDirty = true; }
    private void setDirty(boolean value) { isDirty = value; }

    public boolean isLoaded() { return isLoaded; }

    public int getGridsCount() { return worldGrids.size(); }

    public IPipeGrid getGrid(UUID id)
    {
        for (IPipeGrid grid : worldGrids)
        {
//            System.out.println("Required id: " + id);
//            System.out.println("Current id: " + grid.getId());
//            System.out.println("Equation result: " + (id.equals(grid.getId())));
            if (grid.getId().equals(id)) return grid;
        }
        return null;
    }

    /*data handler control*/
    public void onServerTick()
    {
        worldGrids.forEach(grid -> grid.tick());
    }

    public void onServerWorldTick(World world)
    {
        if(!isLoaded) loadOrCreate();
    }

    private void loadOrCreate()
    {
        if(dataHandler != null) return;
        //TODO per dimension data management debug
        // Now it's working, I think.
        MainClass.LOGGER.debug("World Grids Manager is Loading!");
        DimensionSavedDataManager storage = ServerLifecycleHooks.getCurrentServer().func_241755_D_().getSavedData();
        dataHandler = storage.getOrCreate(GridsDataHandler::new, DATA_IDENTIFIER);
        dataHandler.setManager(this);
        isLoaded = true;
    }

    public void reset()
    {
        worldGrids.clear();
        dataHandler = null;
        isLoaded = false;
    }

    public static class GridsDataHandler extends WorldSavedData
    {
        private WorldGridsManager manager;

        public GridsDataHandler()
        {
            super(DATA_IDENTIFIER);
            manager = MainClass.gridsManager;
        }

        public void setManager(WorldGridsManager manager) { this.manager = manager; }

        @Override
        public boolean isDirty()
        {
            if(manager.isDirty || super.isDirty())
                return true;
            for (IPipeGrid grid : manager.worldGrids)
                if (grid.isDirty())
                    return true;
            return false;
        }



        @Override
        public void read(CompoundNBT nbt)
        {
            manager.worldGrids.clear();
            int count = nbt.getInt("htuGridsCount");
            for (int i = 0; i < count; i++)
            {
                CompoundNBT gridNBT = nbt.getCompound("htuGrid" + i);
                if(gridNBT.hasUniqueId("id"))
                {
                    HorizontalPipeGrid grid = new HorizontalPipeGrid(gridNBT.getUniqueId("id"));
                    grid.readFromNBT(gridNBT);
                    manager.registerGrid(grid);
                }
            }
        }

        @Override
        public CompoundNBT write(CompoundNBT nbt)
        {
            nbt.putInt("htuGridsCount", manager.worldGrids.size());
            int counter = 0;
            for (IPipeGrid grid : manager.worldGrids)
            {
                nbt.put("htuGrid" + counter, grid.writeToNBT(new CompoundNBT()));
                counter++;
            }
            manager.setDirty(false);
            return nbt;
        }
    }
}
