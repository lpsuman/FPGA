package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolLexer;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolToken;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolTokenType;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexerTests {

    private static final BoolToken EOF_TOKEN = new BoolToken(BoolTokenType.EOF, null);

    private static void testForInput(String input, BoolToken... expectedTokens) {
        BoolLexer lexer = new BoolLexer(Utility.getInputStreamFromString(input));

        if (expectedTokens != null) {
            for (BoolToken expectedToken : expectedTokens) {
                BoolToken token = lexer.nextToken();
                assertEquals(token, expectedToken);
            }
        }

        assertEquals(lexer.nextToken(), EOF_TOKEN);
    }

    @Test
    void testEmpty() {
        testForInput("");
    }

    @Test
    void testEndOfLine() {
        testForInput("\n", new BoolToken(BoolTokenType.EOL, null));
    }

    @Test
    void testSimple() {
        testForInput("a or b",
                new BoolToken(BoolTokenType.WORD, "a"),
                new BoolToken(BoolTokenType.WORD, "or"),
                new BoolToken(BoolTokenType.WORD, "b"));
    }

    @Test
    void testNumeric() {
        testForInput(" 12   F3abc ",
                new BoolToken(BoolTokenType.WORD, "12"),
                new BoolToken(BoolTokenType.WORD, "F3abc"));
    }

    @Test
    void testParentheses() {
        testForInput("[( a)1 (b)] }{",
                new BoolToken(BoolTokenType.PARENTHESIS_LEFT, null),
                new BoolToken(BoolTokenType.PARENTHESIS_LEFT, null),
                new BoolToken(BoolTokenType.WORD, "a"),
                new BoolToken(BoolTokenType.PARENTHESIS_RIGHT, null),
                new BoolToken(BoolTokenType.WORD, "1"),
                new BoolToken(BoolTokenType.PARENTHESIS_LEFT, null),
                new BoolToken(BoolTokenType.WORD, "b"),
                new BoolToken(BoolTokenType.PARENTHESIS_RIGHT, null),
                new BoolToken(BoolTokenType.PARENTHESIS_RIGHT, null),
                new BoolToken(BoolTokenType.PARENTHESIS_RIGHT, null),
                new BoolToken(BoolTokenType.PARENTHESIS_LEFT, null));
    }
}
