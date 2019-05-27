package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;

public class JPanelPair {

    private JPanel upperPanel;
    private JPanel lowerPanel;

    public JPanelPair(JPanel upperPanel, JPanel lowerPanel) {
        this.upperPanel = Utility.checkNull(upperPanel, "upper panel");
        this.lowerPanel = Utility.checkNull(lowerPanel, "lower panel");
    }

    public JPanel getUpperPanel() {
        return upperPanel;
    }

    public JPanel getLowerPanel() {
        return lowerPanel;
    }
}
