package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.InputProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolOperatorFactory;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

public class SimpleInputOperator extends AbstractBoolOperator {

    private InputProvider inputProvider;
    private int inputIndex;

    public SimpleInputOperator(InputProvider inputProvider, String inputID) {
        super(false, false, -1, inputID);
        this.inputProvider = Utility.checkNull(inputProvider, "input provider");
        this.inputIndex = this.inputProvider.getIndexForID(inputID);
        BoolOperatorFactory.getGenericFactory().registerDefault(this.getClass());
    }

    @Override
    protected boolean interpret(boolean left, boolean right) {
        return inputProvider.getInput(inputIndex);
    }

    @Override
    public void buildExpression(StringBuilder sb) {
        sb.append(toString());
    }
}
