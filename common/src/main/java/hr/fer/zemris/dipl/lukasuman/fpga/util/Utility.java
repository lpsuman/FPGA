package hr.fer.zemris.dipl.lukasuman.fpga.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is a static Utility class which contains various useful methods. Some of the most used ones are:
 * argument checking, BitSet and Integer operations, String manipulation, etc...
 */
public class Utility {

    private static final String DEFAULT_STRING_LIST_DELIMITER = ", ";

    private Utility() {
    }

    public static <T> T checkNull(T obj, String name) {
        try {
            if (name == null) {
                return Objects.requireNonNull(obj, "Argument must not be null.");
            } else {
                return Objects.requireNonNull(obj, capitalizedString(name) + " must not be null.");
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static int checkRange(int value, int minValue, int maxValue) {
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException(String.format(
                    "Value %d is out of range (must be between %d and %d).", value, minValue, maxValue));
        }

        return value;
    }

    public static <T> T checkLimit(ArgumentLimit<T> limit, T arg) {
        String errorMsg = limit.testForLimit(arg);

        if (errorMsg != null) {
            throw new IllegalArgumentException(errorMsg);
        } else {
            return arg;
        }
    }

    public static <T extends Collection> T checkIfValidCollection(T col, String name) {
        checkNull(col, name);
        checkIfEmpty(col, name);
        checkIfContainsNull(col, name);
        return col;
    }

    public static <T extends Collection> T checkIfEmpty(T col, String name) {
        if (col.isEmpty()) {
            throw new IllegalArgumentException(capitalizedString(name) + " must not be empty.");
        }
        return col;
    }

    public static <T extends Collection> T checkIfContainsNull(T col, String name) {
        if (col.contains(null)) {
            throw new IllegalArgumentException(capitalizedString(name) + " must not contain null elements.");
        }
        return col;
    }

    public static <T> T[] checkIfValidArray(T[] array, String name) {
        checkNull(array, name);
        for (T item : array) {
            checkNull(item, "item in array");
        }
        return array;
    }

    public static String checkIfValidString(String str, String name) {
        checkNull(str, name);
        String result = str.trim();

        if (result.length() == 0) {
            throw new IllegalArgumentException(capitalizedString(name) + " must contain non-empty characters.");
        }

        return result;
    }

    public static BitSet[] newBitSetArray(int arraySize, int bitSetSize) {
        Utility.checkLimit(Constants.NUM_CLB_LIMIT, arraySize);
        Utility.checkLimit(Constants.BITSET_SIZE_LIMIT, bitSetSize);
        BitSet[] result = new BitSet[arraySize];

        for (int i = 0; i < arraySize; i++) {
            result[i] = new BitSet(bitSetSize);
        }

        return result;
    }

    public static void setBitSet(BitSet bitSet, int mask, int length) {
        Utility.checkNull(bitSet, "bitset");
        Utility.checkLimit(Constants.INTEGER_LENGTH_LIMIT, length);
        for (int i = 0; i < length; i++) {
            bitSet.set(i, testBitFromRight(mask, length - 1 - i));
        }
    }

    public static BitSet bitSetFromMask(int mask, int length) {
        Utility.checkLimit(Constants.INTEGER_LENGTH_LIMIT, length);
        BitSet result = new BitSet(length);
        setBitSet(result, mask, length);
        return result;
    }

    public static BitSet bitSetFromMask(String mask) {
        checkNull(mask, "mask");
        BitSet result = new BitSet(mask.length());
        for (int i = 0; i < mask.length(); i++) {
            result.set(i, mask.charAt(i) != '0');
        }
        return result;
    }

    public static String bitSetToString(BitSet bitSet) {
        checkNull(bitSet, "bitset");
        int length = bitSet.length();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }

        return sb.toString();
    }

    public static void appendBitSet(StringBuilder sb, BitSet bitSet, int length) {
        Utility.checkNull(bitSet, "bitset");

        for (int i = 0; i < length; i++) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
    }

    public static void appendStringList(StringBuilder sb, List<String> stringList, String delimiter) {
        checkNull(sb, "string builder");
        checkNull(stringList, "list of strings");
        checkNull(delimiter, "string list delimiter");
        stringList.forEach(str -> sb.append(str).append(delimiter));
        sb.setLength(sb.length() - delimiter.length());
    }

    public static void appendStringList(StringBuilder sb, List<String> stringList) {
        appendStringList(sb, stringList, DEFAULT_STRING_LIST_DELIMITER);
    }

    public static String toBinaryString(int value, int numBits, int numSpaces) {
        StringBuilder sb = new StringBuilder();
        String strWithoutSpaces = toBinaryString(value, numBits);
        if (numSpaces < 1) {
            return strWithoutSpaces;
        }

        for (int i = 0, n = strWithoutSpaces.length(); i < n; i++) {
            sb.append(paddedChar(strWithoutSpaces.charAt(i), Constants.BOOL_VECTOR_PRINT_CELL_SIZE));
        }

        return sb.toString();
    }

    public static String toBinaryString(int value, int numBits) {
        return String.format("%" + numBits + "s", Integer.toBinaryString(value)).replace(' ', '0');
    }

    public static String capitalizedString(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String paddedString(String str, int length) {
        int paddingLength = length - str.length();

        if (paddingLength < 1) {
            return str;
        }

        return String.format("%" + length + "s", str);
    }

    public static String paddedChar(char c, int length) {
        return String.format("%" + length + "c", c);
    }

    public static boolean isPowerOfTwo(int number) {
        return number > 0 && ((number & (number - 1)) == 0);
    }

    public static int lowestPowerOfTwo(int number) {
        if (number < 1) {
            return -1;
        }

        for (int i = 0; i < 32; i++) {
            if ((number & 1) == 1) {
                return i;
            }

            number >>= 1;
        }

        return -1;
    }

    public static boolean testBitFromLeft(int target, int index) {
        if (index < 0 || index > 31) {
            throw new IllegalArgumentException("Invalid index for testing integer bit: " + index);
        }
        return (target & (1 << (31 - index))) != 0;
    }

    public static boolean testBitFromRight(int target, int index) {
        if (index < 0 || index > 31) {
            throw new IllegalArgumentException("Invalid index for testing integer bit: " + index);
        }
        return (target & (1 << index)) != 0;
    }

    public static int setBitFromRight(int target, int index, boolean value) {
        if (index < 0 || index > 31) {
            throw new IllegalArgumentException("Invalid index for setting integer bit: " + index);
        }
        if (value) {
            return target | (1 << index);
        } else {
            return target & ~(1 << index);
        }
    }

    public static InputStream getInputStreamFromString(String str) {
        return new ByteArrayInputStream(str.getBytes(Charset.forName("UTF-8")));
    }

    public static int findIndexOfSubArray(int[] haystack, int[] needle) {
        return findIndexOfSubArray(haystack, needle, computePrefixArray(needle));
    }

    public static int findIndexOfSubArray(int[] haystack, int[] needle, int[] prefixArray) {
        // Knuth-Morris-Pratt

        Utility.checkNull(haystack, "array for searching");
        Utility.checkNull(needle, "array to be searched");
        Utility.checkNull(prefixArray, "prefix array");

        int n = haystack.length;
        int m = needle.length;
        int q = 0;

        for (int i = 0; i < n; i++) {
            while (q > 0 && needle[q] != haystack[i]) {
                q = prefixArray[q - 1];
            }

            if (needle[q] == haystack[i]) {
                ++q;
            }

            if (q == m) {
                return i - m + 1;
            }
        }

        return -1;
    }

    public static int[] computePrefixArray(int[] needle) {
        Utility.checkNull(needle, "array");

        if (needle.length == 0) {
            throw new IllegalArgumentException("Can't calculate a prefix array from an empty array.");
        }

        int m = needle.length;
        int[] prefixFunction = new int[m];
        int k = 0;

        for (int q = 1; q < m; ++q) {
            while (k > 0 && needle[k] != needle[q]) {
                k = prefixFunction[k - 1];
            }

            if (needle[k] == needle[q]) {
                ++k;
            }

            prefixFunction[q] = k;
        }

        return prefixFunction;
    }

    public static Set<Integer> generateRangeSet(int fromIndex, int toIndex) {
        return IntStream.range(fromIndex, toIndex).boxed().collect(Collectors.toSet());
    }

    public static Integer[] generateRangeArray(int fromIndex, int toIndex) {
        return IntStream.range(fromIndex, toIndex).boxed().toArray(Integer[]::new);
    }

    public static List<String> readTextFileByLines(String filePath) throws IOException {
        checkIfValidString(filePath, "file path");
        List<String> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (!line.isEmpty()) {
                    result.add(line);
                }
            }
        }

        return result;
    }

