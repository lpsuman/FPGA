package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolLexer;
import hr.fer.zemris.dipl.lukasuman.fpga.util.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Duplicateable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

/**
 * This class represents the lowest level representation of a boolean function. It is uniquely defined by a truth table.
 * It also stores a name (for easier handling) and a list of input IDs in string form ("a", "b", etc.).
 */
public class BooleanFunction extends AbstractNameHandler implements Serializable, Duplicateable<BooleanFunction> {

    private static final long serialVersionUID = -3337214107238850818L;

    private static final List<Character> EXPRESSION_NON_WORD_CHARACTERS = new ArrayList<>();
    private static final String DEFAULT_NAME = "BooleanFunction";
    private static final String INPUT_IDS_MSG = "input IDs for function";

    static {
        EXPRESSION_NON_WORD_CHARACTERS.add(' ');
        EXPRESSION_NON_WORD_CHARACTERS.addAll(BoolLexer.LEFT_PARENTHESIS_CHARS);
        EXPRESSION_NON_WORD_CHARACTERS.addAll(BoolLexer.RIGHT_PARENTHESIS_CHARS);
    }

    private int numInputs;
    private List<String> inputIDs;
    private int numInputCombinations;
    private BitSet truthTable;
    private String expressionGeneratedFrom;

    public BooleanFunction(List<String> inputIDs, BitSet truthTable, String name) {
        super(name);
        this.inputIDs = Utility.checkIfValidCollection(inputIDs, INPUT_IDS_MSG);
        Utility.checkLimit(Constants.NUM_FUNCTION_INPUTS_LIMIT, inputIDs.size());
        this.truthTable = Utility.checkNull(truthTable, "truth table");
        numInputs = inputIDs.size();
        numInputCombinations = (int) Math.pow(2, numInputs);

        if (truthTable.length() > (1 << inputIDs.size())) {
            throw new IllegalArgumentException(String.format(
                    "Not enough inputs (%d) for the given truth table (of min size %d).",
                    inputIDs.size(), truthTable.length()));
        }
    }

    public BooleanFunction(List<String> inputIDs, BitSet truthTable) {
        this(inputIDs, truthTable, DEFAULT_NAME);
    }

    public BooleanFunction(int numInputs, BitSet truthTable, String name) {
        this(BoolFuncController.generateDefaultInputIDs(numInputs), truthTable, name);
    }

    public BooleanFunction(int numInputs, BitSet truthTable) {
        this(numInputs, truthTable, DEFAULT_NAME);
    }

    public BooleanFunction(BooleanFunction other) {
        this(new ArrayList<>(other.getInputIDs()), (BitSet) other.getTruthTable().clone(), other.getName());
        setExpressionGeneratedFrom(other.getExpressionGeneratedFrom());
    }

    public List<String> getInputIDs() {
        return inputIDs;
    }

    public void setInputIDs(List<String> inputIDs) {
        Utility.checkIfValidCollection(inputIDs, INPUT_IDS_MSG);

        if (inputIDs.size() != this.inputIDs.size()) {
            throw new IllegalArgumentException(String.format("New list of input IDs (size %d) must contain the " +
                    "same number of inputs as this function (%d inputs).", inputIDs.size(), this.inputIDs.size()));
        }

        this.inputIDs = inputIDs;
    }

    public BitSet getTruthTable() {
        return truthTable;
    }

    public int getNumInputs() {
        return numInputs;
    }

    public int getNumInputCombinations() {
        return numInputCombinations;
    }

    public String getExpressionGeneratedFrom() {
        return expressionGeneratedFrom;
    }

    public void setExpressionGeneratedFrom(String expressionGeneratedFrom) {
        this.expressionGeneratedFrom = expressionGeneratedFrom;
    }

    public void updateInputsInExpression(String oldID, String newID) {
        if (expressionGeneratedFrom == null) {
            return;
        }
        for (char leftNonWordChar : EXPRESSION_NON_WORD_CHARACTERS) {
            for (char RightNonWordChar : EXPRESSION_NON_WORD_CHARACTERS) {
                expressionGeneratedFrom = expressionGeneratedFrom.replace(
                        leftNonWordChar + oldID + RightNonWordChar,
                        leftNonWordChar + newID + RightNonWordChar);
            }
        }
        if (expressionGeneratedFrom.startsWith(oldID)) {
            expressionGeneratedFrom = newID + expressionGeneratedFrom.substring(oldID.length());
        }
        if (expressionGeneratedFrom.endsWith(oldID)) {
            expressionGeneratedFrom = expressionGeneratedFrom.substring(0, expressionGeneratedFrom.length() - oldID.length()) + newID;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanFunction boolFunc = (BooleanFunction) o;
        return truthTable.equals(boolFunc.truthTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(truthTable);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Boolean Function, name={%s}, inputs={", getName()));
        Utility.appendStringList(sb, inputIDs);
        sb.append("}\n");
        Utility.appendBitSet(sb, truthTable, numInputCombinations);

        return sb.toString();
    }

    @Override
    public BooleanFunction getDuplicate() {
        return new BooleanFunction(this);
    }
}
