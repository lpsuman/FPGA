package hr.fer.zemris.dipl.lukasuman.fpga.gui.action;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ListAction extends MouseAdapter {
    private static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

    private JList list;
    private KeyStroke keyStroke;

    public ListAction(JList list, Action action, KeyStroke keyStroke) {
        this.list = list;
        this.keyStroke = keyStroke;

        InputMap im = list.getInputMap();
        im.put(keyStroke, keyStroke);
        setAction(action);
        list.addMouseListener(this);
    }

    public ListAction(JList list, Action action) {
        this(list, action, ENTER);
    }

    public void setAction(Action action) {
        list.getActionMap().put(keyStroke, action);
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            Action action = list.getActionMap().get(keyStroke);

            if (action != null) {
                ActionEvent event = new ActionEvent(list, ActionEvent.ACTION_PERFORMED, "");
                action.actionPerformed(event);
            }
        }
    }
}
