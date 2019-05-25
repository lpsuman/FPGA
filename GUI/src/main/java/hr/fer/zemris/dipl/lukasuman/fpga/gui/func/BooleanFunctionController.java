package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func.BooleanExpressionProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func.EditFuncNameListAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func.GenerateFromExpressionAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.ListAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJLabel;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJTextField;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BooleanFunctionController implements BooleanExpressionProvider {

    private JFPGA jfpga;
    private SessionController parentSession;
    private List<BooleanFunction> booleanFunctions;
    private JPanel mainPanel;

    private DefaultListModel<BoolFuncListWrapper> boolFuncListModel;
    private JList<BoolFuncListWrapper> boolFuncJlist;
    JTextField expressionTextField;

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

        for (int i = 0; i < booleanFunctions.size(); i++) {
            boolFuncListModel.addElement(new BoolFuncListWrapper(booleanFunctions.get(i), i));
        }

        boolFuncJlist = new JList<>(boolFuncListModel);
        ListAction la = new ListAction(boolFuncJlist, new EditFuncNameListAction(this));
    }

    private void initActions() {
        generateFromExpressionAction = new GenerateFromExpressionAction(jfpga, this);
    }

    private void initGUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(initFuncList(), BorderLayout.WEST);
    }

    private JPanel initFuncList() {
        JPanel funcListPanel = new JPanel();
        funcListPanel.setLayout(new BorderLayout());

        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BorderLayout());
        funcListPanel.add(upperPanel, BorderLayout.NORTH);

        expressionTextField = new LJTextField(LocalizationKeys.INSERT_EXPRESSION_KEY, jfpga.getFlp());
        expressionTextField.addActionListener(generateFromExpressionAction);
        upperPanel.add(expressionTextField, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        funcListPanel.add(centerPanel, BorderLayout.CENTER);

        centerPanel.add(new LJLabel(LocalizationKeys.BOOLEAN_FUNCTIONS_KEY, jfpga.getFlp()), BorderLayout.NORTH);
        centerPanel.add(boolFuncJlist, BorderLayout.CENTER);

        return funcListPanel;
    }

    @Override
    public String getExpressionString() {
        return expressionTextField.getText();
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

    public int getNumBooleanFunctions() {
        return booleanFunctions.size();
    }

    public void addBooleanFunction(BooleanFunction newBooleanFunction, int index) {
        Utility.checkNull(newBooleanFunction, "new boolean function");
        Utility.checkRange(index, 0, booleanFunctions.size());
        booleanFunctions.add(index, newBooleanFunction);
        BoolFuncListWrapper boolFuncListWrapper = new BoolFuncListWrapper(newBooleanFunction, index);
        boolFuncListModel.add(index, boolFuncListWrapper);

        if (booleanFunctionListeners != null) {
            booleanFunctionListeners.forEach(l -> l.booleanFunctionAdded(newBooleanFunction, index));
        }
        addBooleanFunctionListener(boolFuncListWrapper);
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
}
