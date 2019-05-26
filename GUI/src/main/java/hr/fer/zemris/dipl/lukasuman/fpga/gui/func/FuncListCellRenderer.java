package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;

import javax.swing.*;
import java.awt.*;

public class FuncListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        BooleanFunction func = (BooleanFunction) value;
        label.setText(String.format("%d  %s  (%d)", index, func.getName(), func.getNumInputs()));

        return label;
    }
}
