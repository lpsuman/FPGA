package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.*;

/**
 * This class represents a lexicographical analyzer of boolean expressions. {@link StreamTokenizer} is used on the
 * given input stream in order to handle special characters and whitespaces.
 * See {@link BoolTokenType} for the token types.
 */
public class BoolLexer implements Lexer<BoolToken> {

    private static final char[] LEFT_PARENTHESIS_CHARS = new char[]{'(', '[', '{'};
    private static final char[] RIGHT_PARENTHESIS_CHARS = new char[]{')', ']', '}'};

    private StreamTokenizer tokenizer;

    public BoolLexer(InputStream in) {
        Utility.checkNull(in, "input stream");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        tokenizer = new StreamTokenizer(reader);
        tokenizer.resetSyntax();

        tokenizer.whitespaceChars('\u0000', ' ');
//        tokenizer.whitespaceChars('\n', '\t');
        tokenizer.eolIsSignificant(true);
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('|', '|');
        tokenizer.wordChars('&', '&');
        tokenizer.wordChars('^', '^');

        for (char ordinaryChar : LEFT_PARENTHESIS_CHARS) {
            tokenizer.ordinaryChar(ordinaryChar);
        }

        for (char ordinaryChar : RIGHT_PARENTHESIS_CHARS) {
            tokenizer.ordinaryChar(ordinaryChar);
        }
    }

    @Override
    public BoolToken nextToken() {
        BoolTokenType type = null;
        String value = null;
        try {
            int token;
            loop:
            switch (token = tokenizer.nextToken()) {
                case StreamTokenizer.TT_EOL:
                    type = BoolTokenType.EOL;
                    break;
                case StreamTokenizer.TT_EOF:
                    type = BoolTokenType.EOF;
                    break;
                case StreamTokenizer.TT_WORD:
                    type = BoolTokenType.WORD;
                    value = tokenizer.sval;
                    break;
                default:
                    for (char leftParenthesisChar : LEFT_PARENTHESIS_CHARS) {
                        if (token == leftParenthesisChar) {
                            type = BoolTokenType.PARENTHESIS_LEFT;
                            break loop;
                        }
                    }
                    for (char rightParenthesisChar : RIGHT_PARENTHESIS_CHARS) {
                        if (token == rightParenthesisChar) {
                            type = BoolTokenType.PARENTHESIS_RIGHT;
                            break loop;
                        }
                    }
            }
        } catch (IOException e) {
            throw new BoolLexerException(e.getMessage());
        }

        return new BoolToken(type, value);
    }
}
