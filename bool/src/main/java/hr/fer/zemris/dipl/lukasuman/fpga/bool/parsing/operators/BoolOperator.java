package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators;

/**
 * A boolean operator. May use left and/or right operand (operator NOT doesn't have a left operand).
 * Also has a priority (0 has the highest priority, higher values have lower priority).
 */
public interface BoolOperator extends Comparable<BoolOperator> {

    boolean isUsingLeft();
    boolean isUsingRight();

    BoolOperator getLeft();
    void setLeft(BoolOperator left);

    BoolOperator getRight();
    void setRight(BoolOperator right);

    BoolOperator getParent();
    void setParent(BoolOperator parent);

    int getPriority();
    boolean interpret();
    void buildExpression(StringBuilder sb);
}
