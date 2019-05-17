package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing;

import hr.fer.zemris.dipl.lukasuman.fpga.TestUtil;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators.BoolOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators.SimpleInputOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolOperatorFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BoolExpressionTests {

    @Test
    void testConstructor() {
        TestUtil.argThrow(() -> new BoolExpression(null));
        TestUtil.argThrow(() -> new BoolExpression(new ArrayList<>()));
        TestUtil.argThrow(() -> new BoolExpression(new ArrayList<>(Arrays.asList("test", null))));
    }

    @Test
    void testAll() {
        List<String> indices = new ArrayList<>(Arrays.asList("a", "b", "c"));
        BoolExpression expression = new BoolExpression(indices);

        TestUtil.argThrow(() -> expression.getIndexForID(null));
        TestUtil.argThrow(() -> expression.getIndexForID(""));
        TestUtil.argThrow(() -> expression.getIndexForID("d"));
        TestUtil.argThrow(() -> expression.getInput(-1));
        TestUtil.argThrow(() -> expression.getInput(3));
        TestUtil.argThrow(() -> expression.evaluate(0));

        BoolOperator inputA = new SimpleInputOperator(expression, "a");
        BoolOperator inputB = new SimpleInputOperator(expression, "b");
        BoolOperator inputC = new SimpleInputOperator(expression, "c");
        BoolOperator xor = BoolOperatorFactory.getGenericFactory().getForName("xor");
        xor.setLeft(inputA);
        xor.setRight(inputB);
        BoolOperator and = BoolOperatorFactory.getGenericFactory().getForName("and");
        and.setLeft(xor);
        and.setRight(inputC);
        expression.setRoot(and);

        assertNotNull(expression.getTruthTable());
        assertEquals(expression.getIndexForID("a"), 0);
        assertEquals(expression.getIndexForID("b"), 1);
        assertEquals(expression.getIndexForID("c"), 2);

        assertFalse(expression.evaluate(2));
        assertTrue(expression.evaluate(3));
        assertFalse(expression.evaluate(4));
        assertTrue(expression.evaluate(5));

        assertEquals(expression.toString(), "(a xor b) and c");
    }
}
