package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolLexer;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolParser;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolParserException;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParserTests {

    private static void doTest(String input, int truthTable, int length) {
        BoolParser parser = new BoolParser();
        BoolExpression boolExpression = parser.parse(new BoolLexer(Utility.getInputStreamFromString(input)));

        BitSet calculatedTruthTable = boolExpression.getTruthTable();
        BitSet expectedTruthTable = Utility.bitSetFromMask(truthTable, length);
        calculatedTruthTable.xor(expectedTruthTable);

        assertEquals(calculatedTruthTable.cardinality(), 0);
    }

    private static void testThrows(String input) {
        assertThrows(BoolParserException.class, () -> doTest(input, 0, 2));
    }

    @Test
    void testSimple() {
        doTest("a and b", 0b0001, 4);
    }

    @Test
    void testXOR() {
        doTest("(a or b) and not (a and b)", 0b0110, 4);
    }

    @Test
    void test1() {
        doTest("a xor b and not (b or a)", 0b0011, 4);
    }

    @Test
    void testEmptyThrows() {
        testThrows("");
        testThrows(" ");
        testThrows("a or b and not () and a");
    }

    @Test
    void testOnlyParenthesesThrows() {
        testThrows(")");
        testThrows("[()]");
        testThrows("( (( ) )) ");
    }

    @Test
    void testNoClosedThrows() {
        testThrows("(a or b");
        testThrows("(a or b and not (a and b)");
        testThrows("a and b (");
    }

    @Test
    void testNoOpenThrows() {
        testThrows(") and a");
        testThrows("a xor b)");
        testThrows("a not b or a) and b");
    }

    @Test
    void missingOperandThrows() {
        testThrows("and b");
        testThrows("a and or a");
        testThrows("a and not");
        testThrows("a or (and b)");
        testThrows("a or (b and) xor a");
    }

    @Test
    void missingOperatorThrows() {
        testThrows("a");
        testThrows("a and b a or not a");
        testThrows("a not b");
        testThrows("a (a and b)");
        testThrows("a xor (a or b) not b");
    }

    @Test
    void epicTest() {
        doTest("a xor b and not ((c or d) and e)", 0b00000000111010101111111100010101, 32);
    }
}
