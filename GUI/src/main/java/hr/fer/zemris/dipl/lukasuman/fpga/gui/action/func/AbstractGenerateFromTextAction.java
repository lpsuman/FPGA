package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public abstract class AbstractGenerateFromTextAction extends AbstractAppAction {

    private Supplier<String> textProvider;

    public AbstractGenerateFromTextAction(JFPGA jfpga, Supplier<String> textProvider, String localizationKey) {
        super(jfpga, localizationKey);
        this.textProvider = Utility.checkNull(textProvider, "text provider");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String text = textProvider.get();
        if (text == null || text.isEmpty()) {
            return;
        }

        doAction(text);
    }

    protected abstract void doAction(String text);
}
