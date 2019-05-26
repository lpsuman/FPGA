package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BooleanFunctionController implements BooleanExpressionProvider {

    private JFPGA jfpga;
    private SessionController parentSession;
    private List<BooleanFunction> booleanFunctions;
    private JPanel mainPanel;

    private DefaultListModel<BooleanFunction> boolFuncListModel;
    private JList<BooleanFunction> boolFuncJlist;
    private JTextArea expressionTextArea;
    private JButton expressionButton;

    private DefaultListModel<String> funcInputsListModel;
    private JList<String> funcInputsJList;

    private Action generateFromExpressionAction;

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
            }
        });
    }

    private void initActions() {
        generateFromExpressionAction = new GenerateFromExpressionAction(jfpga, this);
    }

    private void initGUI() {
        mainPanel = new JPanel(new GridBagLayout());
        initInputList();
        initTable();
        initFuncList();
    }

    private void initInputList() {
        JPanel upperPanel = new JPanel();
        mainPanel.add(upperPanel, getGBC(0, 0, GUIConstants.INPUTS_COLUMN_WEIGHT, GUIConstants.UPPER_WEIGHT, 1, 1));

        JPanel listPanel = new JPanel(new BorderLayout());
        mainPanel.add(listPanel, getGBC(0, 1, GUIConstants.INPUTS_COLUMN_WEIGHT, GUIConstants.LOWER_WEIGHT, 1, GridBagConstraints.REMAINDER));
        listPanel.add(new LJLabel(LocalizationKeys.INPUTS_KEY, jfpga.getFlp()), BorderLayout.NORTH);
        listPanel.add(new JScrollPane(funcInputsJList), BorderLayout.CENTER);
    }

    private void initTable() {

    }

    private void initFuncList() {
        JPanel upperPanel = new JPanel(new BorderLayout());
        mainPanel.add(upperPanel, getGBC(2, 0, GUIConstants.FUNC_COLUMN_WEIGHT, GUIConstants.UPPER_WEIGHT, 1, 1));

        expressionTextArea = new LJTextArea(LocalizationKeys.INSERT_EXPRESSION_KEY, jfpga.getFlp(),
                GUIConstants.EXPRESSION_TEXT_AREA_ROWS, GUIConstants.EXPRESSION_TEXT_AREA_COLUMN);
        expressionTextArea.setLineWrap(true);
        upperPanel.add(expressionTextArea, BorderLayout.CENTER);

        expressionButton = new LJButton(LocalizationKeys.GENERATE_FROM_EXPRESSION_KEY, jfpga.getFlp());
        expressionButton.addActionListener(generateFromExpressionAction);
        upperPanel.add(expressionButton, BorderLayout.SOUTH);

        JPanel listPanel = new JPanel(new BorderLayout());
        mainPanel.add(listPanel, getGBC(2, 1, GUIConstants.FUNC_COLUMN_WEIGHT, GUIConstants.LOWER_WEIGHT, 1, GridBagConstraints.REMAINDER));

        JPanel descPanel = new JPanel(new GridBagLayout());
        listPanel.add(descPanel, BorderLayout.NORTH);

        descPanel.add(new LJLabel(LocalizationKeys.BOOLEAN_FUNCTIONS_KEY, jfpga.getFlp()), getGBC(0, 0, 3, 1));
        descPanel.add(new LJLabel(LocalizationKeys.INDEX_KEY, jfpga.getFlp()), getGBC(0, 1, 1, 1));
        descPanel.add(new LJLabel(LocalizationKeys.NAME_KEY, jfpga.getFlp()), getGBC(1, 1, 1, 1));
        descPanel.add(new LJLabel(LocalizationKeys.INPUTS_KEY, jfpga.getFlp()), getGBC(2, 1, 1, 1));

        listPanel.add(new JScrollPane(boolFuncJlist), BorderLayout.CENTER);
    }

    private GridBagConstraints getGBC(int x, int y, double weightX, double weightY, int cellWidth, int cellHeight) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = weightX;
        gbc.weighty = weightY;
        gbc.gridwidth = cellWidth;
        gbc.gridheight = cellHeight;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        return gbc;
    }

    private GridBagConstraints getGBC(int x, int y, int cellWidth, int cellHeight) {
        return getGBC(x, y, 0.0, 0.0, cellWidth, cellHeight);
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
