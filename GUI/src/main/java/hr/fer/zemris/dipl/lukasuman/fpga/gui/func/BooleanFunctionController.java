package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JPanelPair;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.*;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

        loadData();
        initGUI();
    }

    private void loadData() {
//        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
//        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        itemTableModel = new FuncTableModel(parentSession, listOfItems);
        itemTable = new MyJTable(itemTableModel);
        itemTable.setDefaultRenderer(Integer.class, centerRenderer);
        itemTable.setRowSelectionAllowed(true);
        itemTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        inputTableModel = new InputTableModel(parentSession);
        inputTable = new MyJTable(inputTableModel);
        inputTable.setDefaultRenderer(Integer.class, centerRenderer);

        truthTableModel = new TruthTableModel(parentSession);
        truthTable = new MyJTable(truthTableModel);
        truthTable.setDefaultRenderer(Integer.class, centerRenderer);

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

        getJfpga().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                GUIUtility.resizeColumns(inputTable, inputTableModel);
                GUIUtility.resizeColumns(itemTable, itemTableModel);
            }
        });
    }

    private void initGUI() {
        mainPanel = GUIUtility.getPanel();
        JPanel maxSizeMainPanel = GUIUtility.getPanel(new GridBagLayout());
        mainPanel.add(maxSizeMainPanel, BorderLayout.CENTER);
        JPanel temp = mainPanel;
        mainPanel = maxSizeMainPanel;

        initTable();
        initFuncList();

        mainPanel = temp;
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
        GridBagConstraints gbc = GUIUtility.getGBC(0, 0, 0.3, 1.0, 1, 1);
        inputsAndButtonsPanel.add(inputsPanel, gbc);
        inputsPanel.add(new LJLabel(LocalizationKeys.INPUTS_KEY, getLocProv(), SwingConstants.CENTER), BorderLayout.NORTH);
        inputTable.setPreferredScrollableViewportSize(new Dimension(inputTable.getPreferredSize().width, inputTable.getRowHeight() * 6));
        inputsPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);
        inputTable.applyMinSizeInScrollPane();

        JPanel buttonsPanel = GUIUtility.getPanel(new GridLayout(0, 1));
        gbc = GUIUtility.getGBC(1, 0, 0.7, 1.0, 1, 1);
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

        JPanel generateRandomPanel = GUIUtility.getPanel(new GridBagLayout());
        upperPanel.add(generateRandomPanel);

        JPanel genRandBtnPanel = GUIUtility.getPanel();
        genRandBtnPanel.setBorder(new EmptyBorder(0, 0, 0, GUIConstants.DEFAULT_BORDER_SIZE));
        genRandBtnPanel.add(new JButton(getJfpga().getGenerateRandomFunctionAction()));
        GridBagConstraints gbc = GUIUtility.getGBC(0, 0, 0.8, 1.0, 1, 1);
        generateRandomPanel.add(genRandBtnPanel, gbc);

        numInputsComboBox = new JComboBox<>(Utility.generateRangeArray(
                Constants.NUM_FUNCTION_INPUTS_LIMIT.getLowerLimit(),
                Constants.NUM_FUNCTION_INPUTS_LIMIT.getUpperLimit() + 1));
        JPanel genRandComboPanel = GUIUtility.getPanel();
        genRandComboPanel.setBorder(new EmptyBorder(0, GUIConstants.DEFAULT_BORDER_SIZE, 0, 0));
        genRandComboPanel.add(numInputsComboBox);
        gbc = GUIUtility.getGBC(1, 0, 0.2, 1.0, 1, 1);
        generateRandomPanel.add(genRandComboPanel, gbc);

        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getDuplicateSelectedFunctionAction())));
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getRemoveSelectedFunctionAction())));

        JPanel listDescPanel = GUIUtility.getPanel(new GridLayout(0, 1));
        lowerPanel.add(listDescPanel, BorderLayout.NORTH);
        listDescPanel.add(new LJLabel(LocalizationKeys.BOOLEAN_FUNCTIONS_KEY, getLocProv(), SwingConstants.CENTER));

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

        List<BooleanFunction> selectedFunctions = getSelectedItems();

        if (selectedFunctions.size() == 1) {
            BooleanFunction func = selectedFunctions.get(0);
            Utility.checkRange(inputIndex, 0, func.getNumInputs() - 1);
            List<String> oldInputs = new ArrayList<>(func.getInputIDs());
            func.getInputIDs().set(inputIndex, newInputID);

            if (booleanFunctionListeners != null) {
                booleanFunctionListeners.forEach(l -> l.booleanFunctionInputsEdited(func, getIndexSelectedItem(), oldInputs));
            }
        } else {
            String oldInputID = truthTableModel.getInputIDs().get(inputIndex);

            for (BooleanFunction selectedFunction : selectedFunctions) {
                List<String> inputIDs = selectedFunction.getInputIDs();
                int index = inputIDs.indexOf(oldInputID);

                if (index != -1) {
                    inputIDs.set(index, newInputID);
                }
            }

            BooleanVector selectionVector = new BooleanVector(selectedFunctions);
            truthTableModel.setData(selectionVector);
            inputTableModel.setItems(selectionVector.getSortedInputIDs());
        }

        inputTableModel.fireTableDataChanged();
        truthTableModel.fireTableStructureChanged();
        parentSession.setEdited(true);
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
