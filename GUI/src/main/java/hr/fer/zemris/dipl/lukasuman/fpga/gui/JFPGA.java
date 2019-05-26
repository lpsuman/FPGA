package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolParser;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session.*;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.icon.IconLoader;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.FormLocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJMenu;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProviderImpl;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionData;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


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

    private BoolParser parser;

    private Action newSessionAction;
    private Action openSessionAction;
    private Action saveSessionAction;
    private Action saveSessionAsAction;
    private Action closeSessionAction;
    private Action exitAction;

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
//                int exitDecision = JOptionPane.showConfirmDialog(
//                        JFPGA.this, "Are you serious?", "Confirm", JOptionPane.YES_NO_OPTION);
//                if (exitDecision != JOptionPane.YES_OPTION) {
//                    return;
//                }

                if (checkForChanges()) {
                    if (!sessions.isEmpty()) {
                        saveSessionPaths();
                    }
                    dispose();
                }
            }
        });

        sessions = new ArrayList<>();
        mapCloseButtonToComp = new HashMap<>();
        flp = new FormLocalizationProvider(LocalizationProviderImpl.getInstance(), this);
        parser = new BoolParser();

        initActions();
        createMenus();
        initGUI();

        loadPreviousSessions();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LocalizationProviderImpl.getInstance().setLanguage(GUIConstants.DEFAULT_LANGUAGE);
            new JFPGA().setVisible(true);
        });
    }

    private void initActions() {
        newSessionAction = new NewSessionAction(this);
        openSessionAction = new OpenSessionAction(this);
        saveSessionAction = new SaveSessionAction(this);
        saveSessionAsAction = new SaveSessionAsAction(this);
        closeSessionAction = new CloseSessionAction(this);
        exitAction = new ExitAction(this);
    }

    private void createMenus() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new LJMenu("file", flp);
        menuBar.add(fileMenu);

        fileMenu.add(new JMenuItem(newSessionAction));
        fileMenu.add(new JMenuItem(openSessionAction));
        fileMenu.add(new JMenuItem(saveSessionAction));
        fileMenu.add(new JMenuItem(saveSessionAsAction));
        fileMenu.add(new JMenuItem(closeSessionAction));

        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(exitAction));

        JMenu editMenu = new LJMenu("edit", flp);
        menuBar.add(editMenu);

//        editMenu.add(new JMenuItem(copyAction));
//        editMenu.add(new JMenuItem(cutAction));
//        editMenu.add(new JMenuItem(pasteAction));

        JMenu languageMenu = new JMenu("Languages/Jezici");
        menuBar.add(languageMenu);

        for (String language : GUIConstants.SUPPORTED_LANGUAGES) {
            JMenuItem menuItem = new JMenuItem(language);
            menuItem.addActionListener(e -> {
                LocalizationProviderImpl.getInstance().setLanguage(
                        language.substring(0, 2).toLowerCase());
            });
            languageMenu.add(menuItem);
        }