    public static String readTextFile(String filePath) throws IOException {
        return String.join("\n", readTextFileByLines(filePath));
    }

    public static void saveTextFile(String filePath, String text) throws IOException {
        saveTextFile(filePath, Arrays.asList(text.split("\\r?\\n")));
    }

    public static void saveTextFile(String filePath, List<String> lines) throws IOException {
        checkIfValidString(filePath, "file path");
        checkIfValidCollection(lines, "list of lines");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                line = line.trim();

                if (!line.isEmpty()) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }

    public static List<String> breakIntoWords(String text) {
        return Arrays.asList(text.split("\\s+"));
    }

    public static List<String> breakIntoWords(List<String> lines) {
        checkIfValidCollection(lines, "list of lines");
        List<String> words = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] lineParts = line.split("\\s+");
            words.addAll(Arrays.asList(lineParts));
        }

        if (words.isEmpty()) {
            return null;
        }

        return words;
    }

    public static boolean isValidInputID(String str) {
        if (isBinaryChar(str.charAt(0))) {
            if (str.length() == 1) {
                return false;
            }

            boolean containsNonBinary = false;
            for (int i = 0; i < str.length(); i++) {
                if (!isBinaryChar(str.charAt(i))) {
                    containsNonBinary = true;
                    break;
                }
            }

            if (!containsNonBinary) {
                return false;
            }
        }

        if (str.length() > Constants.MAXIMUM_INPUT_ID_LENGTH) {
            throw new IllegalArgumentException(String.format(
                    "Input %s has a too long name (length %d while %d is maximum.",
                    str, str.length(), Constants.MAXIMUM_INPUT_ID_LENGTH));
        }

        return true;
    }

    public static boolean isBinaryChar(char c) {
        return c == '0' || c == '1';
    }

    public static BitSet bitSetFromString(String str) {
        Utility.checkIfValidString(str, "string for the truth table");
        BitSet bitSet = new BitSet(str.length());

        for (int i = 0; i < str.length(); i++) {
            if (!isBinaryChar(str.charAt(i))) {
                throw new IllegalArgumentException(String.format("Can't convert string %s into a truth table (0 or 1).", str));
            }

            bitSet.set(i, str.charAt(i) == '1');
        }

        return bitSet;
    }

    public static String getWorkingDir() {
        return System.getProperty("user.dir");
    }
}
