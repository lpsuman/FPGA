package hr.fer.zemris.dipl.lukasuman.fpga.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.IOException;
import java.util.BitSet;
import java.util.List;

public class MyTypeAdapters {

    private static final TypeAdapter<BitSet> BIT_SET = new TypeAdapter<>() {

        @Override public void write(JsonWriter jsonWriter, BitSet src) throws IOException {
            jsonWriter.value(Utility.bitSetToString(src));
        }
        @Override public BitSet read(JsonReader jsonReader) throws IOException {
            return Utility.bitSetFromMask(jsonReader.nextString());
        }
    };

    private static final TypeAdapter<BooleanFunction> BOOLEAN_FUNCTION = new TypeAdapter<>() {

        private static final String NAME = "name";
        private static final String INPUTS = "inputs";
        private static final String TRUTHTABLE = "truthTable";

        @Override
        public void write(JsonWriter jsonWriter, BooleanFunction booleanFunction) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name(NAME).value(booleanFunction.getName());
            jsonWriter.name(INPUTS);
            getGson().getAdapter(List.class).write(jsonWriter, booleanFunction.getInputIDs());
            jsonWriter.name(TRUTHTABLE);
            getGson().getAdapter(BitSet.class).write(jsonWriter, booleanFunction.getTruthTable());
            jsonWriter.endObject();
        }

        @Override
        public BooleanFunction read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            List<String> inputIDs = null;
            BitSet truthTable = null;
            String name = null;
            for (int i = 0; i < 3; i++) {
                String fieldName = jsonReader.nextName();
                switch (fieldName) {
                    case NAME:
                        name = jsonReader.nextString();
                        break;
                    case INPUTS:
                        inputIDs = (List<String>) getGson().getAdapter(List.class).read(jsonReader);
                        break;
                    case TRUTHTABLE:
                        truthTable = getGson().getAdapter(BitSet.class).read(jsonReader);
                        break;
                    default:
                        throw new JsonParseException(String.format(
                                "Unknown field %s for boolean function in JSON.", fieldName));
                }
            }
            jsonReader.endObject();

            if (inputIDs == null) {
                throw new JsonParseException(String.format("Field %s missing for boolean function.", INPUTS));
            } else if (truthTable == null) {
                throw new JsonParseException(String.format("Field %s missing for boolean function.", TRUTHTABLE));
            } else if (name == null) {
                return new BooleanFunction(inputIDs, truthTable);
            } else {
                return new BooleanFunction(inputIDs, truthTable, name);
            }
        }
    };

    private static final GsonBuilder GSON_BUILDER = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(BitSet.class, BIT_SET)
            .registerTypeAdapter(BooleanFunction.class, BOOLEAN_FUNCTION);

    public static Gson getGson() {
        return GSON_BUILDER.create();
    }
}
