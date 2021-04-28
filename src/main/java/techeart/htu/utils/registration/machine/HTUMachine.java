package techeart.htu.utils.registration.machine;

import net.minecraft.block.Block;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.htu.utils.RegistryHandler;
import techeart.htu.utils.registration.DoubleRegisteredObject;
import techeart.htu.utils.registration.HTUBlock;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Supplier;

public class HTUMachine<TILE extends TileEntity,CONTAINER extends Container>
{

@Nonnull
    private final MachineBlock<TILE>[] machineBlocks;
    private final RegistryObject<ContainerType<CONTAINER>> container;
    private final ScreenManager.IScreenFactory <?,?> gui;

    public <M extends Container, U extends Screen & IHasContainer<M>>  HTUMachine(@Nonnull MachineBlock<TILE>[] machineBlocks, RegistryObject<ContainerType<CONTAINER>> container, ScreenManager.IScreenFactory <M,U> gui) {
        this.machineBlocks = machineBlocks;
        this.container = container;
        this.gui = gui;
    }
    @Nonnull
    public MachineBlock<TILE> getMachineBlock(int i) {
        if(i > machineBlocks.length)
            throw new ArrayIndexOutOfBoundsException("This machine dont have a "+i+" block!");
        return machineBlocks[i];
    }

    @Nonnull
    public MachineBlock<TILE> getMainBlock() {
        return machineBlocks[0];
    }
    public ContainerType<CONTAINER> getContainer() {
        if(this.container == null)
            throw new NullPointerException("Container is NULL");
        return container.get();
    }

    public <M extends Container, U extends Screen & IHasContainer<M>> ScreenManager.IScreenFactory <M,U> getGui() {
        if(this.gui == null)
            throw new NullPointerException("GUI is NULL");
        return (ScreenManager.IScreenFactory<M, U>) gui;
    }

    public static class Builder
    {
        @Nonnull
        String name;
        private ArrayList<HTUBlock.Builder> machineBlocks = new ArrayList<>();
        private ArrayList<Supplier<? extends TileEntity>> machineTiles = new ArrayList<>();
        private IContainerFactory<? extends Container> containerfac;
        private ScreenManager.IScreenFactory<?, ?> gui;

        public Builder(@Nonnull String name) {
            this.name = name;
        }

        public HTUMachine.Builder addBlock(HTUBlock.Builder block,Supplier<? extends TileEntity> tile ) {
            this.machineBlocks.add(block);
            this.machineTiles.add(tile);
            return this;
        }
        public <T extends Container> HTUMachine.Builder needContainer(net.minecraftforge.fml.network.IContainerFactory<T> factory) {
            this.containerfac = factory;
            return this;
        }
        public <M extends Container, U extends Screen & IHasContainer<M>> HTUMachine.Builder needGUI(ScreenManager.IScreenFactory <M,U>   gui) {
            this.gui = gui;
            return this;
        }

        public <TILE extends TileEntity,CONTAINER extends Container> HTUMachine<TILE, CONTAINER> build() {
            IContainerFactory<CONTAINER> containerfactory = (IContainerFactory<CONTAINER>) containerfac;

            MachineBlock<TILE>[] machineBlocks = new MachineBlock[this.machineBlocks.size()];
//            RegistryObject<TileEntityType<?>> TEST =
            //RegistryHandler.MDR.register(name,this.machineTiles.get(i), ForgeRegistries.TILE_ENTITIES)
//              public static final RegistryObject<ContainerType<?>> STEAM_BOILER_CONTAINER = MDR.register("steam_boiler", () -> IForgeContainerType.create(ContainerSteamBoiler::new),ForgeRegistries.CONTAINERS);
            for (int i=0; i<this.machineBlocks.size() ; i++) {
                if(i> 0)
                    name = name+"_"+i;
                DoubleRegisteredObject<Block, Item> block = RegistryHandler.MDR.register("block_" + name, this.machineBlocks.get(i));

                RegistryObject<TileEntityType<?>> tile = null;
                if(this.machineTiles.get(i) != null) {
                    int finalI = i;
                    tile = RegistryHandler.MDR.register(name, () -> TileEntityType.Builder.create(this.machineTiles.get(finalI), block.getPrimary()).build(null), ForgeRegistries.TILE_ENTITIES);
                }
                machineBlocks[i] = new MachineBlock(block, tile);
            }
            RegistryObject<ContainerType<CONTAINER>> container = RegistryHandler.MDR.register(name, () -> IForgeContainerType.create(containerfactory),ForgeRegistries.CONTAINERS);

        return new HTUMachine<>(machineBlocks, container, gui);
        }
    }



}
