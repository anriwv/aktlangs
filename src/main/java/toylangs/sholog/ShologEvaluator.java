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


    public static boolean eval(ShologNode node, Map<String, Boolean> env) {
        return new ShologAstVisitor<Boolean>() {
            @Override
            protected Boolean visit(ShologLit lit) {
                return lit.getValue();
            }

            @Override
            protected Boolean visit(ShologVar var) {
                Boolean value = env.get(var.getName());
                if (value == null)
                    throw new ShologException(UNDEFINED_CODE);
                else
                    return value;
            }

            @Override
            protected Boolean visit(ShologError error) {
                throw new ShologException(error.getCode());
            }

            @Override
            protected Boolean visit(ShologEager eager) {
                boolean left = visit(eager.getLeft());
                boolean right = visit(eager.getRight());
                switch (eager.getOp()) {
                    case And:
                        return left && right;
                    case Or:
                        return left || right;
                    case Xor:
                        return left ^ right;
                    default:
                        throw new UnsupportedOperationException("unknown eager op");
                }
            }

            @Override
            protected Boolean visit(ShologLazy lazy) {
                boolean left = visit(lazy.getLeft());
                switch (lazy.getOp()) {
                    case And:
                        return left && visit(lazy.getRight());
                    case Or:
                        return left || visit(lazy.getRight());
                    default:
                        throw new UnsupportedOperationException("unknown lazy op");
                }
            }
        }.visit(node);
    }
}
