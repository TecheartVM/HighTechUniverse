package techeart.htu.objects.sensors.temperature;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import techeart.htu.MainClass;

public class RenderSensorTemperature extends TileEntityRenderer<TileEntitySensorTemperature>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(MainClass.MODID, "blocks/sensor_temperature");

    public RenderSensorTemperature(TileEntityRendererDispatcher rendererDispatcherIn) { super(rendererDispatcherIn); }

    @Override
    public void render(TileEntitySensorTemperature tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        renderIndicator(tileEntityIn.getBlockState(), tileEntityIn.getLerpedIndicatorHeight(), matrixStackIn, bufferIn);
    }

    protected void renderIndicator(BlockState state, float percent, MatrixStack matrix, IRenderTypeBuffer buffer)
    {
        matrix.push();

        float angle = 0;
        Direction facing = state.get(BlockSensorTemperature.FACING);
        if(facing == Direction.SOUTH) angle = 180;
        else if(facing == Direction.EAST) angle = -90;
        else if(facing == Direction.WEST) angle = 90;

        matrix.translate(.5, .5, .5);
        matrix.rotate(Vector3f.YP.rotationDegrees(angle));
        matrix.translate(0, -.01, .445);

        matrix.scale(0.25f,0.4f,0.25f);

        Matrix4f matrixLast = matrix.getLast().getMatrix();
        Matrix3f normalMatrix = matrix.getLast().getNormal();
        IVertexBuilder builder = buffer.getBuffer(RenderType.getTranslucent());
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE);
        for (int i = 0; i < 4; i++)
        {
            renderIndicatorSide(sprite, matrixLast, normalMatrix, builder, percent);
            matrix.rotate(Vector3f.YP.rotationDegrees(90f));
        }

        if(percent < 0.999f)
            renderIndicatorTop(sprite, matrixLast, normalMatrix, builder, percent);
        matrix.pop();
    }

    private void renderIndicatorSide(TextureAtlasSprite sprite, Matrix4f matrix, Matrix3f normalMatrix, IVertexBuilder builder, float percent)
    {
        float width = 0.125f; //2 pixels of 16

        float minU = sprite.getInterpolatedU(15.0d);
        float maxU = sprite.getInterpolatedU(15.5d);
        float minV = sprite.getInterpolatedV(0.5d);
        float maxV = sprite.getInterpolatedV(6.25d * percent);

        builder.pos(matrix, -width / 2, percent - 0.5f, -width / 2 + 0.001f)
                .color(1.0f,1.0f,1.0f,1.0f).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.pos(matrix, width / 2, percent - 0.5f, -width / 2 + 0.001f)
                .color(1.0f,1.0f,1.0f,1.0f).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.pos(matrix, width / 2, -0.5f, -width / 2 + 0.001f)
                .color(1.0f,1.0f,1.0f,1.0f).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.pos(matrix, -width / 2, -0.5f, -width / 2 + 0.001f)
                .color(1.0f,1.0f,1.0f,1.0f).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();
    }

    protected void renderIndicatorTop(TextureAtlasSprite sprite, Matrix4f matrix, Matrix3f normalMatrix, IVertexBuilder builder, float percent)
    {
        float width = 0.125f; //2 pixels of 16

        float minU = sprite.getInterpolatedU(14.5d);
        float maxU = sprite.getInterpolatedU(16d);
        float minV = sprite.getInterpolatedV(0.5d);
        float maxV = sprite.getInterpolatedV(6.25d);

        builder.pos(matrix, -width / 2, percent - 0.5f, -width / 2)
                .color(1.0f,1.0f,1.0f,1.0f).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();

        builder.pos(matrix, -width / 2, percent - 0.5f, width / 2)
                .color(1.0f,1.0f,1.0f,1.0f).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();

        builder.pos(matrix, width / 2, percent - 0.5f, width / 2)
                .color(1.0f,1.0f,1.0f,1.0f).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();

        builder.pos(matrix, width / 2, percent - 0.5f, -width / 2)
                .color(1.0f,1.0f,1.0f,1.0f).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();
    }
}
