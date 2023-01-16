package fi.nls.hakunapi.core.util;

import java.util.Iterator;

public class SingleIterator<T> implements Iterator<T> {

    private final T t;
    private boolean has;

    public SingleIterator(T t) {
        this.t = t;
        this.has = t != null;
    }

    @Override
    public boolean hasNext() {
        if (has) {
            has = false;
            return true;
        }
        return false;
    }

    @Override
    public T next() {
        return t;
    }

}
