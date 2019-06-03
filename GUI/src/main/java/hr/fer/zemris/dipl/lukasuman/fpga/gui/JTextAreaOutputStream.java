package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class JTextAreaOutputStream extends OutputStream {

    private final JTextArea destination;
    private final PrintStream stdOut;
    private final boolean redirectStdOut;

    public JTextAreaOutputStream(JTextArea destination, boolean redirectStdOut) {
        this.destination = Utility.checkNull(destination, "destination JTextArea");
        this.stdOut = System.out;
        this.redirectStdOut = redirectStdOut;
    }

    @Override
    public void write(byte[] buffer, int offset, int length) {
        final String text = new String(buffer, offset, length);

        if (!redirectStdOut) {
            stdOut.write(buffer, offset, length);
        }

        SwingUtilities.invokeLater(() -> destination.append(text));
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    public PrintStream getStdOut() {
        return stdOut;
    }
}
