package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators.*;

public class BoolOperatorFactory {

    private static SimpleGenericFactory<BoolOperator> genericFactory;

    static {
        BoolOperatorFactory.getGenericFactory().register(NotOperator.class, NotOperator.NAMES);
        BoolOperatorFactory.getGenericFactory().register(AndOperator.class, AndOperator.NAMES);
        BoolOperatorFactory.getGenericFactory().register(OrOperator.class, OrOperator.NAMES);
        BoolOperatorFactory.getGenericFactory().register(XorOperator.class, XorOperator.NAMES);

        BoolOperatorFactory.getGenericFactory().register(NotAndOperator.class, NotAndOperator.NAMES);
        BoolOperatorFactory.getGenericFactory().register(NotOrOperator.class, NotOrOperator.NAMES);
        BoolOperatorFactory.getGenericFactory().register(NotXorOperator.class, NotXorOperator.NAMES);

        BoolOperatorFactory.getGenericFactory().registerDefault(SimpleInputOperator.class);
    }

    private BoolOperatorFactory() {
    }

    public static GenericFactory<BoolOperator> getGenericFactory() {
        if (genericFactory == null) {
            genericFactory = new SimpleGenericFactory<>();
        }
        return genericFactory;
    }
}
