package techeart.htu.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidUtils
{
    //TODO FluidStack info string static getter

    /**Handles tank interaction. Returns true if tank state was changed.*/
    public static TankInteractionResult interactWithTank(PlayerEntity player, Hand handIn, ItemStack heldItem, IFluidHandler tankFluidHandler, int tankIndex)
    {
        LazyOptional<IFluidHandlerItem> lo = new ItemStack(heldItem.getItem(),1).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
        if(!lo.isPresent()) return TankInteractionResult.NO_ITEM;
        IFluidHandlerItem itemFluidHandler = lo.orElse(null);

        FluidStack fluidInItem;
        FluidStack fluidInTank = tankFluidHandler.getFluidInTank(tankIndex);

        //if tank is empty, trying to determine fluid in item stack. Else check amount of the required fluid.
        if(fluidInTank.isEmpty())
            fluidInItem = itemFluidHandler.drain(tankFluidHandler.getTankCapacity(tankIndex), FluidAction.SIMULATE);
        else
            fluidInItem = itemFluidHandler.drain(
                    new FluidStack(fluidInTank, tankFluidHandler.getTankCapacity(tankIndex) - fluidInTank.getAmount()),
                    FluidAction.SIMULATE
            );

        //All systems operable. Lets rock!
        if(fluidInItem.isEmpty())
        {
            //if draining with max value of integer isn't empty, this means that the item cannot be partially emptied (like vanilla bucket).
            if(!itemFluidHandler.drain(new FluidStack(fluidInTank, Integer.MAX_VALUE), FluidAction.SIMULATE).isEmpty())
                return TankInteractionResult.FAILED;

            //Ok, this item doesn't have any fluid (fill item + drain tank)

            //check if we can fill item with tank fluid
            int filled = itemFluidHandler.fill(fluidInTank, player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
            //if can't, do nothing
            if(filled <= 0) return TankInteractionResult.FAILED;
            //if can, drain tank and fill item with drained fluid
            SoundEvent sound = tankFluidHandler.drain(new FluidStack(fluidInTank, filled), FluidAction.EXECUTE).getFluid().getAttributes().getFillSound();

            ModUtils.addItemToPlayer(player ,handIn , 1, new ItemStack(itemFluidHandler.getContainer().getItem(), 1));

            player.world.playSound(null, player.getPosition(), sound, SoundCategory.BLOCKS, 1.0F, 1.0F);

            //if tank became empty so it changed state
            if(tankFluidHandler.getFluidInTank(tankIndex).isEmpty()) return TankInteractionResult.UPDATED;
        }
        else
        {
            boolean wasEmpty = fluidInTank.isEmpty();

            //It seems like this item have suitable fluid... Lets fill tank and drain item
            int filled = tankFluidHandler.fill(fluidInItem, IFluidHandler.FluidAction.EXECUTE);
            SoundEvent sound = itemFluidHandler.drain(
                    new FluidStack(fluidInItem, filled),
                    player.isCreative() ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE
            ).getFluid().getAttributes().getEmptySound();

            ModUtils.addItemToPlayer(player,handIn,1, new ItemStack(itemFluidHandler.getContainer().getItem(), 1));

            player.world.playSound(null, player.getPosition(), sound, SoundCategory.BLOCKS, 1.0F, 1.0F);

            //tank got new fluid so it changed state
            if(wasEmpty && !tankFluidHandler.getFluidInTank(tankIndex).isEmpty()) return TankInteractionResult.UPDATED;
        }
        System.out.println("Fluid in tank now is: " + tankFluidHandler.getFluidInTank(tankIndex).getAmount());
        return TankInteractionResult.SUCCESS;
    }

    public enum TankInteractionResult
    {
        NO_ITEM,
        FAILED,
        SUCCESS,
        UPDATED;

        public boolean itemValid() { return this != NO_ITEM; }
        public boolean success() { return this == SUCCESS || this == UPDATED; }
        public boolean updated() { return this == UPDATED; }
    }
}
