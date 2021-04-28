package techeart.htu.objects.boiler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import techeart.htu.objects.inventory.slots.HTUFurnaceFuelSlot;
import techeart.htu.utils.HTUIntReferenceHolder;
import techeart.htu.utils.RegistryHandler;

import java.util.Objects;

public class ContainerSteamBoiler extends Container
{
    private final TileEntitySteamBoiler tileEntity;

    public ContainerSteamBoiler(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data)
    {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    public ContainerSteamBoiler(final int windowId, final PlayerInventory playerInvenoty, final TileEntitySteamBoiler tileEntity)
    {
        super(RegistryHandler.STEAM_BOILER.getContainer(), windowId);

        this.tileEntity = tileEntity;

        this.addSlot(new HTUFurnaceFuelSlot(tileEntity, 0, 80, 57));

        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(playerInvenoty, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(playerInvenoty, x, 8 + x * 18, 142));
        }

        for(int i = 0; i < tileEntity.getFieldCount(); i++)
        {
            int fi = i;
            trackInt(new HTUIntReferenceHolder(() -> this.tileEntity.getField(fi), value -> this.tileEntity.setField(fi, value)));
        }
    }

    private static TileEntitySteamBoiler getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data)
    {
        Objects.requireNonNull(playerInventory, "'playerInventory' can not be null");
        Objects.requireNonNull(data, "'data' can not be null");

        final TileEntity tileEntity = playerInventory.player.world.getTileEntity(data.readBlockPos());
        if(tileEntity instanceof TileEntitySteamBoiler)
        {
            return (TileEntitySteamBoiler)tileEntity;
        }
        throw new IllegalStateException("Illegal tile entity: " + tileEntity);
    }

    @OnlyIn(Dist.CLIENT)
    public int getBurnLeftScaled(int pixels)
    {
        int i = this.tileEntity.getField(1);

        if (i == 0) { i = 200; }

        return this.tileEntity.getField(0) * pixels / i;
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        for(int i = 0; i < tileEntity.getFieldCount(); i++)
            listener.sendWindowProperty(this, i, tileEntity.getField(i));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void updateProgressBar(int id, int data)
    {
        this.tileEntity.setField(id, data);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return isWithinUsableDistance(IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos()), playerIn, RegistryHandler.STEAM_BOILER.getMainBlock().getBlock());
    }

    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index != 0)
            {
                if (ForgeHooks.getBurnTime(itemstack1) > 0)
                {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else
                {
                    if (index > 0 && index < 28)
                    {
                        if (!this.mergeItemStack(itemstack1, 28, 37, false))
                        {
                            return ItemStack.EMPTY;
                        }
                    }
                    else if (index >= 28 && index < 37 && !this.mergeItemStack(itemstack1, 1, 28, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }
            else if (!this.mergeItemStack(itemstack1, 1, 37, false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    public boolean isBurning() { return tileEntity.isBurning(); }

    @OnlyIn(Dist.CLIENT)
    public int getFieldValueScaled(int field, int pixels)
    {
        int i = 0;
        int f = 0;

        switch (field)
        {
            case 0: i = TileEntitySteamBoiler.internalVolumeWater;
                f = 2;
                break;
            case 1: i = TileEntitySteamBoiler.internalVolumeSteam;
                f = 3;
                break;
            case 2: i = TileEntitySteamBoiler.maxTemperature;
                f = 4;
                break;
            case 3: i = TileEntitySteamBoiler.maxPressure;
                f = 5;
                break;
        }
        if(f <= 0) return 0;
        f = this.tileEntity.getField(f);
        return f * pixels / i;
    }
}
