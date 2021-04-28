package techeart.htu.utils;

import com.google.common.collect.Lists;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ObservableNNList<E> extends NonNullList<E>
{
    private IOnChangedFunc onChangedFunc = () -> { };

    public static <E> NonNullList<E> create(IOnChangedFunc onChangedFunc) { return new ObservableNNList<>(onChangedFunc); }

    public static <E> ObservableNNList<E> withSize(int size, E fill, IOnChangedFunc onChangedFunc)
    {
        Validate.notNull(fill);
        Object[] aobject = new Object[size];
        Arrays.fill(aobject, fill);
        return new ObservableNNList<>(Arrays.asList((E[])aobject), fill, onChangedFunc);
    }

    public static <E> NonNullList<E> from(E defaultElementIn, IOnChangedFunc onChangedFunc, E... elements)
    {
        return new ObservableNNList<>(Arrays.asList(elements), defaultElementIn, onChangedFunc);
    }

    protected ObservableNNList(IOnChangedFunc onChangedFunc) {
        this(Lists.newArrayList(), (E)null, onChangedFunc);
    }

    protected ObservableNNList(List<E> delegateIn, @Nullable E listType, IOnChangedFunc onChangedFunc)
    {
        super(delegateIn, listType);
        this.onChangedFunc = onChangedFunc;
    }

    public E set(int index, E obj)
    {
        E res = super.set(index, obj);
        onChangedFunc.onChanged();
        return res;
    }

    public void add(int index, E obj)
    {
        super.add(index, obj);
        onChangedFunc.onChanged();
    }

    public E remove(int index)
    {
        E res = super.remove(index);
        onChangedFunc.onChanged();
        return res;
    }

    public void clear()
    {
        super.clear();
        onChangedFunc.onChanged();
    }

    @FunctionalInterface
    public interface IOnChangedFunc
    {
        void onChanged();
    }
}
