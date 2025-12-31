package net.pixelateddream.macebattles.util;

public final class ProtectedList<T> extends java.util.ArrayList<T> {
    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public T remove(int index) {
        return null;
    }

    @Override
    public void clear() {
    }
}
