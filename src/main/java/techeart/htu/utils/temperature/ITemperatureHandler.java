package techeart.htu.utils.temperature;

public interface ITemperatureHandler
{
    int getTemperature();
    void setTemperature(int value);

    /**Returns the maximum measurable temperature.*/
    default int getMaxTemperature() { return 50; }
    /**Returns the minimum measurable temperature.*/
    default int getMinTemperature() { return -30; }
}
