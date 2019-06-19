package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.BoolExpression;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolLexer;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolParser;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolParserException;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.function.Supplier;

public class GenerateFromExpressionAction extends AbstractGenerateFromTextAction {

    public GenerateFromExpressionAction(JFPGA jfpga, Supplier<String> textProvider) {
        super(jfpga, textProvider, LocalizationKeys.GENERATE_FROM_EXPRESSION_KEY);
    }

    @Override
    protected void doAction(String text) {
        String funcName = null;
        int equalsSignIndex = text.indexOf('=');
        if (equalsSignIndex != -1) {
            funcName = text.substring(0, equalsSignIndex).trim();
            text = text.substring(equalsSignIndex + 1);
        }
        BoolParser parser = jfpga.getParser();
        BoolExpression boolExpression;

        try {
            boolExpression = parser.parse(new BoolLexer(Utility.getInputStreamFromString(text)));
        } catch (BoolParserException exc) {
            jfpga.showErrorMsg(exc.getMessage());
            return;
        }

        BooleanFunction newFunc = BoolFuncController.generateFromExpression(boolExpression);
        newFunc.setExpressionGeneratedFrom(text);
        if (funcName != null) {
            newFunc.setName(funcName);
        }
        jfpga.getCurrentSession().getBooleanFunctionController().addItem(newFunc);
    }
}
