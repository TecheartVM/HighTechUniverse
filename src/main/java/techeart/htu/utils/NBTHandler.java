package techeart.htu.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import techeart.htu.MainClass;

import java.util.HashSet;
import java.util.Set;

public class NBTHandler extends WorldSavedData
{
    private static final String DATA_IDENTIFIER = MainClass.MODID + "_nbt";
    public final Set<ISaveable> subscribers = new HashSet();

    public NBTHandler() { this(DATA_IDENTIFIER); }
    public NBTHandler(String name) { super(name); }

    public NBTHandler get(World world)
    {
        if(!(world instanceof ServerWorld)) throw new RuntimeException("Attempted to get data from client.");
        DimensionSavedDataManager storage = ServerLifecycleHooks.getCurrentServer().func_241755_D_().getWorld().getSavedData();
        MainClass.LOGGER.info("HANDLER GET");
        return storage.getOrCreate(NBTHandler::new, DATA_IDENTIFIER);
    }

    public void addSubscriber(ISaveable s) { subscribers.add(s); }
    public void removeSubscriber(ISaveable s) { subscribers.remove(s); }

    @Override
    public boolean isDirty()
    {
        boolean flag = false;
        for (ISaveable item : subscribers) flag |= item.isDirty();
        if(flag) System.out.println("There is some changes that is need to be saved.");
        return super.isDirty() || flag;
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        MainClass.LOGGER.info("HANDLER READ");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        MainClass.LOGGER.info("HANDLER WRITE");
        return nbt;
    }
}