package techeart.htu.utils;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;
import techeart.htu.MainClass;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.UUID;

public class ModUtils
{
    public static void playerChatMessage(String info, PlayerEntity playerEntity)
    {
        playerEntity.sendMessage(ITextComponent.getTextComponentOrEmpty(info), UUID.randomUUID());
    }

    public static void playerInfoMessage(String info, PlayerEntity playerEntity)
    {
        playerEntity.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(info), true);
    }

    public static void addItemToPlayer(PlayerEntity player, Hand handIn, int shrinkAmount ,ItemStack newItem)
    {
        ItemStack oldItem = player.getHeldItem(handIn);
        if(oldItem.getCount() !=1) {
            oldItem.shrink(shrinkAmount);
            if (!player.inventory.addItemStackToInventory(newItem))
                player.dropItem(newItem, false, true);
        }
        else
            player.setHeldItem(handIn,newItem);
    }

    public static void addItemToPlayer(PlayerEntity player, ItemStack oldItem, int shrinkAmount ,ItemStack newItem)
    {
        oldItem.shrink(shrinkAmount);
        if(!player.inventory.add(player.inventory.getSlotFor(oldItem),newItem)) //TODO: Check this thing
            if(!player.inventory.addItemStackToInventory(newItem))
                player.dropItem(newItem, false, true);
    }

    public static void unlockAdvancement(PlayerEntity player, String name)
    {
        if(player instanceof ServerPlayerEntity)
        {
            PlayerAdvancements advancements = ((ServerPlayerEntity)player).getAdvancements();
            AdvancementManager manager = ((ServerWorld)player.getEntityWorld()).getServer().getAdvancementManager();
            Advancement advancement = manager.getAdvancement(new ResourceLocation(MainClass.MODID, name));
            if(advancement!=null)
                advancements.grantCriterion(advancement, "code_trigger");
        }
    }
    //TODO: make this thing possible to get gasName
    public static String getFluidName(CompoundNBT data)
    {
        return FluidStack.loadFluidStackFromNBT(data).getTranslationKey();
    }

    public static int getFluidTextureColor(Fluid fluid, int x, int y)
    {
        int result = 0;
        try
        {
            ResourceLocation fluidRes = fluid.getAttributes().getStillTexture();
            InputStream is = Minecraft.getInstance().getResourceManager().getResource(
                    new ResourceLocation(fluidRes.getNamespace(), "textures/" + fluidRes.getPath() + ".png")
            ).getInputStream();
            BufferedImage image = ImageIO.read(is);

            result = image.getRGB(x, y);
            int fluidColor = fluid.getAttributes().getColor();
            if(fluidColor != -1)
            {
                int b = ((result&0xFF) * (fluidColor&0xFF)) / 255;
                int g = (((result>>8)&0xFF) * ((fluidColor>>8)&0xFF)) / 255;
                int r = (((result>>16)&0xFF) * ((fluidColor>>16)&0xFF)) / 255;
                result = r;
                result = (result << 8) + g;
                result = (result << 8) + b;
            }
        }
        catch(Exception e){
            //e.printStackTrace();
        }

        return result;
    }
    public static ResourceLocation getResourceByKey(String path) {
        return new ResourceLocation(MainClass.MODID, path);
    }
}
