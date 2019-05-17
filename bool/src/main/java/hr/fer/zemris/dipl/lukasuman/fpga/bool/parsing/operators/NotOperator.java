package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators;

public class NotOperator extends AbstractBoolOperator {

    public static final String[] NAMES = new String[]{"not"};

    public NotOperator() {
        super(false, true, 0, NAMES[0]);
    }

    @Override
    protected boolean interpret(boolean left, boolean right) {
        return !right;
    }
}