//        JMenu toolsMenu = new LJMenu("tools", flp);
//        menuBar.add(toolsMenu);
//
//        changeCaseMenu = new LJMenu("case_menu", flp);
//        toolsMenu.add(changeCaseMenu);
//        changeCaseMenu.setEnabled(false);
//        changeCaseMenu.add(new JMenuItem(new UpperCaseAction(this)));
//        changeCaseMenu.add(new JMenuItem(new LowerCaseAction(this)));
//        changeCaseMenu.add(new JMenuItem(new InvertCaseAction(this)));
//
//        sortMenu = new LJMenu("sort", flp);
//        toolsMenu.add(sortMenu);
//        sortMenu.setEnabled(false);
//        sortMenu.add(new JMenuItem(new SortAscendingAction(this)));
//        sortMenu.add(new JMenuItem(new SortDescendingAction(this)));
//
//        uniqueMenuItem = new JMenuItem(new UniqueAction(this));
//        toolsMenu.add(uniqueMenuItem);
//        uniqueMenuItem.setEnabled(false);

        this.setJMenuBar(menuBar);
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
                    prefix = flp.getString(LocalizationKeys.NEW_SESSION_KEY);
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
    }

    public void createNewSession(SessionData sessionData, ImageIcon icon) {
        Path filePath = sessionData.getFilePath();
        String title = filePath == null ? flp.getString(LocalizationKeys.NEW_SESSION_KEY) : filePath.getFileName().toString();
        String tooltip = filePath == null ? flp.getString(LocalizationKeys.NEW_SESSION_KEY) : filePath.toAbsolutePath().toString();

        JPanel tabPanel = new JPanel();
        tabPanel.setOpaque(false);
        tabPanel.setLayout(new BorderLayout());
        JLabel tabLabel = new JLabel(title);
        tabLabel.setIcon(icon);
        tabPanel.add(tabLabel, BorderLayout.CENTER);

        SessionController sessionController = new SessionController(sessionData, this, tabLabel);
        sessions.add(sessionController);
        JPanel sessionPanel = sessionController.getMainPanel();

        JPanel closeButtonPanel = new JPanel();
        int verticalBorder = (GUIConstants.CLOSE_BUTTON_SIZE.height - GUIConstants.ICON_SIZE.height) / 2;
        int horizontalBorder = (GUIConstants.CLOSE_BUTTON_SIZE.width - GUIConstants.ICON_SIZE.width) / 2;
        closeButtonPanel.setBorder(new EmptyBorder(verticalBorder, horizontalBorder, verticalBorder, horizontalBorder));
        closeButtonPanel.setOpaque(false);
        tabPanel.add(closeButtonPanel, BorderLayout.EAST);

        JButton closeTabButton = new JButton("X");
        mapCloseButtonToComp.put(closeTabButton, sessionPanel);
        closeTabButton.addActionListener(closeSessionAction);
        closeTabButton.setText("x");
        closeButtonPanel.add(closeTabButton);

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
    public void removeSession(int index) {
        if (index < 0 || index > tabPane.getComponentCount() - 1) {
            return;
        }
        if (!checkSessionForChange(index)) {
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
     * Checks whether there are any modified and unsaved sessions.
     * @return Returns {@code true} if there are unsaved sessions and it is OK to close the window.
     */
    private boolean checkForChanges() {
        for (int i = 0, n = tabPane.getTabCount(); i < n; i++) {
            if (!checkSessionForChange(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the session was modified and asks the user if the changes should be saved.
     * @param index Index of the session.
     * @return Returns {@code false} if the user's decision was CANCEL.
     */
    private boolean checkSessionForChange(int index) {
        SessionController session = sessions.get(index);
        if (session.isEdited()) {
            Path filePath = session.getSessionData().getFilePath();
            String fileName = filePath == null ? flp.getString(LocalizationKeys.NEW_SESSION_KEY) : filePath.getFileName().toString();
            String saveChanges = flp.getString(LocalizationKeys.SAVE_CHANGES_KEY);
            int decision = JOptionPane.showConfirmDialog(
                    this,
                    String.format(flp.getString(LocalizationKeys.SESSION_S_WAS_MODIFIED_KEY)+ " %s", fileName, saveChanges),
                    saveChanges,
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (decision == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (decision == JOptionPane.YES_OPTION) {
                int previousIndex = tabPane.getSelectedIndex();
                tabPane.setSelectedIndex(index);
                saveSessionAction.actionPerformed(null);
                tabPane.setSelectedIndex(previousIndex);
            }
        }
        return true;
    }

    private void saveSessionPaths() {
        List<String> sessionPaths = sessions.stream()
                .map(s -> s.getSessionData().getFilePath())
                .filter(Objects::nonNull)
                .map(Path::toString)
                .collect(Collectors.toList());
        Utility.saveTextFile(GUIConstants.PREVIOUS_SESSIONS_FILE_PATH, sessionPaths);
    }

    private void loadPreviousSessions() {
        List<String> sessionPaths = Utility.readTextFile(GUIConstants.PREVIOUS_SESSIONS_FILE_PATH);

        if (sessionPaths == null) {
            System.err.println("No file of previous sessions found.");
            return;
        }

        for (String sessionPath : sessionPaths) {
            SessionData sessionData;

            try {
                sessionData = SessionData.deserializeFromFile(sessionPath);
            } catch (IOException e) {
                System.err.println("Couldn't load a previous session. IO error.");
                e.printStackTrace();
                return;
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't load a previous session. Invalid class.");
                e.printStackTrace();
                return;
            }

            createNewSession(sessionData, blueDiskette);
        }
    }

    public FormLocalizationProvider getFlp() {
        return flp;
    }

    public SessionController getCurrentSession() {
        int indexCurrentSession = getIndexCurrentSession();

        if (indexCurrentSession < 0) {
            return null;
        } else {
            return sessions.get(indexCurrentSession);
        }
    }

    public int getIndexCurrentSession() {
        return tabPane.getSelectedIndex();
    }

    public void setCurrentSession(int index) {
        tabPane.setSelectedIndex(index);
    }

    public Map<JButton, Component> getMapCloseButtonToComp() {
        return mapCloseButtonToComp;
    }

    public int getIndexComponent(Component component) {
        return tabPane.indexOfComponent(component);
    }

    public BoolParser getParser() {
        return parser;
    }

    public ImageIcon getRedDiskette() {
        return redDiskette;
    }

    public ImageIcon getBlueDiskette() {
        return blueDiskette;
    }

    public Action getSaveSessionAction() {
        return saveSessionAction;
    }
}
