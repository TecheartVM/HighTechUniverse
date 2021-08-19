package techeart.htu.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class WorldUtils
{
    /**Returns Tile entity if is present and its block is loaded. Else returns null.*/
    @Nullable
    public static TileEntity getTileEntity(IBlockReader world, BlockPos pos)
    {
        if(!isBlockLoaded(world, pos)) return null;
        return world.getTileEntity(pos);
    }

    public static boolean isBlockLoaded(IBlockReader world, BlockPos pos)
    {
        if(world == null || !World.isValid(pos)) return false;
        if(world instanceof IWorldReader) return ((IWorldReader)world).isBlockLoaded(pos);
        return true;
    }
}
