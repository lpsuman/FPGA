package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.solve;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PrintSolutionAction extends AbstractAppAction {

    private Supplier<BoolVectorSolution> solutionSupplier;
    private Consumer<String> textConsumer;

    public PrintSolutionAction(JFPGA jfpga, Supplier<BoolVectorSolution> solutionSupplier, Consumer<String> textConsumer) {
        super(jfpga, LocalizationKeys.PRINT_SOLUTION_KEY);
        this.solutionSupplier = Utility.checkNull(solutionSupplier, "solution supplier");
        this.textConsumer = Utility.checkNull(textConsumer, "text consumer");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BoolVectorSolution solution = solutionSupplier.get();

        if (solution == null) {
            jfpga.showWarningMsg(String.format(jfpga.getFlp().getString(LocalizationKeys.SELECT_S_FROM_THE_TABLE_MSG_KEY),
                    jfpga.getFlp().getString(LocalizationKeys.ONE_OR_MORE_SOLUTIONS_KEY)));
            return;
        }

        textConsumer.accept(solution.toString());
    }
}
