package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.BoolExpression;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolToken;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolTokenType;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.Lexer;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.operators.BoolOperator;

import java.util.*;

public class BoolParser implements Parser<BoolToken, BoolExpression> {

    private LinkedList<BoolOperator> stack;
    private Stack<BoolOperator> roots;
    private Stack<Integer> parenthesesElementsCount;
    private boolean isParenthesesJustClosed;

    private List<BoolToken> tokens;
    private BoolExpression boolExpression;

    public BoolParser() {
        stack = new LinkedList<>();
        roots = new Stack<>();
        parenthesesElementsCount = new Stack<>();
        tokens = new ArrayList<>();
    }

    @Override
    public BoolExpression parse(Lexer<BoolToken> lexer) {
        reset();

        Set<String> sortedInputIDs = new TreeSet<>();

        while (true) {
            BoolToken token = lexer.nextToken();
            tokens.add(token);

            if (token.getType() == BoolTokenType.WORD) {
                String value = token.getValue();
                if (!BoolOperatorFactory.getGenericFactory().isMappingPresent(value)) {
                    sortedInputIDs.add(value);
                }
            } else if (token.getType() == BoolTokenType.EOF) {
                if (tokens.size() == 1) {
                    throw new BoolParserException("Empty input is not allowed.");
                }
                break;
            }
        }

        if (sortedInputIDs.isEmpty()) {
            throw new BoolParserException("No inputs in expression.");
        }

        boolExpression = new BoolExpression(new ArrayList<>(sortedInputIDs));
        roots.push(null);
        parenthesesElementsCount.push(0);

        loop:
        for (BoolToken token : tokens) {
            switch (token.getType()) {
                case WORD:
                    parseWord(token.getValue().toLowerCase());
                    break;
                case PARENTHESIS_LEFT:
                    openParenthesis();
                    break;
                case PARENTHESIS_RIGHT:
                    closeParenthesis();
                    break;
                case EOL:
                    endOfLine();
                    break;
                case EOF:
                    endOfFile();
                    break loop;
                default:
                    throw new BoolParserException("Unexpected token type encountered.");
            }
        }

        BoolOperator root = roots.pop();

        if (!root.isUsingLeft() && !root.isUsingRight()) {
            throw new BoolParserException("A literal is not an expression.");
        }

        try {
            boolExpression.setRoot(root);
            boolExpression.evaluate(0);
        } catch (IllegalArgumentException e) {
            throw new BoolParserException(e.getMessage());
        }

        return boolExpression;
    }

    private void parseWord(String value) {
        BoolOperator newOperator;

        if (BoolOperatorFactory.getGenericFactory().isMappingPresent(value)) {
            newOperator = BoolOperatorFactory.getGenericFactory().getForName(value);
        } else {
            newOperator = BoolOperatorFactory.getGenericFactory().getForName(value, boolExpression, value);
        }

        if (newOperator == null) {
            throw new BoolParserException("Unknown word encountered.");
        }

        pushOperator(newOperator);
    }

    private void pushOperator(BoolOperator newOperator) {
        stack.push(newOperator);
        int oldCount = parenthesesElementsCount.pop();
        parenthesesElementsCount.push(oldCount + 1);

        if (roots.peek() == null) {
            if (newOperator.isUsingLeft()) {
                throw new BoolParserException("Operator at the start of an expression (after parenthesis) has no left operand.");
            }

            roots.pop();
            roots.push(newOperator);
            return;
        }

        BoolOperator operator = stack.get(1);

        if (operator.isUsingRight() && newOperator.isUsingLeft() && !isParenthesesJustClosed) {
                throw new BoolParserException(String.format("Two consecutive operators (%s and %s).", operator, newOperator));
        }

        if (!operator.isUsingRight() && !newOperator.isUsingLeft() && !isParenthesesJustClosed) {
                throw new BoolParserException("Literal before NOT.");
        }

        if (!newOperator.isUsingLeft()) {
            if (isParenthesesJustClosed) {
                throw new BoolParserException("Parentheses before NOT.");
            }

            operator.setRight(newOperator);
            return;
        }

        while (true) {
            BoolOperator parent = operator.getParent();

            if (parent == null) {
                if (newOperator.compareTo(operator) <= 0 || isParenthesesJustClosed){
                    roots.pop();
                    roots.push(newOperator);
                }

                newOperator.setLeft(operator);
                isParenthesesJustClosed = false;
                return;
            } else if (newOperator.compareTo(parent) > 0) {
                newOperator.setLeft(operator);
                parent.setRight(newOperator);
                return;
            } else {
                operator = parent;
            }
        }
    }

    private void openParenthesis() {
        roots.push(null);
        parenthesesElementsCount.push(0);
    }

    private void closeParenthesis() {
        isParenthesesJustClosed = true;
        int numElementsEnclosed = parenthesesElementsCount.pop();

        if (parenthesesElementsCount.empty()) {
            throw new BoolParserException("Reached closing parenthesis with no other to close.");
        }

        if (numElementsEnclosed < 2) {
            throw new BoolParserException("Parentheses closed with less than 2 elements inside.");
        }

        popElements(numElementsEnclosed);
        BoolOperator nestedRoot = roots.pop();
        stack.push(nestedRoot);
        int oldCount = parenthesesElementsCount.pop();
        parenthesesElementsCount.push(oldCount + 1);

        BoolOperator currentRoot = roots.pop();

        if (currentRoot == null) {
            roots.push(nestedRoot);
        } else {
            roots.push(currentRoot);
        }

        if (oldCount == 0) {
            return;
        }

        BoolOperator prevOperator = stack.get(1);

        if (!prevOperator.isUsingRight()) {
            throw new BoolParserException("Literal before parentheses: " + prevOperator);
        }

        prevOperator.setRight(nestedRoot);
    }

    private void endOfLine() {
        // ignore
    }

    private void endOfFile() {
        if (parenthesesElementsCount.empty()) {
            throw new BoolParserException("EOF reached with one extra closing parenthesis.");
        }

        int numEnclosedElements = parenthesesElementsCount.pop();

        if (!parenthesesElementsCount.empty()) {
            throw new BoolParserException("EOF reached with too many open parentheses left.");
        }

        popElements(numEnclosedElements);
    }

    private void reset() {
        stack.clear();
        roots.clear();
        parenthesesElementsCount.clear();
        tokens.clear();
        isParenthesesJustClosed = false;
    }

    private void popElements(int count) {
        for (int i = 0; i < count; ++i) {
            stack.pop();
        }
    }
}
