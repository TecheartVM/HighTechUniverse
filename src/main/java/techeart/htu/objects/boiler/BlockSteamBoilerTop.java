package techeart.htu.objects.boiler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import techeart.htu.objects.HTUTileBlock;
import techeart.htu.utils.registration.RegistryHandler;

import javax.annotation.Nullable;

public class BlockSteamBoilerTop extends HTUTileBlock
{
    public BlockSteamBoilerTop()
    {
        super(Block.Properties.create(Material.IRON)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(4.0f, 7.0f)
                .sound(SoundType.METAL)
        );
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return RegistryHandler.STEAM_BOILER.getMachineBlock(1).getMachineTile().create();
    }
}
