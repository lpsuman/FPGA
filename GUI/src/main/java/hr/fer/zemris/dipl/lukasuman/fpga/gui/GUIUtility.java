package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.MyAbstractTableModel;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
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

    public static GridBagConstraints getGBC(int x, int y, double weightX, double weightY, int gridWidth, int gridHeight) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = weightX;
        gbc.weighty = weightY;
        gbc.gridwidth = gridWidth;
        gbc.gridheight = gridHeight;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }

    public static GridBagConstraints getGBC(int x, int y, int cellWidth, int cellHeight) {
        return getGBC(x, y, 0.0, 0.0, cellWidth, cellHeight);
    }

    public static JPanelPair generatePanelPair(JPanel mainPanel, int indexX, double weightX) {
        JPanel parentPanel = GUIUtility.getPanel();
        parentPanel.setPreferredSize(new Dimension(0, 0));
        GridBagConstraints gbc = GUIUtility.getGBC(indexX, 0, weightX, 0.5, 1, 1);
        mainPanel.add(parentPanel, gbc);

        JPanel upperPanel = GUIUtility.getPanelWithBorder();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
        parentPanel.add(upperPanel, BorderLayout.NORTH);
        JPanel lowerPanel = GUIUtility.getPanelWithBorder();
        parentPanel.add(lowerPanel, BorderLayout.CENTER);

        return new JPanelPair(upperPanel, lowerPanel);
    }

    public static void resizeColumns(JTable table, MyAbstractTableModel tableModel) {
        TableColumnModel jTableColumnModel = table.getColumnModel();
        int totalColumnWidth = jTableColumnModel.getTotalColumnWidth();

        for (int i = 0; i < jTableColumnModel.getColumnCount(); i++) {
            TableColumn column = jTableColumnModel.getColumn(i);
            int pWidth = (int) Math.round(tableModel.getColumnWidthPercentage(i) * totalColumnWidth);
            column.setPreferredWidth(pWidth);
        }
    }
}
