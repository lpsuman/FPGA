package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JPanelPair;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.LoadTextAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func.*;
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

public class BooleanFunctionController extends AbstractGUIController implements TextProvider {

    private List<BooleanFunction> booleanFunctions;
    private Set<BooleanFunctionListener> booleanFunctionListeners;

    private InputTableModel inputTableModel;
    private MyJTable inputTable;

    private TruthTableModel truthTableModel;
    private MyJTable truthTable;

    private FuncTableModel funcTableModel;
    private MyJTable funcTable;

    private JTextArea expressionTextArea;
    private JComboBox<Integer> numInputsComboBox;

    private Action generateFromExpressionAction;
    private Action generateFromTextAction;
    private LoadTextAction loadTextAction;
    private LoadTextAction loadExpressionAction;
    private Action generateRandomFunctionAction;
    private Action duplicateSelectedFunctionAction;

    public BooleanFunctionController(List<BooleanFunction> booleanFunctions, JFPGA jfpga, SessionController parentSession) {
        super(jfpga, parentSession);
        this.booleanFunctions = Utility.checkNull(booleanFunctions, "list of boolean functions");

        loadData();
        initActions();
        initGUI();
    }

    private void loadData() {
//        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
//        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        funcTableModel = new FuncTableModel(booleanFunctions, this, jfpga.getFlp());
        funcTable = new MyJTable(funcTableModel);
        funcTable.setDefaultRenderer(Integer.class, centerRenderer);
        funcTable.setRowSelectionAllowed(true);
        funcTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        inputTableModel = new InputTableModel(this, jfpga.getFlp());
        inputTable = new MyJTable(inputTableModel);
        inputTable.setDefaultRenderer(Integer.class, centerRenderer);

        truthTableModel = new TruthTableModel(this, jfpga.getFlp());
        truthTable = new MyJTable(truthTableModel);
        truthTable.setDefaultRenderer(Integer.class, centerRenderer);

        funcTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] selectedIndices = funcTable.getSelectedRows();
                List<BooleanFunction> selectedFunctions = new ArrayList<>(selectedIndices.length);

                for (int selectedIndex : selectedIndices) {
                    selectedFunctions.add(funcTableModel.getBooleanFunctions().get(selectedIndex));
                }

                if (selectedFunctions.size() == 1) {
                    BooleanFunction selectedFunction = selectedFunctions.get(0);
                    truthTableModel.setData(selectedFunction);
                    inputTableModel.setInputIDs(selectedFunction.getInputIDs());
                } else if (selectedFunctions.size() > 1) {
                    BooleanVector selectionVector = new BooleanVector(selectedFunctions);
                    truthTableModel.setData(selectionVector);
                    inputTableModel.setInputIDs(selectionVector.getSortedInputIDs());
                }
            }
        });

        jfpga.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                GUIUtility.resizeColumns(inputTable, inputTableModel);
                GUIUtility.resizeColumns(funcTable, funcTableModel);
            }
        });
    }

    private void initActions() {
        generateFromExpressionAction = new GenerateFromExpressionAction(jfpga, this);
        generateFromTextAction = new GenerateFromTextAction(jfpga, this,
                () -> expressionTextArea.getText());
        loadTextAction = new LoadTextAction(jfpga, LocalizationKeys.LOAD_TEXT_KEY);
        loadExpressionAction = new LoadTextAction(jfpga, LocalizationKeys.LOAD_EXPRESSION_KEY);
        generateRandomFunctionAction = new GenerateRandomFunctionAction(jfpga, this,
                () -> numInputsComboBox.getItemAt(numInputsComboBox.getSelectedIndex()));
        duplicateSelectedFunctionAction = new DuplicateFunctionAction(jfpga, this);
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

        expressionTextArea = new LJTextArea(LocalizationKeys.INSERT_EXPRESSION_KEY, jfpga.getFlp());
        expressionTextArea.setRows(GUIConstants.EXPRESSION_TEXT_AREA_ROWS);
        loadTextAction.addTextLoadListener(lines -> expressionTextArea.setText(String.join("\n", lines)));
        loadExpressionAction.addTextLoadListener(lines -> expressionTextArea.setText(String.join("\n", lines)));
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JScrollPane(expressionTextArea)));

        JPanel inputsAndButtonsPanel = GUIUtility.getPanel(new GridBagLayout());
        upperPanel.add(inputsAndButtonsPanel);

        JPanel inputsPanel = GUIUtility.getPanel();
//        inputsPanel.setPreferredSize(new Dimension(0, 0));
        GridBagConstraints gbc = GUIUtility.getGBC(0, 0, 0.3, 1.0, 1, 1);
        inputsAndButtonsPanel.add(inputsPanel, gbc);
        inputsPanel.add(new LJLabel(LocalizationKeys.INPUTS_KEY, jfpga.getFlp(), SwingConstants.CENTER), BorderLayout.NORTH);
        inputTable.setPreferredScrollableViewportSize(new Dimension(inputTable.getPreferredSize().width, inputTable.getRowHeight() * 6));
        inputsPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);
        inputTable.applyMinSizeInScrollPane();

        JPanel buttonsPanel = GUIUtility.getPanel(new GridLayout(0, 1));
//        buttonsPanel.setPreferredSize(new Dimension(0, 0));
        gbc = GUIUtility.getGBC(1, 0, 0.7, 1.0, 1, 1);
        inputsAndButtonsPanel.add(buttonsPanel, gbc);
        buttonsPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(generateFromExpressionAction)));
        buttonsPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(generateFromTextAction)));
        buttonsPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(loadTextAction)));

        lowerPanel.add(new LJLabel(LocalizationKeys.TRUTH_TABLE_KEY, jfpga.getFlp(), SwingConstants.CENTER), BorderLayout.NORTH);
