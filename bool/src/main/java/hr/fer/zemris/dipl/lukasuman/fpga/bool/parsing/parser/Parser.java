package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.Lexer;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.Token;

public interface Parser<T extends Token, S> {

    S parse(Lexer<T> lexer);
}
