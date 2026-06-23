package toylangs.sqelite;

import cma.CMaInterpreter;
import cma.CMaProgram;
import cma.CMaStack;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import toylangs.sqelite.ast.SqeliteNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SqeliteRandomEquivalenceTest {
    private static final long SEED = 0x5eed_2026L;
    private static final int CASES = 1_000;
    private static final String[] ASSIGNABLE_VARS = {"a", "b", "c"};

    @Test
    public void test01_aggregateAsRightBinaryOperandMatchesEvaluator() {
        assertProgramMatches("9 - max(from [7, 8])");
    }

    @Test
    public void test02_randomProgramsMatchEvaluator() {
        Random random = new Random(SEED);

        for (int i = 0; i < CASES; i++) {
            ProgramCase program = randomProgram(random);
            assertProgramMatches("seed=" + SEED + ", case=" + i + ", source=" + program.source(), program.source());
        }
    }

    private static void assertProgramMatches(String source) {
        assertProgramMatches("source=" + source, source);
    }

    private static void assertProgramMatches(String message, String source) {
        SqeliteNode ast = SqeliteAst.makeSqeliteAst(source);
        Object evaluated = SqeliteEvaluator.eval(ast);
        Object compiled = evalCompiled(ast, evaluated instanceof List<?>);
        assertEquals(message, evaluated, compiled);
    }

    private static Object evalCompiled(SqeliteNode ast, boolean listResult) {
        CMaProgram program = SqeliteCompiler.compile(ast, new ArrayList<>());
        CMaStack stack = CMaInterpreter.run(program);
        if (!listResult) return stack.peek();
        return readList(stack);
    }

    private static List<Integer> readList(CMaStack stack) {
        int address = stack.peek();
        int size = stack.get(address);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(stack.get(address + 1 + i));
        }
        return result;
    }

    private static ProgramCase randomProgram(Random random) {
        List<String> vars = new ArrayList<>();
        List<String> assignments = new ArrayList<>();
        int assignmentCount = random.nextInt(ASSIGNABLE_VARS.length + 1);
        for (int i = 0; i < assignmentCount; i++) {
            String var = ASSIGNABLE_VARS[i];
            String expr = randomIntExpr(random, vars, 2);
            assignments.add(var + " = " + expr);
            vars.add(var);
        }

        String resultExpr = random.nextBoolean()
                ? randomIntExpr(random, vars, 3)
                : randomListExpr(random, vars, 3, false);

        if (assignments.isEmpty()) return new ProgramCase(resultExpr);
        return new ProgramCase(String.join(", ", assignments) + ", " + resultExpr);
    }

    private static String randomIntExpr(Random random, List<String> vars, int depth) {
        if (depth <= 0) return randomScalar(random, vars);

        return switch (random.nextInt(5)) {
            case 0 -> randomScalar(random, vars);
            case 1 -> parens(randomIntExpr(random, vars, depth - 1) + " " + randomArithmeticOp(random) + " "
                    + randomIntExpr(random, vars, depth - 1));
            case 2 -> parens(randomIntExpr(random, vars, depth - 1) + " " + randomComparisonOp(random) + " "
                    + randomIntExpr(random, vars, depth - 1));
            case 3 -> randomAggregate(random, vars, depth - 1);
            default -> parens(randomIntExpr(random, vars, depth - 1));
        };
    }

    private static String randomScalar(Random random, List<String> vars) {
        if (!vars.isEmpty() && random.nextBoolean()) {
            return vars.get(random.nextInt(vars.size()));
        }
        return Integer.toString(random.nextInt(11));
    }

    private static String randomAggregate(Random random, List<String> vars, int depth) {
        String op = switch (random.nextInt(3)) {
            case 0 -> "min";
            case 1 -> "max";
            default -> "avg";
        };
        return op + "(" + randomListExpr(random, vars, depth, true) + ")";
    }

    private static String randomListExpr(Random random, List<String> vars, int depth, boolean forceNonEmpty) {
        String expr = randomSource(random, forceNonEmpty);
        int steps = depth <= 0 ? 0 : random.nextInt(depth + 1);

        for (int i = 0; i < steps; i++) {
            if (forceNonEmpty || random.nextBoolean()) {
                expr = parens(expr) + " select " + randomSelectExpr(random, vars);
            } else {
                expr = parens(expr) + " where " + randomWhereExpr(random, vars);
            }
        }
        return expr;
    }

    private static String randomSource(Random random, boolean forceNonEmpty) {
        int size = forceNonEmpty ? random.nextInt(5) + 1 : random.nextInt(7);
        List<String> elements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            elements.add(Integer.toString(random.nextInt(11)));
        }
        return "from [" + String.join(", ", elements) + "]";
    }

    private static String randomSelectExpr(Random random, List<String> vars) {
        String global = vars.isEmpty() ? Integer.toString(random.nextInt(6)) : vars.get(random.nextInt(vars.size()));
        return switch (random.nextInt(6)) {
            case 0 -> "x";
            case 1 -> parens("x + " + random.nextInt(6));
            case 2 -> parens("x - " + random.nextInt(6));
            case 3 -> parens("x * " + random.nextInt(5));
            case 4 -> parens("x + " + global);
            default -> parens(parens("x + " + random.nextInt(4)) + " * " + (random.nextInt(4) + 1));
        };
    }

    private static String randomWhereExpr(Random random, List<String> vars) {
        String rhs = vars.isEmpty() || random.nextBoolean()
                ? Integer.toString(random.nextInt(11))
                : vars.get(random.nextInt(vars.size()));
        return switch (random.nextInt(5)) {
            case 0 -> parens("x > " + rhs);
            case 1 -> parens("x < " + rhs);
            case 2 -> parens("x == " + rhs);
            case 3 -> parens("x != " + rhs);
            default -> parens(parens("x + " + random.nextInt(4)) + " > " + rhs);
        };
    }

    private static String randomArithmeticOp(Random random) {
        return switch (random.nextInt(3)) {
            case 0 -> "+";
            case 1 -> "-";
            default -> "*";
        };
    }

    private static String randomComparisonOp(Random random) {
        return switch (random.nextInt(4)) {
            case 0 -> ">";
            case 1 -> "<";
            case 2 -> "==";
            default -> "!=";
        };
    }

    private static String parens(String expr) {
        return "(" + expr + ")";
    }

    private record ProgramCase(String source) {
    }
}
