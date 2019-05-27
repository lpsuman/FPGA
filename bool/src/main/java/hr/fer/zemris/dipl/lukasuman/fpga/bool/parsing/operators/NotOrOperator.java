package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators;

public class NotOrOperator extends AbstractBoolOperator {

    public static final String[] NAMES = new String[]{"nor"};

    public NotOrOperator() {
        super(3, NAMES[0]);
    }

    @Override
    protected boolean interpret(boolean left, boolean right) {
        return !(left || right);
    }
}
