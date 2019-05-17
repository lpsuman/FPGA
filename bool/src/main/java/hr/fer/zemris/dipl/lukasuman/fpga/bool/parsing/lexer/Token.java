package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer;

public interface Token<T, V> {

    T getType();
    V getValue();
}
