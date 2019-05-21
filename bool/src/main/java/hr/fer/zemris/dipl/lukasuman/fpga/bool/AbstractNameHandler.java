package hr.fer.zemris.dipl.lukasuman.fpga.bool;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

/**
 * This class can be extended in order to provide storage of a name string.
 */
public abstract class AbstractNameHandler {

    private String name;

    public AbstractNameHandler(String name) {
        this.name = Utility.checkIfValidString(name, getNameMessage());
    }

    private String getNameMessage() {
        return getClass().getSimpleName() + "'s name";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Utility.checkIfValidString(name, getNameMessage());
    }
}
