package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.solve;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.FuncToExpressionConverter;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.SolverController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Consumer;

public class SolutionToExpressionAction extends AbstractAppAction {

    private Consumer<List<String>> expressionConsumer;

    public SolutionToExpressionAction(JFPGA jfpga, Consumer<List<String>> expressionConsumer) {
        super(jfpga, LocalizationKeys.GENERATE_EXPRESSION_KEY);
        this.expressionConsumer = Utility.checkNull(expressionConsumer, "expression consumer");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SolverController solverController = jfpga.getCurrentSession().getSolverController();

        if (solverController.getIndexSelectedItem() == -1) {
            jfpga.showWarningMsg(String.format(jfpga.getFlp().getString(LocalizationKeys.SELECT_S_FROM_THE_TABLE_MSG_KEY),
                    jfpga.getFlp().getString(LocalizationKeys.ONE_OR_MORE_SOLUTIONS_KEY)));
            return;
        }

        BoolVectorSolution selectedSolution = solverController.getSelectedItem();
        FuncToExpressionConverter.setMapping(solverController.getMappingTypesJComboBox().getItemAt(
                solverController.getMappingTypesJComboBox().getSelectedIndex()));
        expressionConsumer.accept(selectedSolution.getAsExpressions());
    }
}
