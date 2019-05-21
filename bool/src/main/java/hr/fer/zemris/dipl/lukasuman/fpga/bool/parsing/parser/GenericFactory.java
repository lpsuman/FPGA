package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser;

/**
 * A generic factory to which concrete implementations can register under a name.
 * Instances may be generated if the name is known. See {@link SimpleGenericFactory} as an example.
 * @param <T> Type of the generated objects.
 */
public interface GenericFactory<T> {

    void register(String name, Class<? extends T> clazz);
    void register(Class<? extends T> clazz, String... names);
    void registerDefault(Class<? extends T> defaultClass);
    boolean isMappingPresent(String name);
    T getForName(String name, Object... arguments);
}
