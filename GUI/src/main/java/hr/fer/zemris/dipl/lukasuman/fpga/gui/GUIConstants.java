package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GUIConstants {

    private GUIConstants() {
    }

    public static final String DEFAULT_APPLICATION_NAME = "JFPGA";
    public static final int DEFAULT_WINDOW_HEIGHT = 800;
    public static final double WINDOW_WIDTH_RATIO = 1.5;
    public static final int DEFAULT_WINDOW_WIDTH = (int)(DEFAULT_WINDOW_HEIGHT * WINDOW_WIDTH_RATIO);
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int DEFAULT_WINDOW_LOCATION_X = (SCREEN_SIZE.width - DEFAULT_WINDOW_WIDTH) / 2;
    public static final int DEFAULT_WINDOW_LOCATION_Y = (SCREEN_SIZE.height - DEFAULT_WINDOW_HEIGHT) / 2;
    public static final boolean REDIRECT_OUT = false;
    public static final boolean SHOW_ERRORS_IN_GUI = false;

    public static final String DEFAULT_LANGUAGE = "en";
    public static final String[] SUPPORTED_LANGUAGES = {"English", "Hrvatski"};
    public static final String TRANSLATION_BUNDLE_PATH = "translations";
    public static final String SESSIONS_FOLDER = "/GUI/data/sessions";
    public static final String LAST_SESSIONS_FILE_NAME = "last_sessions.txt";
    public static final String LAST_LANGUAGE_FILE_NAME = "language.txt";

    public static String getDataDirectory() {
        String currentDir = Utility.getWorkingDir() + SESSIONS_FOLDER;
        if (!Files.exists(Paths.get(currentDir))) {
            currentDir = Utility.getWorkingDir();
        }
        return currentDir + "/";
    }

    public static String getLastSessionsFilePath() {
        return getDataDirectory() + LAST_SESSIONS_FILE_NAME;
    }

    public static String getLastLanguageFilePath() {
        return getDataDirectory() + LAST_LANGUAGE_FILE_NAME;
    }

    public static final Dimension CLOSE_BUTTON_SIZE = new Dimension(22, 22);
    public static final Dimension ICON_SIZE = new Dimension(18, 18);

    public static final int DEFAULT_BORDER_SIZE = 3;
    public static final int DEFAULT_LABEL_BORDER_SIZE = 3;
    public static final int EXPRESSION_TEXT_AREA_ROWS = 4;
    public static final int SHOW_EXPRESSION_TEXT_AREA_ROWS = EXPRESSION_TEXT_AREA_ROWS;
    public static final int MINIMUM_COLUMN_WIDTH = 20;
    public static final double COMBO_BOX_WIDTH_WEIGHT = 0.5;
    public static final double FORMATTED_TEXT_FIELD_WIDTH_WEIGHT = 0.4;
    public static final double CHECK_BOX_WIDTH_WEIGHT = 0.4;

    public static final boolean SHOW_CONFIRM_EXIT_DIALOG = false;
    public static final boolean SHOW_DUPLICATE_BUTTONS = false;
    public static final boolean SHOW_REMOVE_BUTTONS = true;

    public static final int DEFAULT_NUM_CLB_INPUTS_COMBOBOX_INDEX = 1;
    public static final int DEFAULT_SOLVE_MODE_COMBOBOX_INDEX = 1;

    public static final String DUPLICATE_NAME_SUFFIX = "_copy";
    public static final String RANDOM_NAME = "random";
}