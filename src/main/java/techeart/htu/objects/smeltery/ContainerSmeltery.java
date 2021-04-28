package techeart.htu.objects.smeltery;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import techeart.htu.utils.HTUIntReferenceHolder;
import techeart.htu.utils.RegistryHandler;

import java.util.Objects;

//TODO: Fix chunkLoading!!
public class ContainerSmeltery extends Container
{
    private final TileEntitySmeltery tileEntity;

    public ContainerSmeltery(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data)
    {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    public ContainerSmeltery(final int windowId, final PlayerInventory playerInvenoty, final TileEntitySmeltery tileEntity)
    {
        super(RegistryHandler.SMELTERY.getContainer(), windowId);

        this.tileEntity = tileEntity;

        //smeltery inventory
        this.addSlot(new Slot(tileEntity, 0, 26, 17));
        this.addSlot(new Slot(tileEntity, 1, 44, 17));
        this.addSlot(new Slot(tileEntity, 2, 62, 17));
        this.addSlot(new Slot(tileEntity, 3, 80, 17));
        this.addSlot(new SlotSmelteryFuel(tileEntity, 4, 53, 53));
        this.addSlot(new SlotSmelteryOutput(playerInvenoty.player, tileEntity, 5, 131, 35));

        //player inventory
        //main
        for(int y = 0; y <3; y++)
            for(int x = 0; x < 9; x++)
                this.addSlot(new Slot(playerInvenoty, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
        //hotbar
        for(int x = 0; x < 9; x++)
            this.addSlot(new Slot(playerInvenoty, x, 8 + x * 18, 142));

        //int reference holders
        for(int i = 0; i < tileEntity.getFieldCount(); i++)
        {
            int fi = i;
            trackInt(new HTUIntReferenceHolder(() -> this.tileEntity.getField(fi), value -> this.tileEntity.setField(fi, value)));
        }

        addListener(new IContainerListener() {
            @Override
            public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList)
            {
                //ContainerSmeltery.this.tileEntity.updateCurrentRecipe();
            }

            @Override
            public void sendSlotContents(Container containerToSend, int slotIndex, ItemStack stack)
            {
                if(slotIndex < 4) ContainerSmeltery.this.tileEntity.updateCurrentRecipe();
            }

            @Override
            public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) { }
        });
    }

    private static TileEntitySmeltery getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data)
    {
        Objects.requireNonNull(playerInventory, "'playerInventory' can not be null");
        Objects.requireNonNull(data, "'data' can not be null");

        final TileEntity tileEntity = playerInventory.player.world.getTileEntity(data.readBlockPos());
        if(tileEntity instanceof TileEntitySmeltery)
            return (TileEntitySmeltery)tileEntity;

        throw new IllegalStateException("Illegal tile entity: " + tileEntity);
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        for(int i = 0; i < tileEntity.getFieldCount(); i++)
            listener.sendWindowProperty(this, i, tileEntity.getField(i));
    }

    @OnlyIn(Dist.CLIENT)
    public int getWorkProgressScaled(int pixels)
    {
        int i = this.tileEntity.getField(2);
        int j = this.tileEntity.getField(3);
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

    @OnlyIn(Dist.CLIENT)
    public int getBurnLeftScaled(int pixels)
    {
        int i = this.tileEntity.getField(1);
        if (i == 0) { i = 200; }
        return this.tileEntity.getField(0) * pixels / i;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 5)
            {
                if (!this.mergeItemStack(itemstack1, 6, 42, true))
                    return ItemStack.EMPTY;

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index != 0 && index != 1 && index != 2 && index != 3 && index != 4)
            {
                if (TileEntitySmeltery.isItemFuel(itemstack1))
                {
                    if (!this.mergeItemStack(itemstack1, 4, 5, false))
                        return ItemStack.EMPTY;
                }
                else
                {
                    if (this.mergeItemStack(itemstack1, 0, 4, false))
                        return ItemStack.EMPTY;
                    else if (index < 33)
                    {
                        if (!this.mergeItemStack(itemstack1, 33, 42, false))
                            return ItemStack.EMPTY;
                    }
                    else if (index >= 33 && index < 42 && !this.mergeItemStack(itemstack1, 6, 33, false))
                        return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 6, 42, false))
                return ItemStack.EMPTY;

            if (itemstack1.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();

            if (itemstack1.getCount() == itemstack.getCount())
                return ItemStack.EMPTY;

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return isWithinUsableDistance(IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos()), playerIn, RegistryHandler.SMELTERY.getMainBlock().getBlock());
    }

    public boolean isBurning() { return tileEntity.isBurning(); }
}
