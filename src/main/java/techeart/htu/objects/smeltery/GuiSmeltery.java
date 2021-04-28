package techeart.htu.objects.smeltery;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import techeart.htu.MainClass;

@OnlyIn(Dist.CLIENT)
public class GuiSmeltery extends ContainerScreen<ContainerSmeltery>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(MainClass.MODID, "textures/gui/smeltery.png");

    public GuiSmeltery(ContainerSmeltery container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y)
    {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindTexture(TEXTURE);
        GuiSmeltery.blit(matrixStack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, 256, 256);

        //draw fuel bar
        if(container.isBurning())
        {
            int k = this.container.getBurnLeftScaled(13);
            this.blit(matrixStack, this.guiLeft + 53, this.guiTop + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        //draw progress bar
        int l = this.container.getWorkProgressScaled(24);
        this.blit(matrixStack, this.guiLeft + 94, this.guiTop + 35, 190, 0, l, 16);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y)
    {
        this.font.drawString(matrixStack, this.title.getString(), this.xSize / 2f - this.font.getStringWidth(this.title.getString()) / 2f, 6, 4210752);
        this.font.drawText(matrixStack, this.playerInventory.getDisplayName(), 8, this.ySize - 96 + 2, 4210752);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }
}