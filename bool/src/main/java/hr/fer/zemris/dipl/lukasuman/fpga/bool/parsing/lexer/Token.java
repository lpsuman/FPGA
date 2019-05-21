package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer;

/**
 * A token produced by a {@link Lexer}. See {@link BoolToken} as an example.
 * @param <T> Type of the token.
 * @param <V> Value stored in the token.
 */
public interface Token<T, V> {

    T getType();
    V getValue();
}
