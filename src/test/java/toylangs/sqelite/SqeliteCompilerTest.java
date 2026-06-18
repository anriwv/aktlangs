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

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SqeliteCompilerTest {

    private int evalInt(String code) {
        SqeliteNode ast = SqeliteAst.makeSqeliteAst(code);
        CMaProgram program = SqeliteCompiler.compile(ast, new ArrayList<>());
        CMaStack stack = CMaInterpreter.run(program);
        return stack.peek();
    }

    private List<Integer> evalList(String code) {
        SqeliteNode ast = SqeliteAst.makeSqeliteAst(code);
        CMaProgram program = SqeliteCompiler.compile(ast, new ArrayList<>());
        CMaStack stack = CMaInterpreter.run(program);
        int addr = stack.peek();
        int size = stack.get(addr);
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(stack.get(addr + 1 + i));
        }
        return list;
    }

    @Test
    public void test01_num() {
        assertEquals(42, evalInt("42"));
        assertEquals(0, evalInt("0"));
    }

    @Test
    public void test02_operator() {
        assertEquals(15, evalInt("10 + 5"));
        assertEquals(20, evalInt("10 * 2"));
    }

    @Test
    public void test03_var() {
        assertEquals(15, evalInt("a = 10, b = 5, a + b"));
        assertEquals(50, evalInt("x = 10, x * 5"));
    }

    @Test
    public void test04_prio_assoc() {
        assertEquals(20, evalInt("10 + 5 * 2"));
        assertEquals(30, evalInt("(10 + 5) * 2"));
    }

    @Test
    public void test05_source() {
        assertEquals(List.of(1, 2, 3), evalList("from [1, 2, 3]"));
        assertEquals(List.of(), evalList("from []"));
    }

    @Test
    public void test06_query() {
        assertEquals(List.of(2, 4, 6), evalList("from [1, 2, 3] select x * 2"));
        assertEquals(List.of(3), evalList("from [1, 2, 3] where x > 2"));
    }

    @Test
    public void test07_aggregations() {
        assertEquals(3, evalInt("max(from [1, 2, 3])"));
        assertEquals(1, evalInt("min(from [1, 2, 3])"));
        assertEquals(2, evalInt("avg(from [1, 2, 3])"));
    }

    @Test
    public void test08_complex() {
        assertEquals(6, evalInt("max((from [1, 2, 3] select x * 2) where x > 2)"));
        assertEquals(16, evalInt("a = 2, b = 10, max((from [1, 2, 3] select x * a) select x + b)"));
    }
}
