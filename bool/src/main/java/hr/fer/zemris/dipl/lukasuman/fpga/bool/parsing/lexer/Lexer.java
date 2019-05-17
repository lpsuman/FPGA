package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer;

public interface Lexer<T extends Token> {

    T nextToken();
}
