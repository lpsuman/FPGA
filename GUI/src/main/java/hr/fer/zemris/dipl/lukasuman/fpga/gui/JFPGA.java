package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.CloseSessionAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.icon.IconLoader;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.FormLocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProviderImpl;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionData;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JFPGA extends JFrame {

    private static final long serialVersionUID = 3406470032267179318L;

    /**Red diskette icon.*/
    private ImageIcon redDiskette;
    /**Blue diskette icon.*/
    private ImageIcon blueDiskette;

    /**Center panel which contains the tabPane.*/
    private JPanel centerPanel;
    /**The tabbed pane.*/
    private JTabbedPane tabPane;

    private List<SessionController> sessions;

    private Action closeSessionAction;

    /**Map used to link tab closing buttons to their respective tabs.*/
    private Map<JButton, Component> mapCloseButtonToComp;

    /**The localization provider for this frame.*/
    private FormLocalizationProvider flp;

    public JFPGA() {
        setTitle(GUIConstants.DEFAULT_APPLICATION_NAME);
        setSize(GUIConstants.DEFAULT_WINDOW_WIDTH, GUIConstants.DEFAULT_WINDOW_HEIGHT);
        setLocation(GUIConstants.DEFAULT_WINDOW_LOCATION_X, GUIConstants.DEFAULT_WINDOW_LOCATION_Y);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int exitDecision = JOptionPane.showConfirmDialog(
                        JFPGA.this, "Are you serious?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (exitDecision != JOptionPane.YES_OPTION) {
                    return;
                }

                if (checkForChanges()) {
                    dispose();
                }
            }
        });

        sessions = new ArrayList<>();
        mapCloseButtonToComp = new HashMap<>();
        flp = new FormLocalizationProvider(LocalizationProviderImpl.getInstance(), this);
        initGUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LocalizationProviderImpl.getInstance().setLanguage(GUIConstants.DEFAULT_LANGUAGE);
            new JFPGA().setVisible(true);
        });
    }

    private void initGUI() {
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        centerPanel = new JPanel();
        cp.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setLayout(new BorderLayout());

        tabPane = new JTabbedPane();
        centerPanel.add(tabPane, BorderLayout.CENTER);

        tabPane.addChangeListener(e -> {
            String prefix = "";
            int index = tabPane.getSelectedIndex();
            if (index >= 0) {
                Path filePath = sessions.get(index).getSessionData().getFilePath();
                if (filePath == null) {
                    prefix = GUIConstants.DEFAULT_NEW_SESSION_NAME;
                } else {
                    prefix = filePath.toString();
                }
                prefix += " - ";

            }
            setTitle(prefix + GUIConstants.DEFAULT_APPLICATION_NAME);
        });

        try {
            redDiskette = IconLoader.loadRedDisketteIcon();
            blueDiskette = IconLoader.loadBlueDisketteIcon();
        } catch (IOException exc) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error while trying to load icons!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        initActions();
    }

    private void initActions() {
        closeSessionAction = new CloseSessionAction(this);
    }

    public void createNewTab(SessionData sessionData, ImageIcon icon) {
        Path filePath = sessionData.getFilePath();
        String title = filePath == null ? GUIConstants.DEFAULT_NEW_SESSION_NAME : filePath.getFileName().toString();
        String tooltip = filePath == null ? GUIConstants.DEFAULT_NEW_SESSION_NAME : filePath.toAbsolutePath().toString();

        JPanel tabPanel = new JPanel();
        tabPanel.setOpaque(false);
        tabPanel.setLayout(new BorderLayout());
        JLabel tabLabel = new JLabel(title);
        tabLabel.setIcon(icon);
        tabPanel.add(tabLabel, BorderLayout.WEST);

        SessionController sessionController = new SessionController(sessionData);
        JPanel sessionPanel = sessionController.getMainPanel();

        JButton closeTabButton = new JButton("x");
        mapCloseButtonToComp.put(closeTabButton, sessionPanel);
        closeTabButton.addActionListener(closeSessionAction);
        closeTabButton.setText("x");
        tabPanel.add(closeTabButton, BorderLayout.EAST);

        tabPane.add(sessionPanel);
        int indexOfNewTab = tabPane.getTabCount() - 1;
        tabPane.setTabComponentAt(indexOfNewTab, tabPanel);
        tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
        tabPane.setToolTipTextAt(tabPane.getSelectedIndex(), tooltip);
    }

    /**
     * Removes a tab at the specified index.
     * @param index Index of the tab.
     */
    public void removeTab(int index) {
        if (index < 0 || index > tabPane.getComponentCount() - 1) {
            return;
        }
        if (!checkFileForChange(index)) {
            return;
        }

        sessions.remove(index);
        Component comp = tabPane.getComponent(index);
        JButton key = null;
        for (Map.Entry<JButton, Component> entry : mapCloseButtonToComp.entrySet()) {
            Component value = entry.getValue();
            if (value.equals(comp)) {
                key = entry.getKey();
                break;
            }
        }
        mapCloseButtonToComp.remove(key);

        tabPane.remove(index);
    }

    /**
     * Checks whether there are any modified and unsaved files.
     * @return Returns {@code true} if there are unsaved files and it is OK to close the window.
     */
    private boolean checkForChanges() {
        for (int i = 0, n = tabPane.getTabCount(); i < n; i++) {
            if (!checkFileForChange(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the file was modified and asks the user if the changes should be saved.
     * @param index Index of the tab.
     * @return Returns {@code false} if the user's decision was CANCEL.
     */
    private boolean checkFileForChange(int index) {
        SessionData session = sessions.get(index).getSessionData();
        if (session.isEdited()) {
            Path filePath = session.getFilePath();
            String fileName = filePath == null ? GUIConstants.DEFAULT_NEW_SESSION_NAME : filePath.getFileName().toString();
            int decision = JOptionPane.showConfirmDialog(
                    this,
                    "SessionData " + fileName + " was modified. Save changes?",
                    "Save changes",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (decision == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (decision == JOptionPane.YES_OPTION) {
                int previousIndex = tabPane.getSelectedIndex();
                tabPane.setSelectedIndex(index);
//                saveDocumentAction.actionPerformed(null);
                tabPane.setSelectedIndex(previousIndex);
            }
        }
        return true;
    }

    public FormLocalizationProvider getFlp() {
        return flp;
    }
}
