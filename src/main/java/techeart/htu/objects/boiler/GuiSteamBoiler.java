package techeart.htu.objects.boiler;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import techeart.htu.MainClass;
import techeart.htu.objects.fluids.FluidSteam;

import java.awt.*;

public class GuiSteamBoiler extends ContainerScreen<ContainerSteamBoiler>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(MainClass.MODID + ":textures/gui/steam_boiler.png");

    public GuiSteamBoiler(ContainerSteamBoiler container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y)
    {
        //setup background base texture
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindTexture(TEXTURE);
        GuiSteamBoiler.blit(matrixStack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, 256, 256);

        //draw fuel bar
        if (container.isBurning())
        {
            int k = this.getBurnLeftScaled(13);
            this.blit(matrixStack, this.guiLeft + 81, this.guiTop + 41 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        //temperature bar
        int w = getFieldValueScaled(2, 64);
        this.blit(matrixStack, this.guiLeft + 46, this.guiTop + 9 + 63 - w, 176, 14 + 63 - w, 4, w + 1);

        //pressure bar
        w = getFieldValueScaled(3, 64);
        this.blit(matrixStack, this.guiLeft + 126, this.guiTop + 9 + 63 - w, 180, 14, 4, 1);

        //get, color and bind water texture
        TextureAtlasSprite sprite = minecraft.getModelManager().getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE).getSprite(Fluids.WATER.getAttributes().getStillTexture());
        Color c = new Color(Fluids.WATER.getAttributes().getColor(), true);
        float[] f = new float[4];
        c.getComponents(f);
        RenderSystem.color4f(f[0], f[1], f[2], f[3]);
        this.minecraft.getTextureManager().bindTexture(sprite.getAtlasTexture().getTextureLocation());

        //draw water bar
        w = getFieldValueScaled(0, 64);
        for (int i = 0; i < w; i += 16)
        {
            GuiSteamBoiler.blit(matrixStack, this.guiLeft + 26, this.guiTop + 9 + 64 - 16 - i, 0, 16, 16, sprite);
        }
        //draw empty space above water bar
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindTexture(TEXTURE);
        this.blit(matrixStack, this.guiLeft + 26, this.guiTop + 9, 26, 9, 16, 64 - w);

        //get and bind steam texture
        sprite = minecraft.getModelManager().getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE).getSprite(FluidSteam.STEAM.getAttributesForGui().getStillTexture());
        this.minecraft.getTextureManager().bindTexture(sprite.getAtlasTexture().getTextureLocation());

        //setup alpha blend for steam texture
        w = getFieldValueScaled(1, 255);
        float alpha = (float) w/100;
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1, 1, 1, alpha);

        //draw steam bar
        for (int i = 0; i < 64; i += 16)
        {
            GuiSteamBoiler.blit(matrixStack, this.guiLeft + 134, this.guiTop + 9 + 64 - 16 - i, 0, 16, 16, sprite);
        }

        RenderSystem.popMatrix();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y)
    {
        this.font.drawString(matrixStack, this.title.getString(), this.xSize / 2f - this.font.getStringWidth(this.title.getString()) / 2f, 6, 4210752);
        this.font.drawText(matrixStack, this.playerInventory.getDisplayName(), 8, this.ySize - 96 + 4, 4210752);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    private int getBurnLeftScaled(int pixels) { return this.container.getBurnLeftScaled(pixels); }

    private int getFieldValueScaled(int field, int pixels) { return container.getFieldValueScaled(field, pixels); }
}
