package hr.fer.zemris.dipl.lukasuman.fpga.bool;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;

/**
 * This class can be extended in order to provide storage of a name string.
 */
public abstract class AbstractNameHandler implements Serializable {

    private static final long serialVersionUID = 7073824127051413762L;

    private String name;

    public AbstractNameHandler(String name) {
        setName(name);
    }

    private String getNameMessage() {
        return getClass().getSimpleName() + "'s name";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Utility.checkIfValidString(name, getNameMessage());
        if (name.length() > Constants.MAXIMUM_NAME_LENGTH) {
            throw new IllegalArgumentException(String.format("Name %s is too long (length %d while %d is maximum).",
                    name, name.length(), Constants.MAXIMUM_NAME_LENGTH));
        }
    }
}
