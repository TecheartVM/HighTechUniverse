package techeart.htu.utils;

import net.minecraft.util.IntReferenceHolder;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class HTUIntReferenceHolder extends IntReferenceHolder
{
    private final IntSupplier getter;
    private final IntConsumer setter;

    public HTUIntReferenceHolder(final IntSupplier getter, final IntConsumer setter)
    {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int get() { return getter.getAsInt(); }

    @Override
    public void set(int value) { setter.accept(value); }
}
