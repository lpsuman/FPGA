package hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolOperatorFactory;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JPanelPair;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.*;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.*;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BooleanFunctionController extends AbstractGUIController<BooleanFunction> {

    private Set<BooleanFunctionListener> booleanFunctionListeners;

    private InputTableModel inputTableModel;
    private MyJTable inputTable;

    private TruthTableModel truthTableModel;
    private MyJTable truthTable;

    private JTextArea expressionTextArea;
    private JTextArea showExpressionTextArea;
    private JComboBox<Integer> numInputsComboBox;

    public BooleanFunctionController(SessionController parentSession) {
        super(parentSession, parentSession.getSessionData().getBoolFunctions());
    }

    @Override
    protected void loadData() {
        itemTableModel = new FuncTableModel(parentSession, this, getAllItems());
        itemTable = new MyJTable(itemTableModel);
        itemTable.setRowSelectionAllowed(true);
        itemTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        inputTableModel = new InputTableModel(parentSession, this);
        inputTable = new MyJTable(inputTableModel);

        truthTableModel = new TruthTableModel(parentSession, this);
        truthTable = new MyJTable(truthTableModel);

        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                List<BooleanFunction> selectedFunctions = getSelectedItems();

                if (selectedFunctions.isEmpty()) {
                    truthTableModel.loadDefaultItems();
                    inputTableModel.loadDefaultItems();
                } else if (selectedFunctions.size() == 1) {
                    BooleanFunction selectedFunction = selectedFunctions.get(0);
                    truthTableModel.setData(selectedFunction);
                    inputTableModel.setItems(selectedFunction.getInputIDs());
                } else {
                    try {
                        BooleanVector selectionVector = new BooleanVector(selectedFunctions);
                        truthTableModel.setData(selectionVector);
                        inputTableModel.setItems(selectionVector.getSortedInputIDs());
                    } catch (IllegalArgumentException exc) {
                        getJfpga().showErrorMsg(String.format(
                                getLocProv().getString(LocalizationKeys.SELECTED_FUNCTIONS_HAVE_TOO_MANY_INPUTS_KEY),
                                Constants.NUM_FUNCTION_INPUTS_LIMIT.getUpperLimit()));
                        truthTableModel.loadDefaultItems();
                        inputTableModel.loadDefaultItems();
//                        itemTable.clearSelection();
                    }
                }

                showExpressions();
            }
        });

        addResizeAndLocalizationListener(inputTable, inputTableModel);
    }

    @Override
    protected void setupGUI() {
        initTable();
        initFuncList();
    }

    private void initTable() {
        JPanelPair panelPair = GUIUtility.generatePanelPair(mainPanel, 0, 0.4);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        expressionTextArea = new LJTextArea(LocalizationKeys.INSERT_EXPRESSION_KEY, getLocProv());
        expressionTextArea.setRows(GUIConstants.EXPRESSION_TEXT_AREA_ROWS);
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JScrollPane(expressionTextArea)));

        JPanel inputsAndButtonsPanel = GUIUtility.getPanel(new GridBagLayout());
        upperPanel.add(inputsAndButtonsPanel);

        JPanel inputsPanel = GUIUtility.getPanel();
        GridBagConstraints gbc = GUIUtility.getGBC(0, 0, 0.3, 1.0);
        inputsAndButtonsPanel.add(inputsPanel, gbc);
        inputsPanel.add(new LJLabel(LocalizationKeys.INPUTS_KEY, getLocProv(), SwingConstants.CENTER), BorderLayout.NORTH);
        inputTable.setPreferredScrollableViewportSize(new Dimension(inputTable.getPreferredSize().width, inputTable.getRowHeight() * 6));
        inputsPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);
        inputTable.applyMinSizeInScrollPane();

        JPanel buttonsPanel = GUIUtility.getPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));
        gbc = GUIUtility.getGBC(1, 0, 0.7, 1.0);
        inputsAndButtonsPanel.add(buttonsPanel, gbc);
        buttonsPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getGenerateFromExpressionAction())));
        buttonsPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getGenerateFromTextAction())));
        buttonsPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getLoadTextAction())));
        buttonsPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getSaveTextAction())));

        upperPanel.add(new LJLabel(LocalizationKeys.GENERATING_EXPRESSIONS_KEY, getLocProv(), SwingConstants.CENTER));
        showExpressionTextArea = new JTextArea(getLocProv().getString(LocalizationKeys.NO_SELECTED_FUNCTIONS_KEY));
        showExpressionTextArea.setEditable(false);
        showExpressionTextArea.setRows(GUIConstants. SHOW_EXPRESSION_TEXT_AREA_ROWS);
        getLocProv().addLocalizationListener(this::showExpressions);
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JScrollPane(showExpressionTextArea)));

        lowerPanel.add(new LJLabel(LocalizationKeys.TRUTH_TABLE_KEY, getLocProv(), SwingConstants.CENTER), BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(truthTable), BorderLayout.CENTER);
        truthTable.applyMinSizeInScrollPane();
    }

    private void initFuncList() {
        JPanelPair panelPair = GUIUtility.generatePanelPair(mainPanel, 1, 0.2);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getGenerateRandomFunctionAction())));

        numInputsComboBox = GUIUtility.getComboBoxFromLimit(Constants.NUM_FUNCTION_INPUTS_LIMIT);
        numInputsComboBox.setSelectedIndex(1);
        upperPanel.add(GUIUtility.getComboBoxPanel(numInputsComboBox, getLocProv(), LocalizationKeys.INPUTS_KEY));

        if (GUIConstants.SHOW_DUPLICATE_BUTTONS) {
            upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getDuplicateSelectedFunctionAction())));
        }
        if (GUIConstants.SHOW_REMOVE_BUTTONS) {
            upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getRemoveSelectedFunctionAction())));
        }
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getDisplayAllFunctionsAction())));

        lowerPanel.add(new LJLabel(LocalizationKeys.BOOLEAN_FUNCTIONS_KEY, getLocProv(), SwingConstants.CENTER), BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        itemTable.applyMinSizeInScrollPane();
    }

    @Override
    public void setItems(List<BooleanFunction> items, boolean areItemsEditable) {
        super.setItems(items, areItemsEditable);
        inputTableModel.loadDefaultItems();
        truthTableModel.loadDefaultItems();
    }

    @Override
    public void updateActionsAreEnabled() {
        getJfpga().getGenerateFromExpressionAction().setEnabled(areItemsEditable());
        getJfpga().getGenerateFromTextAction().setEnabled(areItemsEditable());
        getJfpga().getGenerateRandomFunctionAction().setEnabled(areItemsEditable());
        getJfpga().getDuplicateSelectedFunctionAction().setEnabled(areItemsEditable());
        getJfpga().getRemoveSelectedFunctionAction().setEnabled(areItemsEditable());
        getJfpga().getUndoRemoveFunctionAction().setEnabled(getRemoveCount() > 0);
    }

    @Override
    public void changeItemName(int index, String newName) {
        super.changeItemName(index, newName);
        truthTableModel.setData(getItem(index));
        truthTableModel.fireTableStructureChanged();
        showExpressions();
    }

    public void changeFunctionInput(int inputIndex, String newInputID) {
        Utility.checkIfValidString(newInputID, "new boolean function's input");

        if (BoolOperatorFactory.getGenericFactory().isMappingPresent(newInputID)) {
            getJfpga().showErrorMsg(String.format(getLocProv().getString(LocalizationKeys.S_INVALID_INPUT_IS_OPERATOR_KEY), newInputID));
            return;
        }

        int[] selectedIndices = getIndicesSelectedItems();

        if (selectedIndices.length == 0) {
            return;
        } else if (selectedIndices.length == 1) {
            BooleanFunction func = getSelectedItem();
            Utility.checkRange(inputIndex, 0, func.getNumInputs() - 1);
            List<String> oldInputs = new ArrayList<>(func.getInputIDs());
            func.getInputIDs().set(inputIndex, newInputID);
            func.updateInputsInExpression(oldInputs.get(inputIndex), newInputID);

            if (booleanFunctionListeners != null) {
                booleanFunctionListeners.forEach(l -> l.booleanFunctionInputsEdited(func, getIndexSelectedItem(), oldInputs));
            }
        } else {
            String oldInputID = truthTableModel.getInputIDs().get(inputIndex);

            for (int selectedIndex : selectedIndices) {
                BooleanFunction selectedFunction = getItem(selectedIndices[selectedIndex]);
                List<String> inputIDs = selectedFunction.getInputIDs();
                int index = inputIDs.indexOf(oldInputID);

                if (index != -1) {
                    List<String> oldInputs = new ArrayList<>(selectedFunction.getInputIDs());
                    inputIDs.set(index, newInputID);
                    selectedFunction.updateInputsInExpression(oldInputs.get(index), newInputID);

                    if (booleanFunctionListeners != null) {
                        booleanFunctionListeners.forEach(l -> l.booleanFunctionInputsEdited(selectedFunction, selectedIndex, oldInputs));
                    }
                }
            }

            BooleanVector selectionVector = new BooleanVector(getSelectedItems());
            truthTableModel.setData(selectionVector);
            inputTableModel.setItems(selectionVector.getSortedInputIDs());
        }

        inputTableModel.fireTableDataChanged();
        truthTableModel.fireTableStructureChanged();
        showExpressions();
        parentSession.setEdited(true);
    }

    public void tableDataChanged(int indexFuncInSelected, int index, BitSet oldTable) {
        BooleanFunction func = getItem(getIndicesSelectedItems()[indexFuncInSelected]);
        func.setExpressionGeneratedFrom(null);

        if (booleanFunctionListeners != null) {
            if (getIndexSelectedItem() < 0) {
                return;
            }
            booleanFunctionListeners.forEach(l -> l.booleanFunctionTableEdited(func, index, oldTable));
        }
    }

    private void showExpressions() {
        List<BooleanFunction> functions = getSelectedItems();
        if (functions == null || functions.isEmpty()) {
            showExpressionTextArea.setText(getLocProv().getString(LocalizationKeys.NO_SELECTED_FUNCTIONS_KEY));
        } else {
            StringBuilder sb = new StringBuilder();
            for (BooleanFunction function : functions) {
                sb.append(function.getName()).append(" = ");
                String expression = function.getExpressionGeneratedFrom();
                if (expression == null) {
                    sb.append(getLocProv().getString(LocalizationKeys.NO_EXPRESSION_KEY));
                } else {
                    sb.append(expression);
                }
                sb.append('\n');
            }
            sb.setLength(sb.length() - 1);
            showExpressionTextArea.setText(sb.toString());
        }
    }

    public JTextArea getExpressionTextArea() {
        return expressionTextArea;
    }

    public JTextArea getShowExpressionTextArea() {
        return showExpressionTextArea;
    }

    public JComboBox<Integer> getNumInputsComboBox() {
        return numInputsComboBox;
    }

    @Override
    protected Collection<? extends TableItemListener<BooleanFunction>> getTableItemListeners() {
        return booleanFunctionListeners;
    }

    public void addBooleanFunctionListener(BooleanFunctionListener listener) {
        if (booleanFunctionListeners == null) {
            booleanFunctionListeners = new HashSet<>();
        }
        booleanFunctionListeners.add(listener);
    }

    public void removeBooleanFunctionListener(BooleanFunctionListener listener) {
        if (booleanFunctionListeners != null) {
            booleanFunctionListeners.remove(listener);
        }
    }
}
