package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators.BoolOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.util.ArgumentIntervalLimit;
import hr.fer.zemris.dipl.lukasuman.fpga.util.ArgumentLimit;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.BitSet;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a boolean expression which stores its operators in a tree in order to maintain
 * operator priority. A string representation is also stored.
 */
public class BoolExpression implements InputProvider {

    private List<String> inputIDs;
    private BoolOperator root;
    private BitSet truthTable;
    private String expressionString;

    private int inputCombination;
    private int numInputCombinations;
    private ArgumentLimit<Integer> inputCombinationLimit;

    public BoolExpression(List<String> inputIDs) {
        Utility.checkNull(inputIDs, "input IDs");

        if (inputIDs.isEmpty()) {
            throw new IllegalArgumentException("List of input IDs must not be empty.");
        }

        inputIDs.forEach(id -> Utility.checkNull(id, "input ID"));
        this.inputIDs = inputIDs;
        Utility.checkLimit(Constants.NUM_FUNCTION_INPUTS_LIMIT, this.inputIDs.size());
        numInputCombinations = (int) Math.pow(2, this.inputIDs.size());
        inputCombinationLimit = new ArgumentIntervalLimit<>("input combination", 0, numInputCombinations);
    }

    @Override
    public int getIndexForID(String inputID) {
        Utility.checkNull(inputID, "inputID");

        int index = inputIDs.indexOf(inputID);

        if (index < 0) {
            throw new IllegalArgumentException("No such input ID in this boolean expression: " + inputID);
        }

        return index;
    }

    @Override
    public boolean getInput(int inputIndex) {
        if (inputIndex < 0 || inputIndex >= inputIDs.size()) {
            throw new IllegalArgumentException("Invalid input index: " + inputIndex);
        }

        return Utility.testBitFromRight(inputCombination, inputIDs.size() - 1 - inputIndex);
    }

    public void setRoot(BoolOperator root) {
        testIfValid(root);
        this.root = root;
        calcTruthTable();
        StringBuilder sb = new StringBuilder();
        root.buildExpression(sb);

        if (root.getPriority() > 0) {
            expressionString = sb.toString().substring(1, sb.length() - 1);
        } else {
            expressionString = sb.toString();
        }
    }

    public boolean evaluate(int inputCombination) {
        if (truthTable == null) {
            throw new IllegalArgumentException("BoolExpression must have a root set before evaluating.");
        }

        Utility.checkLimit(inputCombinationLimit, inputCombination);

        return truthTable.get(inputCombination);
    }

    private void calcTruthTable() {
        truthTable = new BitSet(numInputCombinations);

        for (int i = 0; i < numInputCombinations; ++i) {
            this.inputCombination = i;
            truthTable.set(i, root.interpret());
        }
    }

    public BitSet getTruthTable() {
        if (truthTable == null) {
            throw new IllegalStateException("Boolean expression requires a root in order to have a truth table.");
        }

        return truthTable;
    }

    private static void testIfValid(BoolOperator node) {
        if (node.isUsingLeft()) {
            if (node.getLeft() == null) {
                throw new IllegalArgumentException("Node " + node + " has no left operand.");
            }
            if (node.getLeft().getParent() != node) {
                throw new IllegalArgumentException("Node " + node + " has an invalid parent node.");
            }
            testIfValid(node.getLeft());
        }

        if (node.isUsingRight()) {
            if (node.getRight() == null) {
                throw new IllegalArgumentException("Node " + node + " has no right operand.");
            }
            if (node.getRight().getParent() != node) {
                throw new IllegalArgumentException("Node " + node + " has an invalid parent node.");
            }
            testIfValid(node.getRight());
        }
    }

    public List<String> getInputIDs() {
        return inputIDs;
    }

    @Override
    public String toString() {
        return Objects.requireNonNullElse(expressionString, "[Empty boolean expression]");
    }
}
