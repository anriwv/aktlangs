package toylangs.safdi;

import toylangs.safdi.ast.*;

import java.util.Map;

public class SafdiEvaluator {

    public static class SafdiException extends RuntimeException {
    }

    private final Map<String, Integer> env;

    private SafdiEvaluator(Map<String, Integer> env) {
        this.env = env;
    }

    public static int eval(SafdiNode node, Map<String, Integer> env) {
        SafdiEvaluator safdiEvaluator = new SafdiEvaluator(env);
        return safdiEvaluator.evalNode(node);
    }

    private int evalNode(SafdiNode node) {
        return switch (node) {
            case SafdiNum(int value) -> value;
            case SafdiVar(String name) -> {
                Integer value = env.get(name);
                if (value == null) throw new SafdiException();
                else yield value;
            }
            case SafdiNeg(SafdiNode expr) -> -evalNode(expr);
            case SafdiAdd(SafdiNode left, SafdiNode right) -> evalNode(left) + evalNode(right);
            case SafdiMul(SafdiNode left, SafdiNode right) -> evalNode(left) * evalNode(right);
            case SafdiDiv(SafdiNode left, SafdiNode right, SafdiNode recover) -> {
                int rightValue = evalNode(right);
                if (rightValue == 0) {
                    if (recover != null) yield evalNode(recover);
                    else throw new SafdiException();
                } else yield evalNode(left) / rightValue;
            }
        };
    }
}