//        JScrollPane scrollPane = new JScrollPane(truthTable);
//        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        lowerPanel.add(scrollPane, BorderLayout.CENTER);
        lowerPanel.add(new JScrollPane(truthTable), BorderLayout.CENTER);
        truthTable.applyMinSizeInScrollPane();
    }

    private void initFuncList() {
        JPanelPair panelPair = GUIUtility.generatePanelPair(mainPanel, 2, 0.2);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        JPanel generateRandomPanel = GUIUtility.getPanel(new GridBagLayout());
        upperPanel.add(generateRandomPanel);
//        upperPanel.add(GUIUtility.putIntoPanelWithBorder(generateRandomPanel));

        JPanel genRandBtnPanel = GUIUtility.getPanel();
        genRandBtnPanel.setBorder(new EmptyBorder(0, 0, 0, GUIConstants.DEFAULT_BORDER_SIZE));
        genRandBtnPanel.add(new JButton(generateRandomFunctionAction));
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

        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(duplicateSelectedFunctionAction)));

        JPanel listDescPanel = GUIUtility.getPanel(new GridLayout(0, 1));
        lowerPanel.add(listDescPanel, BorderLayout.NORTH);
        listDescPanel.add(new LJLabel(LocalizationKeys.BOOLEAN_FUNCTIONS_KEY, jfpga.getFlp(), SwingConstants.CENTER));

        lowerPanel.add(new JScrollPane(funcTable), BorderLayout.CENTER);
        funcTable.applyMinSizeInScrollPane();
    }

    public List<BooleanFunction> getBooleanFunctions() {
        return funcTableModel.getBooleanFunctions();
    }

    public int getNumBooleanFunctions() {
        return getBooleanFunctions().size();
    }

    public int getIndexSelectedFunction() {
        return funcTable.getSelectedRow();
    }

    public int[] getIndicesSelectedFunctions() {
        return funcTable.getSelectedRows();
    }

    public BooleanFunction getSelectedFunction() {
        return getBooleanFunction(getIndexSelectedFunction());
    }

    public List<BooleanFunction> getSelectedFunctions() {
        List<BooleanFunction> selectedFunctions = new ArrayList<>();

        for (int selectedIndex : getIndicesSelectedFunctions()) {
            selectedFunctions.add(getBooleanFunction(selectedIndex));
        }

        return selectedFunctions;
    }

    public BooleanFunction getBooleanFunction(int index) {
        Utility.checkRange(index, 0, getNumBooleanFunctions());
        return getBooleanFunctions().get(index);
    }

    public void addBooleanFunction(BooleanFunction newBooleanFunction, int index) {
        Utility.checkNull(newBooleanFunction, "new boolean function");
        Utility.checkRange(index, 0, booleanFunctions.size());
        booleanFunctions.add(index, newBooleanFunction);
        funcTableModel.fireTableRowsInserted(index, index);

        if (booleanFunctionListeners != null) {
            booleanFunctionListeners.forEach(l -> l.booleanFunctionAdded(newBooleanFunction, index));
        }
        parentSession.setEdited(true);
    }

    public void addBooleanFunction(BooleanFunction newBooleanFunction) {
        addBooleanFunction(newBooleanFunction, booleanFunctions.size());
    }

    public void removeBooleanFunction(int index) {
        Utility.checkRange(index, 0, getNumBooleanFunctions() - 1);
        BooleanFunction removed = getBooleanFunctions().remove(index);
        funcTableModel.fireTableRowsDeleted(index, index);

        if (booleanFunctionListeners != null) {
            booleanFunctionListeners.forEach(l -> l.booleanFunctionRemoved(removed, index));
        }
        parentSession.setEdited(true);
    }

    public void changeFunctionName(int index, String newName) {
        Utility.checkRange(index, 0, getNumBooleanFunctions() - 1);
        Utility.checkIfValidString(newName, "new boolean function's name");
        BooleanFunction booleanFunction = getBooleanFunction(index);
        String oldName = booleanFunction.getName();
        booleanFunction.setName(newName);
        funcTableModel.fireTableRowsUpdated(index, index);
        truthTableModel.setData(booleanFunction);
        truthTableModel.fireTableStructureChanged();

        if (booleanFunctionListeners != null) {
            booleanFunctionListeners.forEach(l -> l.booleanFunctionRenamed(booleanFunction, index, oldName));
        }

        parentSession.setEdited(true);
    }

    public void changeFunctionInput(int inputIndex, String newInputID) {
        Utility.checkIfValidString(newInputID, "new boolean function's input");

        List<BooleanFunction> selectedFunctions = getSelectedFunctions();

        if (selectedFunctions.size() == 1) {
            BooleanFunction func = selectedFunctions.get(0);
            Utility.checkRange(inputIndex, 0, func.getNumInputs() - 1);
            List<String> oldInputs = new ArrayList<>(func.getInputIDs());
            func.getInputIDs().set(inputIndex, newInputID);

            if (booleanFunctionListeners != null) {
                booleanFunctionListeners.forEach(l -> l.booleanFunctionInputsEdited(func, getIndexSelectedFunction(), oldInputs));
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
            inputTableModel.setInputIDs(selectionVector.getSortedInputIDs());
        }

        inputTableModel.fireTableDataChanged();
        truthTableModel.fireTableStructureChanged();
        parentSession.setEdited(true);
    }

    @Override
    public String getText() {
        return expressionTextArea.getText();
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
