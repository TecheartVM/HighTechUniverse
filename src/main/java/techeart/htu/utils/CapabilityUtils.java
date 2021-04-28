package techeart.htu.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class CapabilityUtils
{
    public static <T> LazyOptional<T> getCapability(World world, BlockPos pos, Capability<T> cap, @Nullable Direction side)
    {
        if(world == null || world.isRemote || pos == null || cap == null) return LazyOptional.empty();
        TileEntity tile = world.getTileEntity(pos);
        if(tile == null || tile.isRemoved()) return LazyOptional.empty();
        return tile.getCapability(cap, side);
    }

    public static <T> LazyOptional<T> getCapability(ICapabilityProvider provider, Capability<T> cap, @Nullable Direction side)
    {
        if(provider == null || cap == null) return LazyOptional.empty();
        return provider.getCapability(cap, side);
    }
}
