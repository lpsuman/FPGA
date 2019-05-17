package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

public abstract class AbstractBoolOperator implements BoolOperator {

    private boolean isUsingLeft;
    private boolean isUsingRight;
    private int priority;

    private BoolOperator left;
    private BoolOperator right;
    private BoolOperator parent;

    private String name;

    protected AbstractBoolOperator(boolean isUsingLeft, boolean isUsingRight, int priority, String name) {
        Utility.checkNull(name, "operator names");

        this.isUsingLeft = isUsingLeft;
        this.isUsingRight = isUsingRight;
        this.priority = priority;
        this.name = name;
    }

    protected AbstractBoolOperator(int priority, String name) {
        this(true, true, priority, name);
    }

    @Override
    public boolean isUsingLeft() {
        return isUsingLeft;
    }

    @Override
    public boolean isUsingRight() {
        return isUsingRight;
    }

    @Override
    public BoolOperator getLeft() {
        return left;
    }

    @Override
    public void setLeft(BoolOperator left) {
        if (!isUsingLeft) {
            throw new IllegalArgumentException("This operator doesn't use a left operand.");
        }

        this.left = Utility.checkNull(left, "left operator");
        left.setParent(this);
    }

    @Override
    public BoolOperator getRight() {
        return right;
    }

    @Override
    public void setRight(BoolOperator right) {
        if (!isUsingRight) {
            throw new IllegalArgumentException("This operator doesn't use a right operand");
        }

        this.right = Utility.checkNull(right, "right operator");
        right.setParent(this);
    }

    @Override
    public BoolOperator getParent() {
        return parent;
    }

    @Override
    public void setParent(BoolOperator parent) {
        Utility.checkNull(parent, "parent operator");
        this.parent = parent;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(BoolOperator o) {
        return -Integer.compare(this.getPriority(), o.getPriority());
    }

    @Override
    public boolean interpret() {
        boolean leftValue = false;
        boolean rightValue = false;

        if (isUsingLeft) {
            if (left == null) {
                throw new IllegalStateException("Left operand must be set before interpreting this operand.");
            }
            leftValue = left.interpret();
        }

        if (isUsingRight) {
            if (right == null) {
                throw new IllegalStateException("Right operand must be set before interpreting this operand.");
            }
            rightValue = right.interpret();
        }

        return interpret(leftValue, rightValue);
    }

    protected abstract boolean interpret(boolean left, boolean right);

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void buildExpression(StringBuilder sb) {
        sb.append("(");

        if (isUsingLeft) {
            left.buildExpression(sb);
        }

        sb.append(" ").append(name).append(" ");

        if (isUsingRight) {
            right.buildExpression(sb);
        }

        sb.append(")");
    }
}
