package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import com.google.gson.JsonParseException;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.MyGson;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolParser;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.LoadTextAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.SaveTextAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func.*;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session.*;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.solve.*;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.BooleanFunctionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.BooleanVectorController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.FormLocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LJMenu;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProviderHTML;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionData;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
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

    private Action generateFromExpressionAction;
    private Action generateFromTextAction;
    private Action loadTextAction;
    private Action saveTextAction;

    private Action generateRandomFunctionAction;
    private Action duplicateSelectedFunctionAction;
    private Action removeSelectedFunctionAction;
    private Action undoRemoveFunctionAction;
    private Action displayAllFunctionsAction;
    private Action saveFunctionsAction;
    private Action loadFunctionsAction;

    private Action generateFromFunctionsAction;
    private Action generateRandomVectorAction;
    private Action duplicateSelectedVectorAction;
    private Action removeSelectedVectorAction;
    private Action undoRemoveVectorAction;
    private Action displayAllVectorsAction;
    private Action saveVectorsAction;
    private Action loadVectorsAction;
    private Action generateSolvableAction;

    private Action runSolverAction;
    private Action stopSolverAction;
    private Action clearOutputAction;

    private Action solutionToExpressionAction;
    private Action printSolutionAction;
    private Action removeSelectedSolutionAction;
    private Action undoRemoveSolutionAction;
    private Action saveSolutionsAction;
    private Action loadSolutionsAction;

    private List<Action> sessionRequiredActions;

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
                if (GUIConstants.SHOW_CONFIRM_EXIT_DIALOG) {
                    int exitDecision = JOptionPane.showConfirmDialog(
                            JFPGA.this, "Are you serious?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (exitDecision != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                if (checkForChanges()) {
                    if (!sessions.isEmpty()) {
                        saveSessionPaths();
                    }
                    saveLanguage();
                    dispose();
                }
            }
        });

        sessions = new ArrayList<>();
        mapCloseButtonToComp = new HashMap<>();
        flp = new FormLocalizationProvider(LocalizationProviderHTML.getInstance(), this);
        loadLanguage();
        flp.addLocalizationListener(() -> {
            UIManager.put("OptionPane.yesButtonText", flp.getString(LocalizationKeys.YES_KEY));
            UIManager.put("OptionPane.noButtonText", flp.getString(LocalizationKeys.NO_KEY));
            UIManager.put("OptionPane.cancelButtonText", flp.getString(LocalizationKeys.CANCEL_KEY));

            UIManager.put("FileChooser.openButtonText", flp.getString(LocalizationKeys.OPEN_KEY));
            UIManager.put("FileChooser.cancelButtonText", flp.getString(LocalizationKeys.CANCEL_KEY));
            UIManager.put("FileChooser.lookInLabelText", flp.getString(LocalizationKeys.JFC_LOOK_IN_KEY));
            UIManager.put("FileChooser.fileNameLabelText", flp.getString(LocalizationKeys.JFC_FILE_NAME_KEY));
            UIManager.put("FileChooser.filesOfTypeLabelText", flp.getString(LocalizationKeys.JFC_FILES_OF_TYPE_KEY));

            revalidate();
        });
        parser = new BoolParser();

        initActions();
        createMenus();
        initGUI();

        noSessionsPresent();
        loadPreviousSessions();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LocalizationProviderHTML.getInstance().setLanguage(GUIConstants.DEFAULT_LANGUAGE);
            new JFPGA().setVisible(true);
        });
    }

    private void initActions() {
        initSessionActions();
        initFunctionActions();
        initVectorActions();
        initSolverActions();
    }

    private void initSessionActions() {
        newSessionAction = new NewSessionAction(this);
        openSessionAction = new OpenSessionAction(this);
        saveSessionAction = new SaveSessionAction(this);
        saveSessionAsAction = new SaveSessionAsAction(this);
        closeSessionAction = new CloseSessionAction(this);
        exitAction = new ExitAction(this);
    }

    private void initFunctionActions() {
        Supplier<String> textSupplier = () ->
                getCurrentSession().getBooleanFunctionController().getExpressionTextArea().getText();
        Consumer<List<String>> textConsumer = lines ->
                getCurrentSession().getBooleanFunctionController().getExpressionTextArea().setText(String.join("\n", lines));
        generateFromExpressionAction = new GenerateFromExpressionAction(this, textSupplier);
        generateFromTextAction = new GenerateFromTextAction(this, textSupplier);

        loadTextAction = new LoadTextAction(this, LocalizationKeys.LOAD_TEXT_KEY, textConsumer);
        saveTextAction = new SaveTextAction(this, LocalizationKeys.SAVE_TEXT_KEY, textSupplier);

        generateRandomFunctionAction = new GenerateRandomFunctionAction(this, () ->
            GUIUtility.getSelectedComboBoxValue(getCurrentSession().getBooleanFunctionController().getNumInputsComboBox()));
        duplicateSelectedFunctionAction = new DuplicateTableItemAction<>(this,
                () -> getCurrentSession().getBooleanFunctionController(),
                LocalizationKeys.DUPLICATE_FUNCTION_KEY,
                LocalizationKeys.ONE_OR_MORE_FUNCTIONS_KEY);
        removeSelectedFunctionAction = new RemoveTableItemAction<>(this,
                () -> getCurrentSession().getBooleanFunctionController(),
                LocalizationKeys.REMOVE_FUNCTION_KEY,
                LocalizationKeys.ONE_OR_MORE_FUNCTIONS_KEY);
        undoRemoveFunctionAction = new UndoRemoveTableItemAction<>(this,
                () -> getCurrentSession().getBooleanFunctionController(),
                LocalizationKeys.UNDO_REMOVE_FUNCTION_KEY);
        displayAllFunctionsAction = new DisplayAllTableItemAction<>(this,
                () -> getCurrentSession().getBooleanFunctionController(),
                LocalizationKeys.DISPLAY_ALL_FUNCTIONS_KEY);

        solutionToExpressionAction = new SolutionToExpressionAction(this, textConsumer);
        solutionToExpressionAction.setEnabled(false);
        saveFunctionsAction = new SaveTableItemAction<>(this,
                () -> getCurrentSession().getBooleanFunctionController(),
                LocalizationKeys.SAVE_FUNCTIONS_KEY,
                LocalizationKeys.ONE_OR_MORE_FUNCTIONS_KEY);
        loadFunctionsAction = new LoadTableItemAction<>(this,
                () -> getCurrentSession().getBooleanFunctionController(),
                LocalizationKeys.LOAD_FUNCTIONS_KEY,
                LocalizationKeys.ONE_OR_MORE_FUNCTIONS_KEY,
                BooleanFunction[].class);
    }

    private void initVectorActions() {
        generateFromFunctionsAction = new GenerateFromFunctionsAction(this,
                () -> getCurrentSession().getBooleanFunctionController().getSelectedItems(),
                () -> getCurrentSession().getBooleanFunctionController().getAllItems());
        generateRandomVectorAction = new GenerateRandomVectorAction(this,
                () -> GUIUtility.getSelectedComboBoxValue(getCurrentSession().getBooleanVectorController().getNumInputsComboBox()),
                () -> GUIUtility.getSelectedComboBoxValue(getCurrentSession().getBooleanVectorController().getNumFunctionsComboBox()));
        duplicateSelectedVectorAction = new DuplicateTableItemAction<>(this,
                () -> getCurrentSession().getBooleanVectorController(),
                LocalizationKeys.DUPLICATE_VECTOR_KEY,
                LocalizationKeys.ONE_OR_MORE_VECTORS_KEY);
        removeSelectedVectorAction = new RemoveTableItemAction<>(this,
                () -> getCurrentSession().getBooleanVectorController(),
                LocalizationKeys.REMOVE_VECTOR_KEY,
                LocalizationKeys.ONE_OR_MORE_VECTORS_KEY);
        undoRemoveVectorAction = new UndoRemoveTableItemAction<>(this,
                () -> getCurrentSession().getBooleanVectorController(),
                LocalizationKeys.UNDO_REMOVE_VECTOR_KEY);
        displayAllVectorsAction = new DisplayAllTableItemAction<>(this,
                () -> getCurrentSession().getBooleanVectorController(),
                LocalizationKeys.DISPLAY_ALL_VECTORS_KEY);
        saveVectorsAction = new SaveTableItemAction<>(this,
                () -> getCurrentSession().getBooleanVectorController(),
                LocalizationKeys.SAVE_VECTORS_KEY,
                LocalizationKeys.ONE_OR_MORE_VECTORS_KEY);
        loadVectorsAction = new LoadTableItemAction<>(this,
                () -> getCurrentSession().getBooleanVectorController(),
                LocalizationKeys.LOAD_VECTORS_KEY,
                LocalizationKeys.ONE_OR_MORE_VECTORS_KEY,
                BooleanVector[].class);
        generateSolvableAction = new GenerateSolvableAction(this, LocalizationKeys.GENERATE_SOLVABLE_KEY);
    }

    private void initSolverActions() {
        runSolverAction = new RunSolverAction(this,
                () -> getCurrentSession().getBooleanVectorController().getSelectedItem(),
                () -> GUIUtility.getSelectedComboBoxValue(getCurrentSession().getSolverController().getNumCLBInputsComboBox()),
                () -> GUIUtility.getSelectedComboBoxValue(getCurrentSession().getSolverController().getSolverModeComboBox()));

        stopSolverAction = new StopSolverAction(this);
        stopSolverAction.setEnabled(false);
        clearOutputAction = new ClearOutputAction(this);

        printSolutionAction = new PrintSolutionAction(this, () -> {
            if (getCurrentSession().getSolverController().getIndexSelectedItem() != -1) {
                return getCurrentSession().getSolverController().getSelectedItem();
            } else {
                return null;
            }
        }, (text) -> getCurrentSession().getSolverController().getOutputTextArea().append(text));

        removeSelectedSolutionAction = new RemoveTableItemAction<>(this,
                () -> getCurrentSession().getSolverController(),
                LocalizationKeys.REMOVE_SOLUTION_KEY,
                LocalizationKeys.ONE_OR_MORE_SOLUTIONS_KEY);
        undoRemoveSolutionAction = new UndoRemoveTableItemAction<>(this,
                () -> getCurrentSession().getSolverController(),
                LocalizationKeys.UNDO_REMOVE_SOLUTION_KEY);
        saveSolutionsAction = new SaveTableItemAction<>(this,
                () -> getCurrentSession().getSolverController(),
                LocalizationKeys.SAVE_SOLUTIONS_KEY,
                LocalizationKeys.ONE_OR_MORE_SOLUTIONS_KEY);
        loadSolutionsAction = new LoadTableItemAction<>(this,
                () -> getCurrentSession().getSolverController(),
                LocalizationKeys.LOAD_SOLUTIONS_KEY,
                LocalizationKeys.ONE_OR_MORE_SOLUTIONS_KEY,
                BoolVectorSolution[].class);
    }

    private void createMenus() {
        sessionRequiredActions = new ArrayList<>();
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createMenu(LocalizationKeys.FILE_KEY, Collections.singletonList(4),
                newSessionAction,
                openSessionAction,
                saveSessionAction,
                saveSessionAsAction,
                closeSessionAction,
                exitAction));

        Action[] functionActions = new Action[]{
                generateFromExpressionAction,
                generateFromTextAction,
                loadTextAction,
                saveTextAction,
                duplicateSelectedFunctionAction,
                removeSelectedFunctionAction,
                undoRemoveFunctionAction,
                displayAllFunctionsAction,
                saveFunctionsAction,
                loadFunctionsAction
        };
        sessionRequiredActions.addAll(Arrays.asList(functionActions));
        menuBar.add(createMenu(LocalizationKeys.FUNCTIONS_KEY, Collections.singletonList(3), functionActions));

        Action[] vectorActions = new Action[]{
                generateFromFunctionsAction,
                generateRandomVectorAction,
                generateSolvableAction,
                duplicateSelectedVectorAction,
                removeSelectedVectorAction,
                undoRemoveVectorAction,
                displayAllVectorsAction,
                saveVectorsAction,
                loadVectorsAction
        };
        sessionRequiredActions.addAll(Arrays.asList(vectorActions));
        menuBar.add(createMenu(LocalizationKeys.VECTORS_KEY, Collections.singletonList(2), vectorActions));

        Action[] solutionActions = new Action[]{
                removeSelectedSolutionAction,
                undoRemoveSolutionAction,
                saveSolutionsAction,
                loadSolutionsAction
        };
        sessionRequiredActions.addAll(Arrays.asList(solutionActions));
        menuBar.add(createMenu(LocalizationKeys.SOLUTIONS_KEY, null, solutionActions));

        JMenu languageMenu = new JMenu("Languages/Jezici");
        menuBar.add(languageMenu);

        for (String language : GUIConstants.SUPPORTED_LANGUAGES) {
            JMenuItem menuItem = new JMenuItem(language);
            menuItem.addActionListener(e -> LocalizationProviderHTML.getInstance().setLanguage(
                    language.substring(0, 2).toLowerCase()));
            languageMenu.add(menuItem);
        }

        this.setJMenuBar(menuBar);
    }

    private JMenu createMenu(String menuLocKey, List<Integer> separators, Action... menuItemActions) {
        JMenu menu = new LJMenu(menuLocKey, flp);
        for (int i = 0; i < menuItemActions.length; i++) {
            menu.add(new JMenuItem(menuItemActions[i]));
            if (separators != null && separators.contains(i)) {
                menu.addSeparator();
            }
        }
        return menu;
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

                sessions.get(index).updateAsCurrentSession();
            } else {
                noSessionsPresent();
            }
            setTitle(prefix + GUIConstants.DEFAULT_APPLICATION_NAME);
        });

        try {
            redDiskette = IconLoader.loadRedDisketteIcon();
            blueDiskette = IconLoader.loadBlueDisketteIcon();
        } catch (IOException exc) {
            showErrorMsg("Error while trying to load icons!");
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

        try {
            Utility.saveTextFile(GUIConstants.getLastSessionsFilePath(), sessionPaths);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't save session paths.");
        }
    }

    private void loadPreviousSessions() {
        List<String> sessionPaths;
        try {
            sessionPaths = Utility.readTextFileByLines(GUIConstants.getLastSessionsFilePath());
        } catch (FileNotFoundException e) {
            System.err.println("No file of previous sessions found.");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (String sessionPath : sessionPaths) {
            SessionData sessionData;

            try {
                sessionData = MyGson.readFromJson(sessionPath, SessionData.class);
                sessionData.setFilePath(Paths.get(sessionPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("Couldn't load a previous session. File not found: " + sessionPath);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Couldn't load a previous session. IO error.");
                return;
            } catch (JsonParseException e) {
                e.printStackTrace();
                System.err.println("Couldn't load a previous session. Invalid JSON format.");
                return;
            }

            createNewSession(sessionData, blueDiskette);
        }
    }

    private void saveLanguage() {
        try {
            Utility.saveTextFile(GUIConstants.getLastLanguageFilePath(), LocalizationProviderHTML.getInstance().getLanguage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLanguage() {
        List<String> lines;
        try {
            lines = Utility.readTextFileByLines(GUIConstants.getLastLanguageFilePath());
        } catch (IOException e) {
            return;
        }

        String previous_language = lines.get(0);
        List<String> supportedLangTags = Arrays.stream(GUIConstants.SUPPORTED_LANGUAGES)
                .map(t -> t.substring(0, 2).toLowerCase())
                .collect(Collectors.toList());

        if (supportedLangTags.contains(previous_language)) {
            LocalizationProviderHTML.getInstance().setLanguage(previous_language);
        }
    }

    public void showErrorMsg(String errorMsg) {
        JOptionPane.showMessageDialog(
                this,
                errorMsg,
                flp.getString(LocalizationKeys.ERROR_KEY),
                JOptionPane.ERROR_MESSAGE);
    }

    public void showWarningMsg(String warningMsg) {
        JOptionPane.showMessageDialog(
                this,
                warningMsg,
                flp.getString(LocalizationKeys.WARNING_KEY),
                JOptionPane.WARNING_MESSAGE);
    }

    public void showInfoMsg(String infoMsg) {
        JOptionPane.showMessageDialog(
                this,
                infoMsg,
                flp.getString(LocalizationKeys.NOTIFICATION_KEY),
                JOptionPane.INFORMATION_MESSAGE);
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

    private void noSessionsPresent() {
        sessionRequiredActions.forEach(a -> a.setEnabled(false));
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

    public Action getGenerateFromExpressionAction() {
        return generateFromExpressionAction;
    }

    public Action getGenerateFromTextAction() {
        return generateFromTextAction;
    }

    public Action getLoadTextAction() {
        return loadTextAction;
    }

    public Action getSaveTextAction() {
        return saveTextAction;
    }

    public Action getGenerateRandomFunctionAction() {
        return generateRandomFunctionAction;
    }

    public Action getDuplicateSelectedFunctionAction() {
        return duplicateSelectedFunctionAction;
    }

    public Action getRemoveSelectedFunctionAction() {
        return removeSelectedFunctionAction;
    }

    public Action getUndoRemoveFunctionAction() {
        return undoRemoveFunctionAction;
    }

    public Action getDisplayAllFunctionsAction() {
        return displayAllFunctionsAction;
    }

    public Action getSaveFunctionsAction() {
        return saveFunctionsAction;
    }

    public Action getLoadFunctionsAction() {
        return loadFunctionsAction;
    }

    public Action getGenerateFromFunctionsAction() {
        return generateFromFunctionsAction;
    }

    public Action getGenerateRandomVectorAction() {
        return generateRandomVectorAction;
    }

    public Action getDuplicateSelectedVectorAction() {
        return duplicateSelectedVectorAction;
    }

    public Action getRemoveSelectedVectorAction() {
        return removeSelectedVectorAction;
    }

    public Action getUndoRemoveVectorAction() {
        return undoRemoveVectorAction;
    }

    public Action getDisplayAllVectorsAction() {
        return displayAllVectorsAction;
    }

    public Action getSaveVectorsAction() {
        return saveVectorsAction;
    }

    public Action getLoadVectorsAction() {
        return loadVectorsAction;
    }

    public Action getGenerateSolvableAction() {
        return generateSolvableAction;
    }

    public Action getRunSolverAction() {
        return runSolverAction;
    }

    public Action getStopSolverAction() {
        return stopSolverAction;
    }

    public Action getClearOutputAction() {
        return clearOutputAction;
    }

    public Action getSolutionToExpressionAction() {
        return solutionToExpressionAction;
    }

    public Action getPrintSolutionAction() {
        return printSolutionAction;
    }

    public Action getRemoveSelectedSolutionAction() {
        return removeSelectedSolutionAction;
    }

    public Action getUndoRemoveSolutionAction() {
        return undoRemoveSolutionAction;
    }

    public Action getSaveSolutionsAction() {
        return saveSolutionsAction;
    }

    public Action getLoadSolutionsAction() {
        return loadSolutionsAction;
    }
}
