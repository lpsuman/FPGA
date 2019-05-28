package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class AutoSelectJTable extends JTable {

    public AutoSelectJTable(TableModel dm) {
        super(dm);
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        boolean result = super.editCellAt(row, column, e);
        selectAll(e);
        return result;
    }

    private void selectAll(EventObject e) {
        final Component editor = getEditorComponent();

        if (!(editor instanceof JTextComponent)) {
            return;
        }

        if (e == null) {
            ((JTextComponent) editor).selectAll();
            return;
        }

        if (e instanceof KeyEvent) {
            ((JTextComponent) editor).selectAll();
            return;
        }

        if (e instanceof ActionEvent) {
            ((JTextComponent) editor).selectAll();
            return;
        }

        if (e instanceof MouseEvent) {
            SwingUtilities.invokeLater(((JTextComponent) editor)::selectAll);
        }
    }
}
