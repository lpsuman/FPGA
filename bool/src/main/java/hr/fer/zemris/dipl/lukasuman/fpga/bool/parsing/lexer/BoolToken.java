package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.Objects;

/**
 * A {@link Token} which stores a {@link BoolTokenType} and a string value.
 */
public class BoolToken implements Token<BoolTokenType, String> {

    private BoolTokenType type;
    private String value;

    public BoolToken(BoolTokenType type, String value) {
        Utility.checkNull(type, "token type");

        if (type == BoolTokenType.WORD) {
            if (value == null) {
                throw new IllegalArgumentException("Token type WORD requires a value.");
            }

            value = value.trim();

            if (value.isEmpty()) {
                throw new IllegalArgumentException("Token type WORD requires a non empty value.");
            }
        }

        this.type = type;
        this.value = value;
    }

    @Override
    public BoolTokenType getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoolToken boolToken = (BoolToken) o;
        return type == boolToken.type &&
                Objects.equals(value, boolToken.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
