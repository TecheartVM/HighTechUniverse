package techeart.htu.objects.smeltery;

import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class SlotSmelteryOutput extends Slot
{
    private final PlayerEntity player;
    private int removeCount;

    public SlotSmelteryOutput(PlayerEntity player, IInventory inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
        this.player = player;
    }

    @Override
    public boolean isItemValid(ItemStack item) {
        return false;
    }

    @Override
    public ItemStack onTake(PlayerEntity player, ItemStack stack)
    {
        this.onCrafting(stack);
        super.onTake(player, stack);
        return stack;
    }

    @Override
    public ItemStack decrStackSize(int amount)
    {
        if(this.getHasStack()) this.removeCount += Math.min(amount, this.getStack().getCount());
        return super.decrStackSize(amount);
    }

    @Override
    protected void onCrafting(ItemStack stack, int amount)
    {
        this.removeCount += amount;
        this.onCrafting(stack);
    }

    @Override
    protected void onCrafting(ItemStack stack)
    {
        stack.onCrafting(this.player.world, this.player, this.removeCount);

        if (!this.player.world.isRemote)
        {
            int itemAmount = this.removeCount;
            float experience = 0;   //TODO AlloyRecipes.getInstance().getAlloyExperience(stack);

            if (experience == 0.0F)
                itemAmount = 0;
            else if (experience < 1.0F)
            {
                int totalXP = MathHelper.floor((float)itemAmount * experience);

                if (totalXP < MathHelper.ceil((float)itemAmount * experience) && Math.random() < (double)((float)itemAmount * experience - (float)totalXP))
                    ++totalXP;

                itemAmount = totalXP;
            }

            while (itemAmount > 0)
            {
                int k =  ExperienceOrbEntity.getXPSplit(itemAmount);
                itemAmount -= k;
                this.player.world.addEntity(new ExperienceOrbEntity(this.player.world, this.player.getPosX(), this.player.getPosY() + 0.5D, this.player.getPosZ() + 0.5D, k));
            }
        }

        this.removeCount = 0;
    }
}
