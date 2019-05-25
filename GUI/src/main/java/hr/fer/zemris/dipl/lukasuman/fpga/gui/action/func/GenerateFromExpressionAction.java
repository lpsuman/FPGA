package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.BoolExpression;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolLexer;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolParser;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolParserException;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;

public class GenerateFromExpressionAction extends AbstractAppAction {

    private static final long serialVersionUID = -3105847486580084996L;

    private BooleanExpressionProvider booleanExpressionProvider;

    public GenerateFromExpressionAction(JFPGA jfpga, BooleanExpressionProvider booleanExpressionProvider) {
        super(jfpga, LocalizationKeys.GENERATE_FROM_EXPRESSION_KEY);
        this.booleanExpressionProvider = Utility.checkNull(booleanExpressionProvider, "expression provider");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String expression = booleanExpressionProvider.getExpressionString();

        if (expression == null) {
            return;
        }

        BoolParser parser = jfpga.getParser();
        BoolExpression boolExpression = null;

        try {
            boolExpression = parser.parse(new BoolLexer(Utility.getInputStreamFromString(expression)));
        } catch (BoolParserException exc) {
            return;
        }

        BooleanFunction newFunc = BoolFuncController.generateFromExpression(boolExpression);
        jfpga.getCurrentSession().getBooleanFunctionController().addBooleanFunction(newFunc);
    }
}
