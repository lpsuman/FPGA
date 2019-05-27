package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.SetListener;

import java.awt.event.ActionEvent;

public class LoadTextAction extends AbstractAppAction {

    private SetListener<TextLoadListener> listeners;

    public LoadTextAction(JFPGA jfpga, String localizatioinKey, TextLoadListener listener) {
        super(jfpga, localizatioinKey);
        listeners = new SetListener<>();
        addTextLoadListener(listener);
    }

    public void addTextLoadListener(TextLoadListener listener) {
        listeners.addListener(listener);
    }

    public void removeTextLoadListener(TextLoadListener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //TODO load text action
    }
}
