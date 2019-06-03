package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.solve;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;

import java.awt.event.ActionEvent;

public class StopSolverAction extends AbstractAppAction {

    public StopSolverAction(JFPGA jfpga) {
        super(jfpga, LocalizationKeys.STOP_SOLVER_KEY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jfpga.getCurrentSession().stopBooleanSolver();
    }
}
