package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * A concrete implementation of {@link GenericFactory}.
 * Stores names in a map and creates new instances through Java reflections.
 * @param <T> Type of the generated objects.
 */
public class SimpleGenericFactory<T> implements GenericFactory<T> {

    private Map<String, Class<? extends T>> nameToClassMap;
    private Class<? extends T> defaultClass;

    public SimpleGenericFactory() {
        nameToClassMap = new HashMap<>();
    }

    @Override
    public void register(String name, Class<? extends T> clazz) {
        nameToClassMap.put(Utility.checkNull(name, "operator name"), Utility.checkNull(clazz, "class"));
    }

    @Override
    public void register(Class<? extends T> clazz, String... names) {
        Utility.checkNull(names, "names");
        for (String name : names) {
            register(name, clazz);
        }
    }

    @Override
    public void registerDefault(Class<? extends T> defaultClass) {
        this.defaultClass = defaultClass;
    }

    @Override
    public boolean isMappingPresent(String name) {
        return nameToClassMap.get(name) != null;
    }

    @Override
    public T getForName(String name, Object... arguments) {
        Class<? extends T> clazz = nameToClassMap.get(name);

        if (clazz == null) {
            if (defaultClass == null) {
                return null;
            } else {
                return newInstance(defaultClass, arguments);
            }
        }

        return newInstance(clazz, arguments);
    }

    private T newInstance(Class<? extends T> clazz, Object... arguments) {

        Constructor<? extends T> ctr;

        try {
            if (arguments != null) {
                Class<?>[] classes = (Class<?>[]) new Class[arguments.length];
                for (int i = 0; i < arguments.length; i++) {
                    classes[i] = arguments[i].getClass();
                }
                ctr = clazz.getConstructor(classes);
            } else {
                ctr = clazz.getConstructor();
            }
        } catch (NoSuchMethodException e) {
            ctr = (Constructor<? extends T>) clazz.getConstructors()[0];
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }

        try {
            if (arguments == null) {
                return ctr.newInstance();
            } else {
                return ctr.newInstance(arguments);
            }
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
