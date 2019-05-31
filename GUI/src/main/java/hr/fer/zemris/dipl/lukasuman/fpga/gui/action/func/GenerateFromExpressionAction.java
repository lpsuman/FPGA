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

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GenerateFromExpressionAction extends AbstractAppAction {

    private TextProvider textProvider;

    public GenerateFromExpressionAction(JFPGA jfpga, TextProvider textProvider) {
        super(jfpga, LocalizationKeys.GENERATE_FROM_EXPRESSION_KEY);
        this.textProvider = Utility.checkNull(textProvider, "expression provider");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String expression = textProvider.getText();

        if (expression == null) {
            return;
        }

        BoolParser parser = jfpga.getParser();
        BoolExpression boolExpression = null;

        try {
            boolExpression = parser.parse(new BoolLexer(Utility.getInputStreamFromString(expression)));
        } catch (BoolParserException exc) {
            showErrorMsg(exc.getMessage());
            return;
        }

        BooleanFunction newFunc = BoolFuncController.generateFromExpression(boolExpression);
        jfpga.getCurrentSession().getBooleanFunctionController().addItem(newFunc);
    }
}
