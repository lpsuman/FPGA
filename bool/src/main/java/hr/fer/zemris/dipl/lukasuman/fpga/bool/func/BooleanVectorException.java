package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

public class BooleanVectorException extends RuntimeException {

    private String causeTarget;

    public BooleanVectorException(String message, String causeTarget) {
        super(message);
        this.causeTarget = causeTarget;
    }

    public String getCauseTarget() {
        return causeTarget;
    }
}
