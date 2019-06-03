package hr.fer.zemris.dipl.lukasuman.fpga.gui.table;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;

import java.util.ArrayList;
import java.util.List;

public class BoolVecSolutionTableModel extends MyAbstractTableModel<BoolVectorSolution> {

    private static final List<BoolVectorSolution> DEFAULT_ITEMS = new ArrayList<>();
    private static Double[] COLUMN_WIDTH_PERCENTAGES = new Double[]{1.0, 4.0, 1.0, 1.0, 1.0, 1.0};

    public BoolVecSolutionTableModel(SessionController parentSession, AbstractGUIController parentController,
                                     List<BoolVectorSolution> boolVectorSolutions) {

        super(parentSession, parentController, boolVectorSolutions,
                LocalizationKeys.INDEX_KEY,
                LocalizationKeys.NAME_KEY,
                LocalizationKeys.INPUTS_KEY,
                LocalizationKeys.FUNCTIONS_KEY,
                LocalizationKeys.INPUTS_KEY,
                LocalizationKeys.BLOCKS_KEY);
        setColumnWidthPercentages(COLUMN_WIDTH_PERCENTAGES);
    }

    @Override
    protected List<BoolVectorSolution> getDefaultItems() {
        return DEFAULT_ITEMS;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (!displayIndices) {
            columnIndex += 1;
        }

        switch (columnIndex) {
            case 0:
                return rowIndex;
            case 1:
                return items.get(rowIndex).getName();
            case 2:
                return items.get(rowIndex).getBoolVector().getNumInputs();
            case 3:
                return items.get(rowIndex).getBoolVector().getNumFunctions();
            case 4:
                return items.get(rowIndex).getBlockConfiguration().getNumCLBInputs();
            case 5:
                return items.get(rowIndex).getBlockConfiguration().getNumCLB();
            default:
                throw new IllegalArgumentException("Invalid column index: " + columnIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        renameItem(aValue, rowIndex, columnIndex, parentSession.getSolverController());
    }
}
