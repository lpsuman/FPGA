package hr.fer.zemris.dipl.lukasuman.fpga;

import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestUtil {

    public static void argThrow(Executable runnable) {
        assertThrows(IllegalArgumentException.class, runnable);
    }
}
