package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.solve;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.SolverMode;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public class RunSolverAction extends AbstractAppAction {

    private Supplier<BooleanVector> boolVectorProvider;
    private Supplier<Integer> numCLBInputsProvider;
    private Supplier<SolverMode> solverModeProvider;

    public RunSolverAction(JFPGA jfpga,
                           Supplier<BooleanVector> boolVectorProvider,
                           Supplier<Integer> numCLBInputsProvider,
                           Supplier<SolverMode> solverModeProvider) {

        super(jfpga, LocalizationKeys.RUN_SOLVER_KEY);
        this.boolVectorProvider = Utility.checkNull(boolVectorProvider, "boolean vector provider");
        this.numCLBInputsProvider = Utility.checkNull(numCLBInputsProvider, "number of CLB inputs provider");
        this.solverModeProvider = Utility.checkNull(solverModeProvider, "solver mode provider");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BooleanVector vectorToSolve;

        try {
            vectorToSolve = boolVectorProvider.get();
        } catch (IllegalArgumentException exc) {
            jfpga.showWarningMsg(jfpga.getFlp().getString(LocalizationKeys.NO_VECTOR_SELECTED_MSG_KEY));
            return;
        }

        //TODO check if auto clear text

        jfpga.getCurrentSession().runBooleanSolver(new BoolVecProblem(vectorToSolve, numCLBInputsProvider.get()),
                solverModeProvider.get());
    }
}
