package techeart.htu.objects.sensors.temperature;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import techeart.htu.MainClass;
import techeart.htu.utils.ColorConstants;
import techeart.htu.utils.network.packets.PacketSensorTemperatureConfig;

import java.util.function.Predicate;

public class GuiSensorTemperatureConfig extends Screen
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(MainClass.MODID + ":textures/gui/config_temperature_sensor.png");

    protected int xSize = 204;
    protected int ySize = 166;
    protected int guiLeft;
    protected int guiTop;

    protected TextFieldWidget txtMax;
    protected TextFieldWidget txtMin;
    protected Button btnMaxInc;
    protected Button btnMaxDec;
    protected Button btnMinInc;
    protected Button btnMinDec;
    protected Button btnCancel;
    protected Button btnConfirm;

    //predicate for text fields to accept only numbers and minus symbol
    protected Predicate<String> onlyNumbers = (s) -> {
        if(s.length() <= 0) return true;
        char[] chars = s.toCharArray();
        int i = 0;
        for (char c : chars)
        {
            if (!Character.isDigit(c) && !(c == '-' && i == 0)) return false;
            i++;
        }
        return true;
    };

    private final TileEntitySensorTemperature tile;

    private int minTemperature;
    private int maxTemperature;
    private int temperature;

    protected GuiSensorTemperatureConfig(TileEntitySensorTemperature tile)
    {
        super(new TranslationTextComponent("htu.gui.sensor_temperature.config"));
        this.tile = tile;
        minTemperature = tile.getMinTemperature();
        maxTemperature = tile.getMaxTemperature();
        temperature = tile.getTemperature();

        System.out.println(minTemperature);
        System.out.println(maxTemperature);
        System.out.println(temperature);
    }

    protected void submitChanges()
    {
        applyMinTemp();
        applyMaxTemp();

        //sending settings to server
        MainClass.PACKET_HANDLER.sendToServer(new PacketSensorTemperatureConfig(tile.getPos(), minTemperature, maxTemperature));
        //setting on client for future requests from this gui
        tile.setMeasurementLimits(minTemperature, maxTemperature);

        closeScreen();
    }

    protected void applyMinTemp()
    {
        if(!txtMin.getText().isEmpty())
            minTemperature = MathHelper.clamp(Integer.parseInt(txtMin.getText()), TileEntitySensorTemperature.MIN_TEMPERATURE, maxTemperature - 1);
        txtMin.setText(String.valueOf(minTemperature));
    }

    protected void applyMaxTemp()
    {
        if(!txtMax.getText().isEmpty())
            maxTemperature = MathHelper.clamp(Integer.parseInt(txtMax.getText()), minTemperature + 1, TileEntitySensorTemperature.MAX_TEMPERATURE);
        txtMax.setText(String.valueOf(maxTemperature));
    }

    @Override
    public void init()
    {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        minecraft.keyboardListener.enableRepeatEvents(true);

        btnCancel = addButton(new Button(guiLeft + 104, guiTop + 146, 100, 20, DialogTexts.GUI_CANCEL, (param) -> closeScreen()));
        btnConfirm = addButton(new Button(guiLeft, guiTop + 146, 100, 20, DialogTexts.GUI_DONE, (param) -> submitChanges()));

        btnMaxInc = addButton(new Button(guiLeft, guiTop, 20, 20, new StringTextComponent("-"), (param) -> {
            if(maxTemperature > minTemperature + 1) maxTemperature--;
            txtMax.setText(String.valueOf(maxTemperature));
        }));
        btnMaxDec = addButton(new Button(guiLeft + 100, guiTop, 20, 20, new StringTextComponent("+"), (param) -> {
            if(maxTemperature < TileEntitySensorTemperature.MAX_TEMPERATURE) maxTemperature++;
            txtMax.setText(String.valueOf(maxTemperature));
        }));
        btnMinInc = addButton(new Button(guiLeft, guiTop + 112, 20, 20, new StringTextComponent("-"), (param) -> {
            if(minTemperature > TileEntitySensorTemperature.MIN_TEMPERATURE) minTemperature--;
            txtMin.setText(String.valueOf(minTemperature));
        }));
        btnMinDec = addButton(new Button(guiLeft + 100, guiTop + 112, 20, 20, new StringTextComponent("+"), (param) -> {
            if(minTemperature < maxTemperature - 1) minTemperature++;
            txtMin.setText(String.valueOf(minTemperature));
        }));

        txtMax = new TextFieldWidget(this.font, guiLeft + 20, guiTop + 2, 80, 16, new StringTextComponent("Max t")) {
            @Override
            public void setFocused2(boolean focused)
            {
                boolean flag = txtMax.isFocused();
                super.setFocused(focused);
                if(flag && !txtMax.isFocused()) applyMaxTemp();
            }
        };
        txtMax.setCanLoseFocus(true);
        txtMax.setMaxStringLength(6);
        txtMax.setValidator(onlyNumbers);
        txtMax.setText(String.valueOf(maxTemperature));
        children.add(txtMax);
        setFocusedDefault(txtMax);
        txtMax.setFocused2(true);

        txtMin = new TextFieldWidget(this.font, guiLeft + 20, guiTop + 114, 80, 16, new StringTextComponent("Min t")) {
            @Override
            public void setFocused2(boolean focused)
            {
                boolean flag = txtMin.isFocused();
                super.setFocused(focused);
                if(flag && !txtMin.isFocused()) applyMinTemp();
            }
        };
        txtMin.setCanLoseFocus(true);
        txtMin.setMaxStringLength(6);
        txtMin.setValidator(onlyNumbers);
        txtMin.setText(String.valueOf(minTemperature));
        children.add(txtMin);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(matrixStack);
        txtMax.render(matrixStack, mouseX, mouseY, partialTicks);
        txtMin.render(matrixStack, mouseX, mouseY, partialTicks);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(MatrixStack matrixStack)
    {
        super.renderBackground(matrixStack);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        minecraft.getTextureManager().bindTexture(TEXTURE);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);

        //draw bar
        int i = MathHelper.clamp(Math.floorDiv((temperature - minTemperature) * 80, maxTemperature - minTemperature), 0, 80);
        blit(matrixStack, guiLeft + 150, guiTop + 23 + 80 - i, 204, 80 - i, 22, i);

        //draw title
        font.drawText(matrixStack, title, guiLeft + xSize / 2f - font.getStringWidth(title.getString()) / 2f, guiTop - 14, ColorConstants.TRANSPARENT_GUI_TEXT.getColor());
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
