package hr.fer.zemris.dipl.lukasuman.fpga.gui.action;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class EditListAction<T> extends AbstractAction {
    private JList<T> list;
    private JPopupMenu editPopup;
    private JTextField editTextField;

    public EditListAction() {
    }

    protected abstract void applyValueToModel(String value, DefaultListModel<T> model, int row);

    protected abstract String getTextFromValue(T value);

    public void actionPerformed(ActionEvent e) {
        list = (JList<T>) e.getSource();
        DefaultListModel<T> model = (DefaultListModel<T>) list.getModel();

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
            DefaultListModel<T> model = (DefaultListModel<T>) list.getModel();
            int row = list.getSelectedIndex();
            applyValueToModel(value, model, row);
            editPopup.setVisible(false);
        });

        editPopup = new JPopupMenu();
        editPopup.setBorder( new EmptyBorder(0, 0, 0, 0) );
        editPopup.add(editTextField);
    }
}
