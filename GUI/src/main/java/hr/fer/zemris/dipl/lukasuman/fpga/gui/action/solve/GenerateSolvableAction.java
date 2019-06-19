package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.solve;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.LocalizationKeys;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GenerateSolvableAction extends AbstractAppAction {

    public GenerateSolvableAction(JFPGA jfpga, String localizationKey) {
        super(jfpga, localizationKey);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (jfpga.getCurrentSession() == null) {
            return;
        }
        int numInputs = GUIUtility.getSelectedComboBoxValue(jfpga.getCurrentSession().getBooleanVectorController().getNumInputsComboBox());
        int numFunctions = GUIUtility.getSelectedComboBoxValue(jfpga.getCurrentSession().getBooleanVectorController().getNumFunctionsComboBox());
        int numCLBInputs = GUIUtility.getSelectedComboBoxValue(jfpga.getCurrentSession().getSolverController().getNumCLBInputsComboBox());

        Object input = JOptionPane.showInputDialog(jfpga,
                jfpga.getFlp().getString(LocalizationKeys.INPUT_NUM_CLB_MSG_KEY),
                jfpga.getFlp().getString(LocalizationKeys.INPUT_NUM_CLB_MSG_KEY),
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "" + (2 * numFunctions));
        if (input == null) {
            return;
        }
        int numCLB = Integer.parseInt((String) input);

        BooleanVector solvableVector = BoolFuncController.generateSolvable(numInputs, numFunctions, numCLBInputs, numCLB, false);
        solvableVector.setName("solvable_" + numCLB);
        jfpga.getCurrentSession().getBooleanVectorController().addItem(solvableVector);
    }
}
