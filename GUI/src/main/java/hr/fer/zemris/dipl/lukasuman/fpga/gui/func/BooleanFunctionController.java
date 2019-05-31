package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
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
    private JComboBox<Integer> numInputsComboBox;

    public BooleanFunctionController(SessionController parentSession) {
        super(parentSession, parentSession.getSessionData().getBoolFunctions());
    }

    @Override
    protected void loadData() {
        itemTableModel = new FuncTableModel(parentSession, getItems());
        itemTable = new MyJTable(itemTableModel);
        itemTable.setRowSelectionAllowed(true);
        itemTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        inputTableModel = new InputTableModel(parentSession);
        inputTable = new MyJTable(inputTableModel);

        truthTableModel = new TruthTableModel(parentSession);
        truthTable = new MyJTable(truthTableModel);

        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] selectedIndices = itemTable.getSelectedRows();
                List<BooleanFunction> selectedFunctions = new ArrayList<>(selectedIndices.length);

                for (int selectedIndex : selectedIndices) {
                    selectedFunctions.add(itemTableModel.getItems().get(selectedIndex));
                }

                if (selectedFunctions.size() == 1) {
                    BooleanFunction selectedFunction = selectedFunctions.get(0);
                    truthTableModel.setData(selectedFunction);
                    inputTableModel.setItems(selectedFunction.getInputIDs());
                } else if (selectedFunctions.size() > 1) {
                    BooleanVector selectionVector = new BooleanVector(selectedFunctions);
                    truthTableModel.setData(selectionVector);
                    inputTableModel.setItems(selectionVector.getSortedInputIDs());
                }
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
        JPanelPair panelPair = GUIUtility.generatePanelPair(mainPanel, 1, 0.4);
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

        JPanel buttonsPanel = GUIUtility.getPanel(new GridLayout(0, 1));
        gbc = GUIUtility.getGBC(1, 0, 0.7, 1.0);
        inputsAndButtonsPanel.add(buttonsPanel, gbc);
        buttonsPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getGenerateFromExpressionAction())));
        buttonsPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getGenerateFromTextAction())));
        buttonsPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getLoadTextAction())));

        lowerPanel.add(new LJLabel(LocalizationKeys.TRUTH_TABLE_KEY, getLocProv(), SwingConstants.CENTER), BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(truthTable), BorderLayout.CENTER);
        truthTable.applyMinSizeInScrollPane();
    }

    private void initFuncList() {
        JPanelPair panelPair = GUIUtility.generatePanelPair(mainPanel, 2, 0.2);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getGenerateRandomFunctionAction())));

        numInputsComboBox = GUIUtility.getComboBoxFromLimit(Constants.NUM_FUNCTION_INPUTS_LIMIT);
        numInputsComboBox.setSelectedIndex(1);
        upperPanel.add(GUIUtility.getComboBoxPanel(numInputsComboBox, getLocProv(), LocalizationKeys.INPUTS_KEY));

        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getDuplicateSelectedFunctionAction())));
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getRemoveSelectedFunctionAction())));
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getDisplayAllFunctionsAction())));

        lowerPanel.add(new LJLabel(LocalizationKeys.BOOLEAN_FUNCTIONS_KEY, getLocProv(), SwingConstants.CENTER), BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        itemTable.applyMinSizeInScrollPane();
    }

    @Override
    public void changeItemName(int index, String newName) {
        super.changeItemName(index, newName);
        truthTableModel.setData(getItem(index));
        truthTableModel.fireTableStructureChanged();
    }

    public void changeFunctionInput(int inputIndex, String newInputID) {
        Utility.checkIfValidString(newInputID, "new boolean function's input");

        int[] selectedIndices = getIndicesSelectedItems();

        if (selectedIndices.length == 0) {
            return;
        } else if (selectedIndices.length == 1) {
            BooleanFunction func = getSelectedItem();
            Utility.checkRange(inputIndex, 0, func.getNumInputs() - 1);
            List<String> oldInputs = new ArrayList<>(func.getInputIDs());
            func.getInputIDs().set(inputIndex, newInputID);

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
        parentSession.setEdited(true);
    }

    public void tableDataChanged(int index, BitSet oldTable) {
        if (booleanFunctionListeners != null) {
            if (getIndexSelectedItem() < 0) {
                return;
            }
            booleanFunctionListeners.forEach(l -> l.booleanFunctionTableEdited(getSelectedItem(), index, oldTable));
        }
    }

    public JTextArea getExpressionTextArea() {
        return expressionTextArea;
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
