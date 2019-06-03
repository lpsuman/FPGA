package hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JPanelPair;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJLabel;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.BoolVectorTableModel;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.MyJTable;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.TableItemListener;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BooleanVectorController extends AbstractGUIController<BooleanVector> {

    private Set<BooleanVectorListener> booleanVectorListeners;

    private JComboBox<Integer> numInputsComboBox;
    private JComboBox<Integer> numFunctionsComboBox;

    private int indexVectorBeingEdited;

    public BooleanVectorController(SessionController parentSession) {
        super(parentSession, parentSession.getSessionData().getBoolVectors());
    }

    @Override
    protected void loadData() {
        indexVectorBeingEdited = -1;
        itemTableModel = new BoolVectorTableModel(parentSession, this, getAllItems());
        itemTable = new MyJTable(itemTableModel);
        itemTable.setRowSelectionAllowed(true);
        itemTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        itemTable.addColumnClickedListener(3, row -> {
            if (indexVectorBeingEdited == -1 || row != indexVectorBeingEdited) {
                getJfpga().getCurrentSession().getBooleanFunctionController().setItems(getItems().get(row).getBoolFunctions(), areItemsEditable());
                indexVectorBeingEdited = row;
            }
        });

        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                stopEditingVector();
            }
        });
    }

    @Override
    protected void setupGUI() {
        JPanelPair panelPair = GUIUtility.generatePanelPair(mainPanel, 0, 0.5);
        JPanel upperPanel = panelPair.getUpperPanel();
        JPanel lowerPanel = panelPair.getLowerPanel();

        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getGenerateFromFunctionsAction())));
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getGenerateRandomVectorAction())));

        numInputsComboBox = GUIUtility.getComboBoxFromLimit(Constants.NUM_FUNCTION_INPUTS_LIMIT);
        numInputsComboBox.setSelectedIndex(1);
        upperPanel.add(GUIUtility.getComboBoxPanel(numInputsComboBox, getLocProv(), LocalizationKeys.INPUTS_KEY));

        numFunctionsComboBox = GUIUtility.getComboBoxFromLimit(Constants.NUM_FUNCTIONS_LIMIT);
        numFunctionsComboBox.setSelectedIndex(2);
        upperPanel.add(GUIUtility.getComboBoxPanel(numFunctionsComboBox, getLocProv(), LocalizationKeys.FUNCTIONS_KEY));

        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getDuplicateSelectedVectorAction())));
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getRemoveSelectedVectorAction())));
        upperPanel.add(GUIUtility.putIntoPanelWithBorder(new JButton(getJfpga().getDisplayAllVectorsAction())));

        lowerPanel.add(new LJLabel(LocalizationKeys.BOOLEAN_VECTORS_KEY, getLocProv(), SwingConstants.CENTER), BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        itemTable.applyMinSizeInScrollPane();
    }

    @Override
    public void updateActionsAreEnabled() {
        getJfpga().getGenerateFromFunctionsAction().setEnabled(areItemsEditable());
        getJfpga().getGenerateRandomVectorAction().setEnabled(areItemsEditable());
        getJfpga().getDuplicateSelectedVectorAction().setEnabled(areItemsEditable());
        getJfpga().getRemoveSelectedVectorAction().setEnabled(areItemsEditable());
    }

    public JComboBox<Integer> getNumInputsComboBox() {
        return numInputsComboBox;
    }

    public JComboBox<Integer> getNumFunctionsComboBox() {
        return numFunctionsComboBox;
    }

    public void updateVectorBeingEdited() {
        if (indexVectorBeingEdited != -1) {
            getItems().get(indexVectorBeingEdited).updateTable(false);
            itemTableModel.fireTableRowsUpdated(indexVectorBeingEdited, indexVectorBeingEdited);
        }
    }

    public void stopEditingVector() {
        if (indexVectorBeingEdited != -1) {
            indexVectorBeingEdited = -1;
            getJfpga().getDisplayAllFunctionsAction().actionPerformed(null);
        }
    }

    @Override
    protected Collection<? extends TableItemListener<BooleanVector>> getTableItemListeners() {
        return booleanVectorListeners;
    }

    public void addBooleanVectorListener(BooleanVectorListener listener) {
        if (booleanVectorListeners == null) {
            booleanVectorListeners = new HashSet<>();
        }
        booleanVectorListeners.add(listener);
    }

    public void removeBooleanVectorListener(BooleanVectorListener listener) {
        if (booleanVectorListeners != null) {
            booleanVectorListeners.remove(listener);
        }
    }
}
