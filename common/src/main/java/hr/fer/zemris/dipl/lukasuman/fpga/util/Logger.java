package hr.fer.zemris.dipl.lukasuman.fpga.util;

import java.util.List;

public interface Logger {

    void setLogging(boolean isEnabled);
    void log(String str);
    void log(char c);
    String getLog();
    void resetLog();
    void logCheckpoint();
    List<String> getCheckpoints();
    void resetCheckpoints();
}
