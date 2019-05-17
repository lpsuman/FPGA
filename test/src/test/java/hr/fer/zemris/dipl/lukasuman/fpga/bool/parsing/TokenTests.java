package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolToken;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolTokenType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenTests {

    private static final BoolTokenType DUMMY_TYPE = BoolTokenType.WORD;
    private static final String DUMMY_STRING = "lol";

    @Test
    void testNullTypeThrow() {
        assertThrows(NullPointerException.class, () -> new BoolToken(null, DUMMY_STRING));
    }

    @Test
    void testNullValueIsOK() {
        BoolToken token = new BoolToken(BoolTokenType.PARENTHESIS_LEFT, null);
        assertEquals(token.getType(), BoolTokenType.PARENTHESIS_LEFT);
        assertNull(token.getValue());
    }

    @Test
    void testNullValueThrow() {
        assertThrows(IllegalArgumentException.class, () -> new BoolToken(BoolTokenType.WORD, null));
    }

    @Test
    void testValueIsSaved() {
        BoolToken token = new BoolToken(DUMMY_TYPE, DUMMY_STRING);
        assertEquals(token.getType(), DUMMY_TYPE);
        assertEquals(token.getValue(), DUMMY_STRING);
    }
}
