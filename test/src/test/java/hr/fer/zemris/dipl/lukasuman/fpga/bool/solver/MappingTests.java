package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MappingTests {

    private static final String OPER_A = "a";
    private static final String OPER_B = "b";

    @Test
    void testFull() {
        String result = FuncToExpressionConverter.getString(0b0001, OPER_A, OPER_B);
        assertNotNull(result);
        assertEquals(String.format("%s and %s", OPER_A, OPER_B), result);

        FuncToExpressionConverter.setMapping(FuncToExpressionConverter.FuncToStringMappingTypes.NOT_AND_OR_XOR_NAND_NOR_XNOR);
        result = FuncToExpressionConverter.getString(0b1001, OPER_A, OPER_B);
        assertNotNull(result);
        assertEquals(String.format("%s xnor %s", OPER_A, OPER_B), result);

        FuncToExpressionConverter.setMapping(FuncToExpressionConverter.FuncToStringMappingTypes.NOT_AND_OR_XOR);
        result = FuncToExpressionConverter.getString(0b1001, OPER_A, OPER_B);
        assertNotNull(result);
        assertEquals(String.format("not (%s xor %s)", OPER_A, OPER_B), result);

        FuncToExpressionConverter.setMapping(FuncToExpressionConverter.FuncToStringMappingTypes.NOT_AND_OR);
        result = FuncToExpressionConverter.getString(0b1001, OPER_A, OPER_B);
        assertNotNull(result);
        assertEquals(String.format("(%s and %s) or (not %s and not %s)", OPER_A, OPER_B, OPER_A, OPER_B), result);
    }
}
