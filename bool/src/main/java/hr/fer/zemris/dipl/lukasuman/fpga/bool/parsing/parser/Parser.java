package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.Lexer;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.Token;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Resetable;

public interface Parser<T extends Token, S> extends Resetable {

    S parse(Lexer<T> lexer);
}
