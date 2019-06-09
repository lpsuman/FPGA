package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class MyGson {

    private static final String UNKNOWN_FIELD_ERROR_MSG = "Unknown field %s for %s in JSON.";
    private static final String MISSING_FIELD_ERROR_MSG = "Field %s for %s is missing in JSON.";

    private static final TypeAdapter<BitSet> BIT_SET_TYPE_ADAPTER = new TypeAdapter<>() {

        @Override public void write(JsonWriter jsonWriter, BitSet src) throws IOException {
            jsonWriter.value(Utility.bitSetToString(src));
        }

        @Override public BitSet read(JsonReader jsonReader) throws IOException {
            return Utility.bitSetFromMask(jsonReader.nextString());
        }
    };

    private static final TypeAdapter<BooleanFunction> BOOLEAN_FUNCTION_TYPE_ADAPTER = new TypeAdapter<>() {

        private static final String CLASS_NAME = "boolean function";
        private static final String NAME = "name";
        private static final String EXPRESSION = "expression";
        private static final String INPUTS = "inputs";
        private static final String TRUTHTABLE = "truthTable";

        @Override
        public void write(JsonWriter jsonWriter, BooleanFunction booleanFunction) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name(NAME).value(booleanFunction.getName());
            if (booleanFunction.getExpressionGeneratedFrom() != null) {
                jsonWriter.name(EXPRESSION).value(booleanFunction.getExpressionGeneratedFrom());
            }
            jsonWriter.name(INPUTS);
            getGson().getAdapter(List.class).write(jsonWriter, booleanFunction.getInputIDs());
            jsonWriter.name(TRUTHTABLE);
            getGson().getAdapter(BitSet.class).write(jsonWriter, booleanFunction.getTruthTable());
            jsonWriter.endObject();
        }

        @Override
        public BooleanFunction read(JsonReader jsonReader) throws IOException {
            String name = null;
            String expression = null;
            List<String> inputIDs = null;
            BitSet truthTable = null;
            jsonReader.beginObject();
            while (jsonReader.peek() != JsonToken.END_OBJECT) {
                String fieldName = jsonReader.nextName();
                switch (fieldName) {
                    case NAME:
                        name = jsonReader.nextString();
                        break;
                    case EXPRESSION:
                        expression = jsonReader.nextString();
                        break;
                    case INPUTS:
                        inputIDs = (List<String>) getGson().getAdapter(List.class).read(jsonReader);
                        break;
                    case TRUTHTABLE:
                        truthTable = getGson().getAdapter(BitSet.class).read(jsonReader);
                        break;
                    default:
                        throw new JsonParseException(String.format(
                                UNKNOWN_FIELD_ERROR_MSG, fieldName, CLASS_NAME));
                }
            }
            jsonReader.endObject();

            BooleanFunction func;
            if (inputIDs == null) {
                throw new JsonParseException(String.format(MISSING_FIELD_ERROR_MSG, INPUTS, CLASS_NAME));
            } else if (truthTable == null) {
                throw new JsonParseException(String.format(MISSING_FIELD_ERROR_MSG, TRUTHTABLE, CLASS_NAME));
            } else if (name == null) {
                func = new BooleanFunction(inputIDs, truthTable);
            } else {
                func = new BooleanFunction(inputIDs, truthTable, name);
            }

            func.setExpressionGeneratedFrom(expression);
            return func;
        }
    };

    private static final TypeAdapter<BooleanVector> BOOLEAN_VECTOR_TYPE_ADAPTER = new TypeAdapter<>() {

        private static final String CLASS_NAME = "boolean vector";
        private static final String NAME = "name";
        private static final String FUNCTIONS = "functions";

        @Override
        public void write(JsonWriter jsonWriter, BooleanVector booleanVector) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name(NAME).value(booleanVector.getName());
            jsonWriter.name(FUNCTIONS);
            jsonWriter.beginArray();
            for (BooleanFunction boolFunction : booleanVector.getBoolFunctions()) {
                getGson().getAdapter(BooleanFunction.class).write(jsonWriter, boolFunction);
            }
            jsonWriter.endArray();
            jsonWriter.endObject();
        }

        @Override
        public BooleanVector read(JsonReader jsonReader) throws IOException {
            String name = null;
            List<BooleanFunction> functions = new ArrayList<>();
            jsonReader.beginObject();
            while (jsonReader.peek() != JsonToken.END_OBJECT) {
                String fieldName = jsonReader.nextName();
                switch (fieldName) {
                    case NAME:
                        name = jsonReader.nextString();
                        break;
                    case FUNCTIONS:
                        jsonReader.beginArray();
                        while (jsonReader.peek() != JsonToken.END_ARRAY) {
                            functions.add(getGson().getAdapter(BooleanFunction.class).read(jsonReader));
                        }
                        jsonReader.endArray();
                        break;
                    default:
                        throw new JsonParseException(String.format(
                                UNKNOWN_FIELD_ERROR_MSG, fieldName, CLASS_NAME));
                }
            }
            jsonReader.endObject();

            if (functions.isEmpty()) {
                throw new JsonParseException(String.format(MISSING_FIELD_ERROR_MSG, FUNCTIONS, CLASS_NAME));
            } else if (name == null) {
                return new BooleanVector(functions);
            } else {
                return new BooleanVector(functions, name);
            }
        }
    };

    private static final GsonBuilder GSON_BUILDER = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(BitSet.class, BIT_SET_TYPE_ADAPTER)
            .registerTypeAdapter(BooleanFunction.class, BOOLEAN_FUNCTION_TYPE_ADAPTER)
            .registerTypeAdapter(BooleanVector.class, MyGson.BOOLEAN_VECTOR_TYPE_ADAPTER);

    public static void registerTypeAdapter(Type type, Object typeAdapter) {
        GSON_BUILDER.registerTypeAdapter(type, typeAdapter);
    }

    public static Gson getGson() {
        return GSON_BUILDER.create();
    }

    public static <T> T readFromJson(String filePath, Class<T> classOfData) throws IOException {
        return MyGson.getGson().fromJson(Utility.readTextFile(filePath), classOfData);
    }

    public static <T> void writeToJson(String filePath, T data, Class<T> classOfData) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            MyGson.getGson().toJson(data, classOfData, fileWriter);
        }
    }
}
