package hr.fer.zemris.dipl.lukasuman.fpga.gui.action;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 *	A simple popup editor for a JList that allows you to change
 *  the value in the selected row.
 *
 *  The default implementation has a few limitations:
 *
 *  a) the JList must be using the DefaultListModel
 *  b) the data in the model is replaced with a String object
 *
 *  If you which to use a different model or different data then you must
 *  extend this class and:
 *
 *  a) invoke the setModelClass(...) method to specify the ListModel you need
 *  b) override the applyValueToModel(...) method to update the model
 */
public class EditListAction extends AbstractAction {
    private JList list;

    private JPopupMenu editPopup;
    private JTextField editTextField;
    private Class<?> modelClass;

    public EditListAction() {
        setModelClass(DefaultListModel.class);
    }

    protected void setModelClass(Class modelClass) {
        this.modelClass = modelClass;
    }

    protected void applyValueToModel(String value, ListModel model, int row) {
        DefaultListModel dlm = (DefaultListModel)model;
        dlm.set(row, value);
    }

    protected String getTextFromValue(Object value) {
        return value.toString();
    }

    public void actionPerformed(ActionEvent e) {
        list = (JList) e.getSource();
        ListModel model = list.getModel();

        if (! modelClass.isAssignableFrom(model.getClass())) {
            return;
        }

        if (editPopup == null) {
            createEditPopup();
        }

        int row = list.getSelectedIndex();
        Rectangle r = list.getCellBounds(row, row);

        editPopup.setPreferredSize(new Dimension(r.width, r.height));
        editPopup.show(list, r.x, r.y);

        editTextField.setText(getTextFromValue(list.getSelectedValue()));
        editTextField.selectAll();
        editTextField.requestFocusInWindow();
    }

    private void createEditPopup() {
        editTextField = new JTextField();
        Border border = UIManager.getBorder("List.focusCellHighlightBorder");
        editTextField.setBorder( border );

        editTextField.addActionListener(e -> {
            String value = editTextField.getText();
            ListModel model = list.getModel();
            int row = list.getSelectedIndex();
            applyValueToModel(value, model, row);
            editPopup.setVisible(false);
        });

        editPopup = new JPopupMenu();
        editPopup.setBorder( new EmptyBorder(0, 0, 0, 0) );
        editPopup.add(editTextField);
    }
}
