package toylangs.sqelite;

import toylangs.sqelite.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqeliteEvaluator {

    private final Map<String, Integer> env;

    private SqeliteEvaluator(Map<String, Integer> env) {
        this.env = env != null ? env : new HashMap<>();
    }

    public static Object eval(SqeliteNode node) {
        return new SqeliteEvaluator(new HashMap<>()).evalNode(node);
    }

    private Object evalNode(SqeliteNode node) {
        return switch (node) {

            case SqeliteProg(List<SqeliteAssign> assignments, SqeliteNode expr) -> {
                for (SqeliteAssign assign : assignments) {
                    int val = (Integer) evalNode(assign.expr());
                    env.put(assign.varName(), val);
                }
                yield evalNode(expr);
            }
            case SqeliteAssign _ -> throw new RuntimeException("SqeliteAssign should not be evaluated directly");

            case SqeliteNum(int value) -> value;

            case SqeliteVar(String name) -> {
                Integer val = env.get(name);
                if (val == null) throw new RuntimeException("Undefined variable: " + name);
                yield val;
            }

            case SqeliteSource(List<Integer> elements) -> new ArrayList<>(elements);

            case SqeliteBinOp(String op, SqeliteNode left, SqeliteNode right) -> {
                int l = (Integer) evalNode(left);
                int r = (Integer) evalNode(right);
                yield switch (op) {
                    case "+" -> l + r;
                    case "-" -> l - r;
                    case "*" -> l * r;
                    case "/" -> l / r;
                    case ">" -> (l > r) ? 1 : 0;
                    case "<" -> (l < r) ? 1 : 0;
                    case "==" -> (l == r) ? 1 : 0;
                    case "!=" -> (l != r) ? 1 : 0;
                    default -> throw new IllegalArgumentException("Unsupported operator: " + op);
                };
            }

            case SqeliteWhere(SqeliteNode condition) -> evalNode(condition);
            case SqeliteSelect(SqeliteNode expr) -> evalNode(expr);

            case SqeliteQuery(String type, SqeliteNode left, SqeliteNode right) -> {
                Object leftResult = evalNode(left);
                List<Integer> source = (List<Integer>) leftResult;
                List<Integer> result = new ArrayList<>();

                for (Integer elem : source) {
                    bindVar(right, elem);
                    if ("where".equals(type)) {
                        int cond = (Integer) evalNode(right);
                        if (cond != 0) result.add(elem);
                    } else { // "select"
                        int mapped = (Integer) evalNode(right);
                        result.add(mapped);
                    }
                }
                yield result;
            }

            case SqeliteMin(SqeliteNode expr) -> {
                List<Integer> list = (List<Integer>) evalNode(expr);
                yield list.stream().min(Integer::compare).orElseThrow(() -> new RuntimeException("min() called on empty list"));
            }

            case SqeliteMax(SqeliteNode expr) -> {
                List<Integer> list = (List<Integer>) evalNode(expr);
                yield list.stream().max(Integer::compare).orElseThrow(() -> new RuntimeException("max() called on empty list"));
            }

            case SqeliteAvg(SqeliteNode expr) -> {
                List<Integer> list = (List<Integer>) evalNode(expr);
                if (list.isEmpty()) throw new RuntimeException("avg() called on empty list");
                int sum = list.stream().mapToInt(Integer::intValue).sum();
                yield sum / list.size();
            }
        };
    }

    private void bindVar(SqeliteNode node, int elem) {
        String varName = findFirstVarName(node);
        if (varName != null) {
            env.put(varName, elem);
        }
    }

    private String findFirstVarName(SqeliteNode node) {
        return switch (node) {
            case SqeliteVar(String name) -> name;
            case SqeliteWhere(SqeliteNode cond) -> findFirstVarName(cond);
            case SqeliteSelect(SqeliteNode expr) -> findFirstVarName(expr);
            case SqeliteBinOp(_, SqeliteNode left, SqeliteNode right) -> {
                String fromLeft = findFirstVarName(left);
                if (fromLeft != null) yield fromLeft;
                yield findFirstVarName(right);
            }
            default -> null;
        };
    }
}
