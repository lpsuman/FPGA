package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJLabel;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.MyAbstractTableModel;
import hr.fer.zemris.dipl.lukasuman.fpga.util.ArgumentLimit;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class GUIUtility {

    public static final DefaultTableCellRenderer LEFT_TABLE_CELL_RENDERER = new DefaultTableCellRenderer();
    public static final DefaultTableCellRenderer CENTER_TABLE_CELL_RENDERER = new DefaultTableCellRenderer();
    public static final DefaultTableCellRenderer RIGHT_TABLE_CELL_RENDERER = new DefaultTableCellRenderer();

    static {
        LEFT_TABLE_CELL_RENDERER.setHorizontalAlignment(SwingConstants.LEFT);
        CENTER_TABLE_CELL_RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
        RIGHT_TABLE_CELL_RENDERER.setHorizontalAlignment(SwingConstants.RIGHT);
    }

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
        return gbc;
    }

    public static GridBagConstraints getGBC(int x, int y, double weightX, double weightY) {
        return getGBC(x, y, weightX, weightY, 1, 1);
    }

    public static JPanelPair generatePanelPair(JPanel mainPanel, int indexX, double weightX) {
        JPanel parentPanel = GUIUtility.getPanel();
        parentPanel.setPreferredSize(new Dimension(0, 0));
        GridBagConstraints gbc = GUIUtility.getGBC(indexX, 0, weightX, 0.5);
        mainPanel.add(parentPanel, gbc);

        JPanel upperPanel = GUIUtility.getPanelWithBorder();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
        parentPanel.add(upperPanel, BorderLayout.NORTH);
        JPanel lowerPanel = GUIUtility.getPanelWithBorder();
        parentPanel.add(lowerPanel, BorderLayout.CENTER);

        return new JPanelPair(upperPanel, lowerPanel);
    }

    public static JPanelPair putGridBagInBorderCenter() {
        JPanel mainPanel = GUIUtility.getPanel();
        JPanel maxSizeMainPanel = GUIUtility.getPanel(new GridBagLayout());
        mainPanel.add(maxSizeMainPanel, BorderLayout.CENTER);
        JPanel temp = mainPanel;
        mainPanel = maxSizeMainPanel;

        return new JPanelPair(mainPanel, temp);
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

    public static JComboBox<Integer> getComboBoxFromLimit(ArgumentLimit<Integer> limit) {
        JComboBox<Integer> comboBox = new JComboBox<>(Utility.generateRangeArray(
                limit.getLowerLimit(),
                limit.getUpperLimit() + 1));

        comboBox.setPrototypeDisplayValue(limit.getUpperLimit());
        return comboBox;
    }

    public static JPanel getComboBoxPanel(JComboBox comboBox, LocalizationProvider locProvider, String labelLocKey) {
        JPanel comboBoxPanel = GUIUtility.getPanelWithBorder(new GridBagLayout());

        GridBagConstraints gbc = GUIUtility.getGBC(0, 0, 1.0 - GUIConstants.COMBO_BOX_WIDTH_WEIGHT, 1.0);
        comboBoxPanel.add(GUIUtility.putIntoPanelWithBorder(
                new LJLabel(labelLocKey, locProvider, SwingConstants.CENTER)), gbc);

        gbc = GUIUtility.getGBC(1, 0, GUIConstants.COMBO_BOX_WIDTH_WEIGHT, 1.0);
        comboBoxPanel.add(GUIUtility.putIntoPanelWithBorder(comboBox), gbc);

        return comboBoxPanel;
    }
}
