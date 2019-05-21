package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer;

/**
 * A lexicographical analyzer. See {@link BoolLexer} as an example.
 * @param <T> Type of the {@link Token}.
 */
public interface Lexer<T extends Token> {

    T nextToken();
}
