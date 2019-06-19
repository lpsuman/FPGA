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
import hr.fer.zemris.dipl.lukasuman.fpga.gui.LocalizationKeys;
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
    private JFormattedTextField maxNumFailsFTF;
    private JFormattedTextField noBestThresholdFTF;
    private JFormattedTextField bestExistsThresholdFTF;
    private JFormattedTextField maxBelowAttemptsFTF;

    private JCheckBox solveIndividuallyCheckBox;
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

        JPanel inputAndModePanel = GUIUtility.getPanel(new GridLayout(0, 2));
        upperPanel.add(inputAndModePanel);
        JPanel solverConfigLeftPanel = GUIUtility.getPanel(new GridBagLayout());
        inputAndModePanel.add(solverConfigLeftPanel);
        int indexInLeftPanel = 0;
        JPanel solverConfigRightPanel = GUIUtility.getPanel(new GridBagLayout());
        inputAndModePanel.add(solverConfigRightPanel);
        int indexInRightPanel = 0;

        numCLBInputsComboBox = GUIUtility.getComboBoxFromLimit(Constants.NUM_CLB_INPUTS_LIMIT);
        numCLBInputsComboBox.setSelectedIndex(GUIConstants.DEFAULT_NUM_CLB_INPUTS_COMBOBOX_INDEX);
        GUIUtility.addComboBoxPanel(numCLBInputsComboBox, getLocProv(),
                LocalizationKeys.NUMBER_OF_CLB_INPUTS_KEY, solverConfigLeftPanel, indexInLeftPanel++);

        solverModeComboBox = new LJComboBox<>(SolverMode.values(), getLocProv(), Arrays.asList(
                LocalizationKeys.BRUTE_SOLVE_MODE_KEY,
                LocalizationKeys.FAST_SOLVE_MODE_KEY,
                LocalizationKeys.NORMAL_SOLVE_MODE_KEY,
                LocalizationKeys.FULL_SOLVE_MODE_KEY
        ));
        solverModeComboBox.setSelectedIndex(GUIConstants.DEFAULT_SOLVE_MODE_COMBOBOX_INDEX);
        GUIUtility.addComboBoxPanel(solverModeComboBox, getLocProv(),
                LocalizationKeys.SOLVING_MODE_KEY, solverConfigRightPanel, indexInRightPanel++);

        populationSizeFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.POPULATION_SIZE_LIMIT, Constants.DEFAULT_POPULATION_SIZE);
        GUIUtility.addFTFPanel(populationSizeFTF, getLocProv(),
                LocalizationKeys.POPULATION_SIZE_KEY, solverConfigLeftPanel, indexInLeftPanel++);

        maxGenerationsFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.MAX_GENERATIONS_LIMIT, Constants.DEFAULT_MAX_NUM_GENERATIONS);
        GUIUtility.addFTFPanel(maxGenerationsFTF, getLocProv(),
                LocalizationKeys.NUMBER_OF_GENERATIONS_KEY, solverConfigRightPanel, indexInRightPanel++);

        numThreadsFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.NUM_THREADS_LIMIT, Constants.DEFAULT_NUM_WORKERS);
        GUIUtility.addFTFPanel(numThreadsFTF, getLocProv(),
                LocalizationKeys.NUMBER_OF_THREADS_KEY, solverConfigLeftPanel, indexInLeftPanel++);

        elitismSizeFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.ELITISM_SIZE_LIMIT, Constants.DEFAULT_MIN_ELITISM_SIZE);
        GUIUtility.addFTFPanel(elitismSizeFTF, getLocProv(),
                LocalizationKeys.SIZE_OF_ELITISM_KEY, solverConfigRightPanel, indexInRightPanel++);

        mutationChanceFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.DOUBLE_RATIO_LIMIT, Constants.OPERATOR_CHANCE_MULTIPLIER);
        GUIUtility.addFTFPanel(mutationChanceFTF, getLocProv(),
                LocalizationKeys.MUTATION_CHANCE_KEY, solverConfigLeftPanel, indexInLeftPanel++);

        annealingThresholdFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.ANNEALING_THRESHOLD_LIMIT, Constants.DEFAULT_ANNEALING_THRESHOLD);
        GUIUtility.addFTFPanel(annealingThresholdFTF, getLocProv(),
                LocalizationKeys.ANNEALING_THRESHOLD_KEY, solverConfigRightPanel, indexInRightPanel++);

        maxNumFailsFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.NUM_FAILS_LIMIT, Constants.DEFAULT_MAX_NUM_FAILS);
        GUIUtility.addFTFPanel(maxNumFailsFTF, getLocProv(),
                LocalizationKeys.MAX_NUM_FAILS_KEY, solverConfigLeftPanel, indexInLeftPanel++);

        maxBelowAttemptsFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.DOUBLE_RATIO_LIMIT, Constants.DEFAULT_MAX_NUM_BELOW_THRESHOLD_ATTEMPTS);
        GUIUtility.addFTFPanel(maxBelowAttemptsFTF, getLocProv(),
                LocalizationKeys.BELOW_THRESHOLD_ATTEMPTS_KEY, solverConfigRightPanel, indexInRightPanel++);

        noBestThresholdFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.DOUBLE_RATIO_LIMIT, Constants.DEFAULT_NO_BEST_THRESHOLD_TO_STOP_TRYING);
        GUIUtility.addFTFPanel(noBestThresholdFTF, getLocProv(),
                LocalizationKeys.NO_BEST_THRESHOLD_KEY, solverConfigLeftPanel, indexInLeftPanel++);

        bestExistsThresholdFTF = GUIUtility.getFormattedTextFieldFromLimit(
                Constants.DOUBLE_RATIO_LIMIT, Constants.DEFAULT_NO_BEST_THRESHOLD_TO_STOP_TRYING);
        GUIUtility.addFTFPanel(bestExistsThresholdFTF, getLocProv(),
                LocalizationKeys.BEST_EXISTS_THRESHOLD_KEY, solverConfigRightPanel, indexInRightPanel++);

        solveIndividuallyCheckBox = new JCheckBox();
        solveIndividuallyCheckBox.setSelected(Constants.DEFAULT_SOLVE_INDIVIDUALLY);
        GUIUtility.addcheckBoxPanel(solveIndividuallyCheckBox, getLocProv(),
                LocalizationKeys.SOLVE_INDIVIDUALLY_KEY, solverConfigLeftPanel, indexInLeftPanel++);

        printOnlyBestCheckBox = new JCheckBox();
        printOnlyBestCheckBox.setSelected(Constants.DEFAULT_PRINT_ONLY_BEST_SOLUTION);
        GUIUtility.addcheckBoxPanel(printOnlyBestCheckBox, getLocProv(),
                LocalizationKeys.PRINT_ONLY_BEST_SOLUTIONS_KEY, solverConfigRightPanel, indexInRightPanel++);

        useStatisticsCheckBox = new JCheckBox();
        useStatisticsCheckBox.setSelected(Constants.DEFAULT_USE_STATISTICS);
        GUIUtility.addcheckBoxPanel(useStatisticsCheckBox, getLocProv(),
                LocalizationKeys.USE_STATISTICS_KEY, solverConfigLeftPanel, indexInLeftPanel++);

        printOnlyGlobalStatisticsCheckBox = new JCheckBox();
        printOnlyGlobalStatisticsCheckBox.setSelected(Constants.DEFAULT_PRINT_ONLY_GLOBAL_STATISTICS);
        GUIUtility.addcheckBoxPanel(printOnlyGlobalStatisticsCheckBox, getLocProv(),
                LocalizationKeys.PRINT_ONLY_GLOBAL_STATISTICS_KEY, solverConfigRightPanel, indexInRightPanel++);

        JPanel runPanel = GUIUtility.getPanel(new GridLayout(1, 3));
        upperPanel.add(runPanel);
        runPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getRunSolverAction())));
        runPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getStopSolverAction())));
        clearTextButton = new JButton(getJfpga().getClearOutputAction());
        runPanel.add(GUIUtility.putIntoPanelWithBorder(clearTextButton));

        clearToggleButton = new JToggleButton();
        //TODO toggle button

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

    public JFormattedTextField getMaxNumFailsFTF() {
        return maxNumFailsFTF;
    }

    public JFormattedTextField getNoBestThresholdFTF() {
        return noBestThresholdFTF;
    }

    public JFormattedTextField getBestExistsThresholdFTF() {
        return bestExistsThresholdFTF;
    }

    public JFormattedTextField getMaxBelowAttemptsFTF() {
        return maxBelowAttemptsFTF;
    }

    public JCheckBox getSolveIndividuallyCheckBox() {
        return solveIndividuallyCheckBox;
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
