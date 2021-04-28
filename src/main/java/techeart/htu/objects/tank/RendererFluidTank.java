package techeart.htu.objects.tank;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.fluids.FluidStack;

public class RendererFluidTank extends TileEntityRenderer<TileEntityFluidTank>
{
    public RendererFluidTank(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(TileEntityFluidTank tileEntity, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        FluidStack fluid = tileEntity.getFluid();
        if(!fluid.isEmpty())
        {
            int capacity = tileEntity.getCapacity();
            renderFluid(tileEntity.getWorld(), tileEntity.getPos(), fluid.getFluid(), tileEntity.getLerpedFluidAmount() / (float) capacity, matrixStackIn, bufferIn);
        }
    }

    protected void renderFluid(IBlockDisplayReader world, BlockPos pos, Fluid fluid, float percent, MatrixStack matrix, IRenderTypeBuffer buffer)
    {
        matrix.push();
        matrix.translate(.5, .5, .5);
        Matrix4f matrixLast = matrix.getLast().getMatrix();
        Matrix3f normalMatrix = matrix.getLast().getNormal();
        int color = fluid.getAttributes().getColor(world, pos);
        TextureAtlasSprite fluidSprite = getFluidSprite(fluid);
        IVertexBuilder builder = buffer.getBuffer(RenderType.getTranslucent());
        for (int i = 0; i < 4; i++)
        {
            renderFluidSide(fluidSprite, matrixLast, normalMatrix, builder, color, percent);
            matrix.rotate(Vector3f.YP.rotationDegrees(90f));
        }

        if(percent < 0.999f) renderFluidTop(fluidSprite, matrixLast, normalMatrix, builder, color, percent);
        matrix.pop();
    }

    private void renderFluidSide(TextureAtlasSprite fluidSprite, Matrix4f matrix, Matrix3f normalMatrix, IVertexBuilder builder, int color, float percent)
    {
        float b = ((color)&0xFF) / 255f;
        float g = ((color>>8)&0xFF) / 255f;
        float r = ((color>>16)&0xFF) / 255f;
        float a = ((color>>24)&0xFF) / 255f;

        float width = 0.75f; //12 pixels of 16

        float minU = fluidSprite.getInterpolatedU(3);
        float maxU = fluidSprite.getInterpolatedU(13);
        float minV = fluidSprite.getInterpolatedV(1);
        float maxV = fluidSprite.getInterpolatedV(15 * percent);

        builder.pos(matrix, -width / 2, percent - 0.5f, -width / 2 + 0.001f)
                .color(r, g, b, a).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.pos(matrix, width / 2, percent - 0.5f, -width / 2 + 0.001f)
                .color(r, g, b, a).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.pos(matrix, width / 2, -0.5f, -width / 2 + 0.001f)
                .color(r, g, b, a).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.pos(matrix, -width / 2, -0.5f, -width / 2 + 0.001f)
                .color(r, g, b, a).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();
    }

    protected void renderFluidTop(TextureAtlasSprite fluidSprite, Matrix4f matrix, Matrix3f normalMatrix, IVertexBuilder builder, int color, float percent)
    {
        float b = ((color)&0xFF) / 255f;
        float g = ((color>>8)&0xFF) / 255f;
        float r = ((color>>16)&0xFF) / 255f;
        float a = ((color>>24)&0xFF) / 255f;

        float width = 0.75f; //12 pixels of 16

        float minU = fluidSprite.getInterpolatedU(3);
        float maxU = fluidSprite.getInterpolatedU(13);
        float minV = fluidSprite.getInterpolatedV(1);
        float maxV = fluidSprite.getInterpolatedV(13);

        builder.pos(matrix, -width / 2, percent - 0.5f, -width / 2)
                .color(r, g, b, a).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();

        builder.pos(matrix, -width / 2, percent - 0.5f, width / 2)
                .color(r, g, b, a).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();

        builder.pos(matrix, width / 2, percent - 0.5f, width / 2)
                .color(r, g, b, a).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();

        builder.pos(matrix, width / 2, percent - 0.5f, -width / 2)
                .color(r, g, b, a).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();
    }

    protected TextureAtlasSprite getFluidSprite(Fluid fluid)
    {
        return Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluid.getAttributes().getStillTexture());
    }
}
