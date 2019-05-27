package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JPanelPair;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func.BooleanExpressionProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func.EditFuncInputListAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func.EditFuncNameListAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func.GenerateFromExpressionAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.ListAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.*;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BooleanFunctionController implements BooleanExpressionProvider {

    private JFPGA jfpga;
    private SessionController parentSession;
    private List<BooleanFunction> booleanFunctions;
    private JPanel mainPanel;

    private DefaultListModel<String> funcInputsListModel;
    private JList<String> funcInputsJList;

    private TruthTableModel truthTableModel;
    private JTable truthTable;

    private JTextArea expressionTextArea;
    private JButton expressionButton;
    private JButton loadExpressionButton;

    private DefaultListModel<BooleanFunction> boolFuncListModel;
    private JList<BooleanFunction> boolFuncJlist;

    private Action generateFromExpressionAction;
    private Action loadExpressionAction;

    private Set<BooleanFunctionListener> booleanFunctionListeners;

    public BooleanFunctionController(List<BooleanFunction> booleanFunctions, JFPGA jfpga, SessionController parentSession) {
        this.booleanFunctions = Utility.checkNull(booleanFunctions, "list of boolean functions");
        this.jfpga = Utility.checkNull(jfpga, "JFPGA");
        this.parentSession = Utility.checkNull(parentSession, "parent session");

        loadData(booleanFunctions);
        initActions();
        initGUI();
    }

    private void loadData(List<BooleanFunction> booleanFunctions) {
        boolFuncListModel = new DefaultListModel<>();
        booleanFunctions.forEach(f -> boolFuncListModel.addElement(f));
        boolFuncJlist = new JList<>(boolFuncListModel);
        boolFuncJlist.setCellRenderer(new FuncListCellRenderer());
        new ListAction(boolFuncJlist, new EditFuncNameListAction(this));

        funcInputsListModel = new DefaultListModel<>();
        funcInputsJList = new JList<>(funcInputsListModel);
        new ListAction(funcInputsJList, new EditFuncInputListAction(this));
        boolFuncJlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                funcInputsListModel.clear();
                BooleanFunction selectedFunction = booleanFunctions.get(getIndexSelectedFunction());
                funcInputsListModel.addAll(selectedFunction.getInputIDs());

                truthTableModel.setData(selectedFunction.getInputIDs(), selectedFunction.getName(), selectedFunction.getTruthTable());
            }
        });

        truthTableModel = new TruthTableModel();
        truthTable = new JTable(truthTableModel);
    }

    private void initActions() {
        generateFromExpressionAction = new GenerateFromExpressionAction(jfpga, this);
    }

    private void initGUI() {
        mainPanel = GUIUtility.getPanel();
//        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        JPanel maxSizeMainPanel = GUIUtility.getPanel(new GridBagLayout());
        mainPanel.add(maxSizeMainPanel, BorderLayout.CENTER);
        JPanel temp = mainPanel;
        mainPanel = maxSizeMainPanel;

        initInputList();
        initTable();
        initFuncList();

        mainPanel = temp;
    }

    private JPanelPair generatePanelPair(int indexX, double weightX) {
        JPanel parentPanel = GUIUtility.getPanel();
        GridBagConstraints gbc = GUIUtility.getGBC(indexX, 0, weightX, 1.0, 1, 1);
        mainPanel.add(parentPanel, gbc);

        JPanel upperPanel = GUIUtility.getPanelWithBorder();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
        parentPanel.add(upperPanel, BorderLayout.NORTH);
        JPanel lowerPanel = GUIUtility.getPanelWithBorder();
        parentPanel.add(lowerPanel, BorderLayout.CENTER);

        return new JPanelPair(upperPanel, lowerPanel);
    }

    private void initInputList() {
        JPanelPair panelPair = generatePanelPair(0, 0.1);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        lowerPanel.add(new LJLabel(LocalizationKeys.INPUTS_KEY, jfpga.getFlp(), SwingConstants.CENTER), BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(funcInputsJList), BorderLayout.CENTER);
    }

    private void initTable() {
        JPanelPair panelPair = generatePanelPair(1, 0.4);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        lowerPanel.add(new LJLabel(LocalizationKeys.TRUTH_TABLE_KEY, jfpga.getFlp(), SwingConstants.CENTER), BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(truthTable), BorderLayout.CENTER);
    }

    private void initFuncList() {
        JPanelPair panelPair = generatePanelPair(2, 0.2);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        expressionTextArea = new LJTextArea(LocalizationKeys.INSERT_EXPRESSION_KEY, jfpga.getFlp());
        expressionTextArea.setRows(GUIConstants.EXPRESSION_TEXT_AREA_ROWS);
        expressionTextArea.setLineWrap(true);
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(expressionTextArea));

        expressionButton = new LJButton(LocalizationKeys.GENERATE_FROM_EXPRESSION_KEY, jfpga.getFlp());
        expressionButton.addActionListener(generateFromExpressionAction);
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(expressionButton));

        loadExpressionButton = new LJButton(LocalizationKeys.LOAD_EXPRESSION_KEY, jfpga.getFlp());

        JPanel listDescPanel = GUIUtility.getPanel(new GridLayout(0, 1));
        lowerPanel.add(listDescPanel, BorderLayout.NORTH);
        listDescPanel.add(new LJLabel(LocalizationKeys.BOOLEAN_FUNCTIONS_KEY, jfpga.getFlp(), SwingConstants.CENTER));

        JPanel descPanel = GUIUtility.getPanel(new GridLayout(1, 0));
        listDescPanel.add(descPanel);
        descPanel.add(new LJLabel(LocalizationKeys.INDEX_KEY, jfpga.getFlp(), SwingConstants.LEFT));
        descPanel.add(new LJLabel(LocalizationKeys.NAME_KEY, jfpga.getFlp(), SwingConstants.CENTER));
        descPanel.add(new LJLabel(LocalizationKeys.INPUTS_KEY, jfpga.getFlp(), SwingConstants.RIGHT));

        lowerPanel.add(new JScrollPane(boolFuncJlist), BorderLayout.CENTER);
    }

    public int getNumBooleanFunctions() {
        return booleanFunctions.size();
    }

    public int getIndexSelectedFunction() {
        return boolFuncJlist.getSelectedIndex();
    }

    public void addBooleanFunction(BooleanFunction newBooleanFunction, int index) {
        Utility.checkNull(newBooleanFunction, "new boolean function");
        Utility.checkRange(index, 0, booleanFunctions.size());
        booleanFunctions.add(index, newBooleanFunction);
        boolFuncListModel.add(index, newBooleanFunction);

        if (booleanFunctionListeners != null) {
            booleanFunctionListeners.forEach(l -> l.booleanFunctionAdded(newBooleanFunction, index));
        }
        parentSession.setEdited(true);
    }

    public void addBooleanFunction(BooleanFunction newBooleanFunction) {
        addBooleanFunction(newBooleanFunction, booleanFunctions.size());
    }

    public void removeBooleanFunction(int index) {
        Utility.checkRange(index, 0, booleanFunctions.size() - 1);
        BooleanFunction removed = booleanFunctions.remove(index);
        if (booleanFunctionListeners != null) {
            booleanFunctionListeners.forEach(l -> l.booleanFunctionRemoved(removed, index));
        }
        parentSession.setEdited(true);
    }

    public void changeFunctionName(int index, String newName) {
        Utility.checkRange(index, 0, booleanFunctions.size() - 1);
        Utility.checkIfValidString(newName, "new boolean function's name");
        BooleanFunction booleanFunction = booleanFunctions.get(index);
        String oldName = booleanFunction.getName();
        booleanFunction.setName(newName);
        if (booleanFunctionListeners != null) {
            booleanFunctionListeners.forEach(l -> l.booleanFunctionNameEdited(booleanFunction, index, oldName));
        }
        parentSession.setEdited(true);
    }

    public void changeFunctionInput(int funcIndex, String newInputID, int inputIndex) {
        Utility.checkRange(funcIndex, 0, booleanFunctions.size() - 1);
        Utility.checkIfValidString(newInputID, "new boolean function's input");
        BooleanFunction func = booleanFunctions.get(funcIndex);
        Utility.checkRange(inputIndex, 0, func.getNumInputs() - 1);
        List<String> oldInputs = new ArrayList<>(func.getInputIDs());
        func.getInputIDs().set(inputIndex, newInputID);

        if (booleanFunctionListeners != null) {
            booleanFunctionListeners.forEach(l -> l.booleanFunctionInputsEdited(func, funcIndex, oldInputs));
        }
        parentSession.setEdited(true);
    }

    @Override
    public String getExpressionString() {
        return expressionTextArea.getText();
    }

    public JPanel getMainPanel() {
        return mainPanel;
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
