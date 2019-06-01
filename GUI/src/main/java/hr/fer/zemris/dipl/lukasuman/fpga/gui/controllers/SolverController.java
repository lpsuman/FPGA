package hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.SolverMode;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JPanelPair;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JTextAreaOutputStream;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJButton;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJLabel;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.BoolVecSolutionTableModel;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.MyJTable;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.TableItemListener;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;

public class SolverController extends AbstractGUIController<BoolVectorSolution> {

    private JComboBox<Integer> numCLBInputsComboBox;
    private JComboBox<SolverMode> solverModeComboBox;

    private JTextArea outputTextArea;
    private JButton clearTextButton;
    private JToggleButton clearToggleButton;

    public SolverController(SessionController parentSession) {
        super(parentSession, parentSession.getSessionData().getBoolSolutions());
    }

    @Override
    protected void loadData() {
        itemTableModel = new BoolVecSolutionTableModel(parentSession, getAllItems());
        itemTable = new MyJTable(itemTableModel);
        itemTable.setRowSelectionAllowed(true);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        itemTable.addColumnClickedListener(3, (row) -> {
            BooleanVector vectorClone = new BooleanVector(getItem(row).getBoolVector());
            getJfpga().getCurrentSession().getBooleanVectorController().setItems(Collections.singletonList(vectorClone));
        });
    }

    @Override
    protected void setupGUI() {
        initSolverGUI();
        initSolutionTable();
    }

    private void initSolverGUI() {
        JPanelPair panelPair = GUIUtility.generatePanelPair(mainPanel, 0, 0.66);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        numCLBInputsComboBox = GUIUtility.getComboBoxFromLimit(Constants.NUM_CLB_INPUTS_LIMIT);
        numCLBInputsComboBox.setSelectedIndex(1);
        upperPanel.add(GUIUtility.getComboBoxPanel(numCLBInputsComboBox, getLocProv(), LocalizationKeys.NUMBER_OF_CLB_INPUTS_KEY));

        solverModeComboBox = new JComboBox<>(SolverMode.values());
        upperPanel.add(GUIUtility.getComboBoxPanel(solverModeComboBox, getLocProv(), LocalizationKeys.SOLVING_MODE_KEY));

        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getRunSolverAction())));

        clearTextButton = new JButton(getJfpga().getClearOutputAction());
        //TODO toggle button
        clearToggleButton = new JToggleButton();

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JTextAreaOutputStream out = new JTextAreaOutputStream(outputTextArea);
        System.setOut(new PrintStream(out));
        lowerPanel.add(GUIUtility.getClearableTextArea(outputTextArea, clearTextButton, clearToggleButton), BorderLayout.CENTER);
    }

    private void initSolutionTable() {
        JPanelPair panelPair = GUIUtility.generatePanelPair(mainPanel, 1, 0.33);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        lowerPanel.add(new LJLabel(LocalizationKeys.SOLUTIONS_KEY, getLocProv(), SwingConstants.CENTER), BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        itemTable.applyMinSizeInScrollPane();
    }

    @Override
    protected Collection<? extends TableItemListener<BoolVectorSolution>> getTableItemListeners() {
        return null;
    }

    public JComboBox<Integer> getNumCLBInputsComboBox() {
        return numCLBInputsComboBox;
    }

    public JComboBox<SolverMode> getSolverModeComboBox() {
        return solverModeComboBox;
    }

    public JTextArea getOutputTextArea() {
        return outputTextArea;
    }

    public JButton getClearTextButton() {
        return clearTextButton;
    }

    public JToggleButton getClearToggleButton() {
        return clearToggleButton;
    }
}
