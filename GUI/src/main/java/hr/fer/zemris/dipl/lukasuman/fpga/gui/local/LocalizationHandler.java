package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

public class LocalizationHandler {

    /**The localization key.*/
    private String key;

    /**The localization provider.*/
    private LocalizationProvider lp;

    public LocalizationHandler(String key, LocalizationProvider lp, LocalizationListener listener) {
        this.key = Utility.checkNull(key, "localization key");
        this.lp = Utility.checkNull(lp, "localization provider");
        lp.addLocalizationListener(Utility.checkNull(listener, "localization listener"));
    }

    public String getKey() {
        return key;
    }

    public LocalizationProvider getLp() {
        return lp;
    }

    public String getString() {
        return lp.getString(key);
    }
}
