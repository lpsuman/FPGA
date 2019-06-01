package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class JTextAreaOutputStream extends OutputStream {

    private final JTextArea destination;

    public JTextAreaOutputStream(JTextArea destination) {
        this.destination = Utility.checkNull(destination, "destination JTextArea");
    }

    @Override
    public void write(byte[] buffer, int offset, int length) {
        final String text = new String(buffer, offset, length);
        SwingUtilities.invokeLater(() -> destination.append(text));
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }
}
