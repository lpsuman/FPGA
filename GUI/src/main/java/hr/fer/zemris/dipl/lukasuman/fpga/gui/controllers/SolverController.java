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
import java.util.*;

public class SolverController extends AbstractGUIController<BoolVectorSolution> {

    private Set<TableItemListener<BoolVectorSolution>> solutionListeners;

    private JComboBox<Integer> numCLBInputsComboBox;
    private JComboBox<SolverMode> solverModeComboBox;

    private JFormattedTextField populationSizeFTF;
    private JFormattedTextField maxGenerationsFTF;
    private JFormattedTextField elitismSizeFTF;
    private JFormattedTextField numThreadsFTF;
    private JFormattedTextField annealingThresholdFTF;
    private JFormattedTextField mutationChanceFTF;

    private JCheckBox printOnlyBestCheckBox;
    private JCheckBox useStatisticsCheckBox;
    private JCheckBox printOnlyGlobalStatisticsCheckBox;

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

        JPanel inputAndModePanel = GUIUtility.getPanel(new GridLayout(1, 2));
        upperPanel.add(inputAndModePanel);
        numCLBInputsComboBox = GUIUtility.getComboBoxFromLimit(Constants.NUM_CLB_INPUTS_LIMIT);
        numCLBInputsComboBox.setSelectedIndex(GUIConstants.DEFAULT_NUM_CLB_INPUTS_COMBOBOX_INDEX);
        inputAndModePanel.add(GUIUtility.getComboBoxPanel(numCLBInputsComboBox, getLocProv(),
                LocalizationKeys.NUMBER_OF_CLB_INPUTS_KEY));

        solverModeComboBox = new LJComboBox<>(SolverMode.values(), getLocProv(), Arrays.asList(
                LocalizationKeys.BRUTE_SOLVE_MODE_KEY,
                LocalizationKeys.FAST_SOLVE_MODE_KEY,
                LocalizationKeys.NORMAL_SOLVE_MODE_KEY,
                LocalizationKeys.FULL_SOLVE_MODE_KEY
        ));
        solverModeComboBox.setSelectedIndex(GUIConstants.DEFAULT_SOLVE_MODE_COMBOBOX_INDEX);
        inputAndModePanel.add(GUIUtility.getComboBoxPanel(solverModeComboBox, getLocProv(),
                LocalizationKeys.SOLVING_MODE_KEY));

        JPanel solverConfigPanel = GUIUtility.getPanel(new GridBagLayout());
        upperPanel.add(solverConfigPanel);
        int indexInPanel = 0;

        populationSizeFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.POPULATION_SIZE_LIMIT, Constants.DEFAULT_POPULATION_SIZE);
        GUIUtility.addFTFPanel(populationSizeFTF, getLocProv(),
                LocalizationKeys.POPULATION_SIZE_KEY, solverConfigPanel, indexInPanel++);

        maxGenerationsFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.MAX_GENERATIONS_LIMIT, Constants.DEFAULT_MAX_NUM_GENERATIONS);
        GUIUtility.addFTFPanel(maxGenerationsFTF, getLocProv(),
                LocalizationKeys.NUMBER_OF_GENERATIONS_KEY, solverConfigPanel, indexInPanel++);

        elitismSizeFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.ELITISM_SIZE_LIMIT, Constants.DEFAULT_MIN_ELITISM_SIZE);
        GUIUtility.addFTFPanel(elitismSizeFTF, getLocProv(),
                LocalizationKeys.SIZE_OF_ELITISM_KEY, solverConfigPanel, indexInPanel++);

        numThreadsFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.NUM_THREADS_LIMIT, Constants.DEFAULT_NUM_WORKERS);
        GUIUtility.addFTFPanel(numThreadsFTF, getLocProv(),
                LocalizationKeys.NUMBER_OF_THREADS_KEY, solverConfigPanel, indexInPanel++);

        annealingThresholdFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.ANNEALING_THRESHOLD_LIMIT, Constants.DEFAULT_ANNEALING_THRESHOLD);
        GUIUtility.addFTFPanel(annealingThresholdFTF, getLocProv(),
                LocalizationKeys.ANNEALING_THRESHOLD_KEY, solverConfigPanel, indexInPanel++);

        mutationChanceFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.DOUBLE_RATIO_LIMIT, Constants.OPERATOR_CHANCE_MULTIPLIER);
        GUIUtility.addFTFPanel(mutationChanceFTF, getLocProv(),
                LocalizationKeys.MUTATION_CHANCE_KEY, solverConfigPanel, indexInPanel++);

        printOnlyBestCheckBox = new JCheckBox();
        printOnlyBestCheckBox.setSelected(Constants.DEFAULT_PRINT_ONLY_BEST_SOLUTION);
        GUIUtility.addcheckBoxPanel(printOnlyBestCheckBox, getLocProv(),
                LocalizationKeys.PRINT_ONLY_BEST_SOLUTIONS_KEY, solverConfigPanel, indexInPanel++);

        useStatisticsCheckBox = new JCheckBox();
        useStatisticsCheckBox.setSelected(Constants.DEFAULT_USE_STATISTICS);
        GUIUtility.addcheckBoxPanel(useStatisticsCheckBox, getLocProv(),
                LocalizationKeys.USE_STATISTICS_KEY, solverConfigPanel, indexInPanel++);

        printOnlyGlobalStatisticsCheckBox = new JCheckBox();
        printOnlyGlobalStatisticsCheckBox.setSelected(Constants.DEFAULT_PRINT_ONLY_GLOBAL_STATISTICS);
        GUIUtility.addcheckBoxPanel(printOnlyGlobalStatisticsCheckBox, getLocProv(),
                LocalizationKeys.PRINT_ONLY_GLOBAL_STATISTICS_KEY, solverConfigPanel, indexInPanel++);

        JPanel runPanel = GUIUtility.getPanel(new GridLayout(1, 2));
        upperPanel.add(runPanel);
        runPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getRunSolverAction())));
        runPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getStopSolverAction())));

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
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getPrintSolutionAction())));
        if (GUIConstants.SHOW_REMOVE_BUTTONS) {
            upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getRemoveSelectedSolutionAction())));
        }

        lowerPanel.add(new LJLabel(LocalizationKeys.SOLUTIONS_KEY, getLocProv(), SwingConstants.CENTER), BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        itemTable.applyMinSizeInScrollPane();
    }

    @Override
    public void updateActionsAreEnabled() {
        getJfpga().getPrintSolutionAction().setEnabled(true);
        getJfpga().getRemoveSelectedSolutionAction().setEnabled(areItemsEditable());
        getJfpga().getUndoRemoveSolutionAction().setEnabled(getRemoveCount() > 0);
        getJfpga().getSaveSolutionsAction().setEnabled(true);
        getJfpga().getLoadSolutionsAction().setEnabled(true);
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

    public JFormattedTextField getPopulationSizeFTF() {
        return populationSizeFTF;
    }

    public JFormattedTextField getMaxGenerationsFTF() {
        return maxGenerationsFTF;
    }

    public JFormattedTextField getElitismSizeFTF() {
        return elitismSizeFTF;
    }

    public JFormattedTextField getNumThreadsFTF() {
        return numThreadsFTF;
    }

    public JFormattedTextField getAnnealingThresholdFTF() {
        return annealingThresholdFTF;
    }

    public JFormattedTextField getMutationChanceFTF() {
        return mutationChanceFTF;
    }

    public JCheckBox getPrintOnlyBestCheckBox() {
        return printOnlyBestCheckBox;
    }

    public JCheckBox getUseStatisticsCheckBox() {
        return useStatisticsCheckBox;
    }

    public JCheckBox getPrintOnlyGlobalStatisticsCheckBox() {
        return printOnlyGlobalStatisticsCheckBox;
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

    public void addSolutionListener(TableItemListener<BoolVectorSolution> listener) {
        if (solutionListeners == null) {
            solutionListeners = new HashSet<>();
        }
        solutionListeners.add(listener);
    }

    public void removeSolutionListener(TableItemListener<BoolVectorSolution> listener) {
        if (solutionListeners != null) {
            solutionListeners.remove(listener);
        }
    }
}
