package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GUIUtility {

    private GUIUtility() {
    }

    public static Border getBorder(int size) {
        return new EmptyBorder(size, size, size, size);
    }

    public static JPanel getPanel(LayoutManager layoutManager) {
        Utility.checkNull(layoutManager, "layout manager");
        return new JPanel(layoutManager);
    }

    public static JPanel getPanel() {
        return getPanel(new BorderLayout());
    }

    public static JPanel getPanelWithBorder(LayoutManager layoutManager) {
        Utility.checkNull(layoutManager, "layout manager");
        JPanel result = getPanel(layoutManager);
        result.setBorder(getBorder(GUIConstants.DEFAULT_BORDER_SIZE));
        return result;
    }

    public static JPanel getPanelWithBorder() {
        return getPanelWithBorder(new BorderLayout());
    }

    public static JPanel putIntoPanelWithBorder(Component component) {
        JPanel result = getPanelWithBorder();
        result.add(component);
        return result;
    }

    public static GridBagConstraints getGBC(int x, int y, double weightX, double weightY, int cellWidth, int cellHeight) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = weightX;
        gbc.weighty = weightY;
        gbc.gridwidth = cellWidth;
        gbc.gridheight = cellHeight;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        return gbc;
    }

    public static GridBagConstraints getGBC(int x, int y, int cellWidth, int cellHeight) {
        return getGBC(x, y, 0.0, 0.0, cellWidth, cellHeight);
    }
}
