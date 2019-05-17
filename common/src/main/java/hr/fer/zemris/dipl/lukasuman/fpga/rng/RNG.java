package hr.fer.zemris.dipl.lukasuman.fpga.rng;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.NoSuchFileException;
import java.util.Properties;

public class RNG {

    private static final String RNG_CONFIG_FILE_NAME = "rng-config.properties";
    private static final String RNG_PROVIDER_PROPERTY_KEY = "rng-provider";

    private static RNGProvider rngProvider;

    static {
        InputStream inputStream = RNG.class.getClassLoader().getResourceAsStream(RNG_CONFIG_FILE_NAME);
        if (inputStream == null) {
            throw new RuntimeException(
                    new NoSuchFileException("RNG config file " + RNG_CONFIG_FILE_NAME + " not found on the classpath.")
            );
        }

        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String className = (String) properties.get(RNG_PROVIDER_PROPERTY_KEY);
        try {
            Class<?> providerClass = RNG.class.getClassLoader().loadClass(className);
            rngProvider = (RNGProvider) providerClass.getDeclaredConstructor().newInstance();

        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);

        } catch (ClassCastException e) {
            throw new RuntimeException(className + " class does not implement " + IRNG.class.getSimpleName(), e);
        }
    }

    public static IRNG getRNG() {
        return rngProvider.getRNG();
    }
}
