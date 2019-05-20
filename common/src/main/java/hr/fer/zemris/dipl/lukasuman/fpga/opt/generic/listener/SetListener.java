package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener;

import java.util.HashSet;
import java.util.Set;

public class SetListener<T> {

    private Set<T> listeners;

    public SetListener() {
    }

    public void addListener(T listener) {
        if (listeners == null) {
            listeners = new HashSet<>();
        }
        listeners.add(listener);
    }

    public void removeListener(T listener) {
        if (hasListeners()) {
            listeners.remove(listener);
        }
    }

    public boolean hasListeners() {
        return listeners != null && !listeners.isEmpty();
    }

    public Set<T> getListeners() {
        return listeners;
    }
}
