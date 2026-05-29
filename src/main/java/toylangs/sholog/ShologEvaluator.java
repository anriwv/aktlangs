package toylangs.sholog;

import toylangs.sholog.ast.*;

import java.util.Map;

public class ShologEvaluator {

    public static class ShologException extends RuntimeException {
        private final int code;

        public ShologException(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return "code " + code;
        }
    }

    /**
     * Defineerimata muutuja veakood.
     */
    private static final int UNDEFINED_CODE = 127;

    private final Map<String, Boolean> env;

    private ShologEvaluator(Map<String, Boolean> env) {
        this.env = env;
    }

    public static boolean eval(ShologNode node, Map<String, Boolean> env) {
        ShologEvaluator shologEvaluator = new ShologEvaluator(env);
        return shologEvaluator.eval(node);
    }

    private boolean eval(ShologNode node) {
        return switch (node) {
            case ShologLit(boolean value) -> value;
            case ShologVar(String name) -> {
                Boolean value = env.get(name);
                if (value == null)
                    throw new ShologException(UNDEFINED_CODE);
                else
                    yield value;
            }
            case ShologError(int code) -> throw new ShologException(code);
            case ShologEager(ShologEager.Op op, ShologNode left, ShologNode right) -> {
                boolean leftValue = eval(left);
                boolean rightValue = eval(right);
                yield switch (op) {
                    case And -> leftValue && rightValue;
                    case Or -> leftValue || rightValue;
                    case Xor -> leftValue ^ rightValue;
                };
            }
            case ShologLazy(ShologLazy.Op op, ShologNode left, ShologNode right) -> {
                boolean leftValue = eval(left);
                yield switch (op) {
                    case And -> leftValue && eval(right);
                    case Or -> leftValue || eval(right);
                };
            }
        };
    }
}
