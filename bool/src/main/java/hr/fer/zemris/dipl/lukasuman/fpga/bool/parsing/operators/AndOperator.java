package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators;

public class AndOperator extends AbstractBoolOperator {

    public static final String[] NAMES = new String[]{"and"};

    public AndOperator() {
        super(1, NAMES[0]);
    }

    @Override
    protected boolean interpret(boolean left, boolean right) {
        return left && right;
    }
}
