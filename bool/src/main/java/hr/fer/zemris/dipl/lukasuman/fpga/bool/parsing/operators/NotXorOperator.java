package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators;

public class NotXorOperator extends AbstractBoolOperator {

    public static final String[] NAMES = new String[]{"xnor"};

    public NotXorOperator() {
        super(2, NAMES[0]);
    }

    @Override
    protected boolean interpret(boolean left, boolean right) {
        return (!left && !right) || (left && right);
    }
}
