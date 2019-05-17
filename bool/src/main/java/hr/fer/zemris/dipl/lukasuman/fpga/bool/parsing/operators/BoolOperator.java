package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators;

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
