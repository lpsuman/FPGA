package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser;

public interface GenericFactory<T> {

    void register(String name, Class<? extends T> clazz);
    void register(Class<? extends T> clazz, String... names);
    void registerDefault(Class<? extends T> defaultClass);
    boolean isMappingPresent(String name);
    T getForName(String name, Object... arguments);
}
