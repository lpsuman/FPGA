package hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.FuncToExpressionConverter;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.SolverMode;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JPanelPair;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JTextAreaOutputStream;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJComboBox;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJComboBoxRenderer;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJLabel;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.BoolVecSolutionTableModel;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.MyJTable;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.TableItemListener;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class SolverController extends AbstractGUIController<BoolVectorSolution> {

    private JComboBox<Integer> numCLBInputsComboBox;
    private JComboBox<SolverMode> solverModeComboBox;

    private JTextArea outputTextArea;
    private PrintStream textAreaOutputStream;
    private JButton clearTextButton;
    private JToggleButton clearToggleButton;

    private JComboBox<FuncToExpressionConverter.FuncToStringMappingTypes> mappingTypesJComboBox;

    public SolverController(SessionController parentSession) {
        super(parentSession, parentSession.getSessionData().getBoolSolutions());
    }

    @Override
    protected void loadData() {
        itemTableModel = new BoolVecSolutionTableModel(parentSession, this, getAllItems());
        itemTable = new MyJTable(itemTableModel);
        itemTable.setRowSelectionAllowed(true);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        itemTable.addColumnClickedListener(3, (row) -> {
            BooleanVector vectorClone = new BooleanVector(getItem(row).getBoolVector());
            getJfpga().getCurrentSession().getBooleanVectorController().setItems(Collections.singletonList(vectorClone), false);
        });

        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = getIndexSelectedItem();
                if (selectedIndex != -1) {
                    getJfpga().getSolutionToExpressionAction().setEnabled(getSelectedItem().canBeConvertedToExpression());
                }
            }
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
//        numCLBInputsComboBox.setSelectedIndex(1);
        upperPanel.add(GUIUtility.getComboBoxPanel(numCLBInputsComboBox, getLocProv(), LocalizationKeys.NUMBER_OF_CLB_INPUTS_KEY));

        solverModeComboBox = new LJComboBox<>(SolverMode.values(), getLocProv(), Arrays.asList(
                LocalizationKeys.BRUTE_SOLVE_MODE_KEY,
                LocalizationKeys.FAST_SOLVE_MODE_KEY,
                LocalizationKeys.NORMAL_SOLVE_MODE_KEY,
                LocalizationKeys.FULL_SOLVE_MODE_KEY
        ));
        solverModeComboBox.setSelectedIndex(1);
        upperPanel.add(GUIUtility.getComboBoxPanel(solverModeComboBox, getLocProv(), LocalizationKeys.SOLVING_MODE_KEY));

        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getRunSolverAction())));
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getStopSolverAction())));

        clearTextButton = new JButton(getJfpga().getClearOutputAction());
        //TODO toggle button
        clearToggleButton = new JToggleButton();

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
//        ((DefaultCaret)outputTextArea.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        textAreaOutputStream = new PrintStream(new JTextAreaOutputStream(outputTextArea, GUIConstants.REDIRECT_OUT));
        lowerPanel.add(GUIUtility.getClearableTextArea(outputTextArea, clearTextButton, clearToggleButton), BorderLayout.CENTER);
    }

    private void initSolutionTable() {
        JPanelPair panelPair = GUIUtility.generatePanelPair(mainPanel, 1, 0.33);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getSolutionToExpressionAction())));
        mappingTypesJComboBox = new JComboBox<>(FuncToExpressionConverter.FuncToStringMappingTypes.values());
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(mappingTypesJComboBox));
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getRemoveSelectedSolutionAction())));

        lowerPanel.add(new LJLabel(LocalizationKeys.SOLUTIONS_KEY, getLocProv(), SwingConstants.CENTER), BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        itemTable.applyMinSizeInScrollPane();
    }

    @Override
    public void updateActionsAreEnabled() {
        getJfpga().getRemoveSelectedSolutionAction().setEnabled(areItemsEditable());
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

    public PrintStream getTextAreaOutputStream() {
        return textAreaOutputStream;
    }

    public JButton getClearTextButton() {
        return clearTextButton;
    }

    public JToggleButton getClearToggleButton() {
        return clearToggleButton;
    }

    public JComboBox<FuncToExpressionConverter.FuncToStringMappingTypes> getMappingTypesJComboBox() {
        return mappingTypesJComboBox;
    }
}
