package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import java.awt.*;

public class GUIConstants {

    private GUIConstants() {
    }

    public static final String DEFAULT_APPLICATION_NAME = "JFPGA";
    public static final int DEFAULT_WINDOW_WIDTH = 600;
    public static final int DEFAULT_WINDOW_HEIGHT = 900;
    public static final int DEFAULT_WINDOW_LOCATION_X = (1920 - DEFAULT_WINDOW_WIDTH) / 2;
    public static final int DEFAULT_WINDOW_LOCATION_Y = (1080 - DEFAULT_WINDOW_HEIGHT) / 2;

    public static final String DEFAULT_LANGUAGE = "en";
    public static final String[] SUPPORTED_LANGUAGES = {"English", "Hrvatski"};
    public static final String TRANSLATION_BUNDLE_PATH = "translations";
    public static final String DATA_ABSOLUTE_PATH = "C:/FER/Diplomski/FPGA/GUI/data/sessions";
    public static final String PREVIOUS_SESSIONS_FILE_PATH = DATA_ABSOLUTE_PATH + "\\last_session.txt";

    public static final Dimension CLOSE_BUTTON_SIZE = new Dimension(22, 22);
    public static final Dimension ICON_SIZE = new Dimension(18, 18);

    public static final int DEFAULT_BORDER_SIZE = 3;
    public static final int DEFAULT_LABEL_BORDER_SIZE = 3;
    public static final int EXPRESSION_TEXT_AREA_ROWS = 4;
    public static final int MINIMUM_COLUMN_WIDTH = 20;
}