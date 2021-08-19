package techeart.htu.objects.sensors.level;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import techeart.htu.MainClass;

public class RenderSensorFluidLevel extends TileEntityRenderer<TileEntitySensorFluidLevel>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(MainClass.MODID, "textures/blocks/block_sensor_fluid_level_dial.png");

    private static final String TEXTURE_PATH = "textures/blocks/block_sensor_fluid_level_dial";
    private final ModelSensorFluidLevelDial model = new ModelSensorFluidLevelDial();

    public RenderSensorFluidLevel(TileEntityRendererDispatcher rendererDispatcherIn) { super(rendererDispatcherIn); }

    @Override
    public void render(TileEntitySensorFluidLevel tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        matrixStackIn.push();
        BlockState blockstate = tileEntityIn.getBlockState();
        matrixStackIn.rotate(Vector3f.ZN.rotationDegrees(180));
        matrixStackIn.translate(-0.5d, 0.1d, 0.5d);

        float f1 = (float)(blockstate.get(BlockSensorFluidLevel.ROTATION) * 360) / 16.0F + 180;
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(f1));

        int phaseIndex = tileEntityIn.getDialPhase();
        String s = TEXTURE_PATH + phaseIndex + ".png";
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(new ResourceLocation(MainClass.MODID, s)));

        model.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 1, 1, 1, 1);
        matrixStackIn.pop();
    }

    public static final class ModelSensorFluidLevelDial extends Model
    {
        public ModelRenderer rod;
        public ModelRenderer bracing;
        public ModelRenderer dial;

        public ModelSensorFluidLevelDial()
        {
            super(RenderType::getEntityCutoutNoCull);
            this.textureWidth = 16;
            this.textureHeight = 16;

            this.bracing = new ModelRenderer(this, 4, 5);
            this.bracing.mirror = true;
            this.bracing.setRotationPoint(0.0F, -2.5F, 0.0F);
            this.bracing.addBox(-0.7F, -1.0F, -1.3F, 1.4F, 0.5F, 2.0F, 0.0F, 0.0F, 0.0F);
            this.rod = new ModelRenderer(this, 0, 5);
            this.rod.setRotationPoint(0.0F, -2.5F, 0.0F);
            this.rod.addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, 0.0F, 0.0F, 0.0F);
            this.dial = new ModelRenderer(this, 0, 0);
            this.dial.setRotationPoint(0.0F, -1.0F, -0.7F);
            this.dial.addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 1.0F, 0.0F, 0.0F, 0.0F);
            this.setRotateAngle(dial, -0.3490658503988659F, 0.0F, 0.0F);

            this.rod.addChild(this.bracing);
            this.bracing.addChild(this.dial);
        }

        @Override
        public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
        {
            rod.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }

        public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
        {
            modelRenderer.rotateAngleX = x;
            modelRenderer.rotateAngleY = y;
            modelRenderer.rotateAngleZ = z;
        }
    }
}
