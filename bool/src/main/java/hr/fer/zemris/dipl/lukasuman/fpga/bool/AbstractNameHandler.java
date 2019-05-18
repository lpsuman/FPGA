package hr.fer.zemris.dipl.lukasuman.fpga.bool;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

public abstract class AbstractNameHandler {

    private String name;

    public AbstractNameHandler(String name) {
        this.name = Utility.checkIfValidString(name, getNameMessage());
    }

    protected abstract String getNameMessage();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Utility.checkIfValidString(name, getNameMessage());
    }
}
