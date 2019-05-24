package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BooleanFunctionController {

    private List<BooleanFunction> booleanFunctions;
    private JList<BoolFuncListWrapper> boolFuncJlist;
    private JPanel mainPanel;

    private Set<BooleanFunctionListener> booleanFunctionListeners;

    public BooleanFunctionController(List<BooleanFunction> booleanFunctions) {
        Utility.checkNull(booleanFunctions, "list of boolean functions");
        this.booleanFunctions = booleanFunctions;
        loadData(booleanFunctions);
        initGUI();
    }

    private void loadData(List<BooleanFunction> booleanFunctions) {
        DefaultListModel<BoolFuncListWrapper> listModel = new DefaultListModel<>();

        for (int i = 0; i < booleanFunctions.size(); i++) {
            listModel.addElement(new BoolFuncListWrapper(booleanFunctions.get(i), i));
        }

        boolFuncJlist = new JList<>(listModel);
    }

    private void initGUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();


        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.add(boolFuncJlist, BorderLayout.CENTER);

        mainPanel.add(listPanel, BorderLayout.CENTER);
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
        booleanFunctionListeners.forEach(l -> l.booleanFunctionAdded(newBooleanFunction, index));
    }

    public void addBooleanFunction(BooleanFunction newBooleanFunction) {
        addBooleanFunction(newBooleanFunction, booleanFunctions.size());
    }

    public void removeBooleanFunction(int index) {
        Utility.checkRange(index, 0, booleanFunctions.size() - 1);
        BooleanFunction removed = booleanFunctions.remove(index);
        booleanFunctionListeners.forEach(l -> l.booleanFunctionRemoved(removed, index));
    }

    public void changeFunctionName(int index, String newName) {
        Utility.checkRange(index, 0, booleanFunctions.size() - 1);
        Utility.checkIfValidString(newName, "new boolean function's name");
        BooleanFunction booleanFunction = booleanFunctions.get(index);
        String oldName = booleanFunction.getName();
        booleanFunction.setName(newName);
        booleanFunctionListeners.forEach(l -> l.booleanFunctionNameEdited(booleanFunction, index, oldName));
    }
}
