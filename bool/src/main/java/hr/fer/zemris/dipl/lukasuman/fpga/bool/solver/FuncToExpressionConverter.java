package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class FuncToExpressionConverter {

    public enum FuncToStringMappingTypes {
        NOT_AND_OR_XOR_NAND_NOR_XNOR(0),
        NOT_AND_OR_XOR(1),
        NOT_AND_OR(2)
        ;

        private final int index;

        FuncToStringMappingTypes(int index) {
            this.index = index;
        }
    }

    private static class FuncToString {
        private final String formatString;
        private final boolean usesFirstVariable;
        private final boolean usesSecondVariable;
        private final boolean isDNF;

        public FuncToString(String formatString, boolean usesFirstVariable, boolean usesSecondVariable, boolean isDNF) {
            Utility.checkNull(formatString, "format string");
            int leftParenthesisCount = countChars(formatString, '(');
            int rightParenthesisCount = countChars(formatString, ')');

            if (leftParenthesisCount != rightParenthesisCount) {
                throw new IllegalArgumentException("Invalid number of parentheses in string format: " + formatString);
            }

            int variableCount = countChars(formatString, '%');
            if (isDNF) {
                if (variableCount != 4 || !usesFirstVariable || !usesSecondVariable) {
                    throw new IllegalArgumentException("Invalid DNF string format: " + formatString);
                }
            } else {
                if (usesFirstVariable) {
                    variableCount--;
                }
                if (usesSecondVariable) {
                    variableCount--;
                }
                if (variableCount != 0) {
                    throw new IllegalArgumentException("Invalid string format: " + formatString);
                }
            }
            this.formatString = formatString;
            this.usesFirstVariable = usesFirstVariable;
            this.usesSecondVariable = usesSecondVariable;
            this.isDNF = isDNF;
        }

        public FuncToString(String formatString, boolean usesFirstVariable, boolean usesSecondVariable) {
            this(formatString, usesFirstVariable, usesSecondVariable, false);
        }

        public FuncToString(String formatString) {
            this(formatString, true, true);
        }

        private int countChars(String str, char ch) {
            return (int) str.chars()
                    .filter(c -> c == ch)
                    .count();
        }
    }

    private static final FuncToString[] FULL_FUNC_TO_STRING_MAPPING = new FuncToString[]{
            // 0000
            new FuncToString("False", false, false),
            // 0001
            new FuncToString("(%s and %s)"),
            // 0010
            new FuncToString("(%s and not %s)"),
            // 0011
            new FuncToString("%s", true, false),
            // 0100
            new FuncToString("(not %s and %s)"),
            // 0101
            new FuncToString("%s", false, true),
            // 0110
            new FuncToString("(%s xor %s)"),
            // 0111
            new FuncToString("(%s or %s)"),
            // 1000
            new FuncToString("(%s nor %s)"),
            // 1001
            new FuncToString("(%s xnor %s)"),
            // 1010
            new FuncToString("not %s", false, true),
            // 1011
            new FuncToString("(%s or not %s)"),
            // 1100
            new FuncToString("not %s", true, false),
            // 1101
            new FuncToString("(not %s or %s)"),
            // 1110
            new FuncToString("(%s nand %s)"),
            // 1111
            new FuncToString("True", false, false)
    };

    private static final FuncToString[] XOR_FUNC_TO_STRING_MAPPING = new FuncToString[16];
    private static final FuncToString[] SIMPLE_FUNC_TO_STRING_MAPPING = new FuncToString[16];
    private static final List<FuncToString[]> FUNC_TO_STRING_MAPPINGS =
            new ArrayList<>(FuncToStringMappingTypes.values().length);
    private static FuncToStringMappingTypes CURRENT_MAPPING_TYPE;

    static {
        System.arraycopy(FULL_FUNC_TO_STRING_MAPPING, 0, XOR_FUNC_TO_STRING_MAPPING, 0, 16);
        XOR_FUNC_TO_STRING_MAPPING[0b1000] = new FuncToString("not (%s or %s)");
        XOR_FUNC_TO_STRING_MAPPING[0b1001] = new FuncToString("not (%s xor %s)");
        XOR_FUNC_TO_STRING_MAPPING[0b1110] = new FuncToString("not (%s and %s)");

        System.arraycopy(XOR_FUNC_TO_STRING_MAPPING, 0, SIMPLE_FUNC_TO_STRING_MAPPING, 0, 16);
        SIMPLE_FUNC_TO_STRING_MAPPING[0b0110] = new FuncToString("((%s and not %s) or (not %s and %s))", true, true, true);
        SIMPLE_FUNC_TO_STRING_MAPPING[0b1001] = new FuncToString("((%s and %s) or (not %s and not %s))", true, true, true);

        FUNC_TO_STRING_MAPPINGS.add(FULL_FUNC_TO_STRING_MAPPING);
        FUNC_TO_STRING_MAPPINGS.add(XOR_FUNC_TO_STRING_MAPPING);
        FUNC_TO_STRING_MAPPINGS.add(SIMPLE_FUNC_TO_STRING_MAPPING);
        CURRENT_MAPPING_TYPE = FuncToStringMappingTypes.NOT_AND_OR_XOR;
    }

    private FuncToExpressionConverter() {
    }

    public static void setMapping(FuncToStringMappingTypes mappingType) {
        Utility.checkNull(mappingType, "mapping type");
        CURRENT_MAPPING_TYPE = mappingType;
    }

    public static String getString(int truthTable, String strA, String strB) {
        Utility.checkRange(truthTable, 0, 15);
        FuncToString funcToString = FUNC_TO_STRING_MAPPINGS.get(CURRENT_MAPPING_TYPE.index)[truthTable];

        if (funcToString.isDNF) {
            checkA(strA);
            checkB(strB);
            return String.format(funcToString.formatString, strA, strB, strA, strB);
        }

        if (funcToString.usesFirstVariable) {
            checkA(strA);
            if (funcToString.usesSecondVariable) {
                checkB(strB);
                return String.format(funcToString.formatString, strA, strB);
            } else {
                return String.format(funcToString.formatString, strA);
            }
        } else {
            if (funcToString.usesSecondVariable) {
                checkB(strB);
                return String.format(funcToString.formatString, strB);
            } else {
                return funcToString.formatString;
            }
        }
    }

    private static void checkA(String strA) {
        Utility.checkIfValidString(strA, "left operand string");
    }

    private static  void checkB(String strB) {
        Utility.checkIfValidString(strB, "right operand string");
    }
}
