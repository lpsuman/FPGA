package hr.fer.zemris.dipl.lukasuman.fpga;

import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtil {

    public static void argThrow(Executable runnable) {
        assertThrows(IllegalArgumentException.class, runnable);
    }

    public static void testClone(Object original, Object clone) {
        assertNotSame(original, clone);
        assertEquals(original.getClass(), clone.getClass());
        assertEquals(original, clone);
    }
}
