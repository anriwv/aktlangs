package toylangs.sqelite;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import toylangs.sqelite.ast.SqeliteNode;

import java.util.List;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SqeliteEvaluatorTest {

    private Object eval(String code) {
        SqeliteNode ast = SqeliteAst.makeSqeliteAst(code);
        return SqeliteEvaluator.eval(ast);
    }

    @Test
    public void test01_num() {
        assertEquals(42, eval("42"));
        assertEquals(0, eval("0"));
    }

    @Test
    public void test02_operator() {
        assertEquals(15, eval("10 + 5"));
        assertEquals(20, eval("10 * 2"));
    }

    @Test
    public void test03_var() {
        assertEquals(15, eval("a = 10, b = 5, a + b"));
        assertEquals(50, eval("x = 10, x * 5"));
    }

    @Test
    public void test04_prio_assoc() {
        assertEquals(20, eval("10 + 5 * 2"));
        assertEquals(30, eval("(10 + 5) * 2"));
    }

    @Test
    public void test05_source() {
        assertEquals(List.of(1, 2, 3), eval("from [1, 2, 3]"));
        assertEquals(List.of(), eval("from []"));
    }

    @Test
    public void test06_query() {
        assertEquals(List.of(2, 4, 6), eval("from [1, 2, 3] select x * 2"));
        assertEquals(List.of(3), eval("from [1, 2, 3] where x > 2"));
    }

    @Test
    public void test07_aggregations() {
        assertEquals(3, eval("max(from [1, 2, 3])"));
        assertEquals(1, eval("min(from [1, 2, 3])"));
        assertEquals(2, eval("avg(from [1, 2, 3])"));
    }

    @Test
    public void test08_complex() {
        assertEquals(6, eval("max((from [1, 2, 3] select x * 2) where x > 2)"));
        assertEquals(16, eval("a = 2, b = 10, max((from [1, 2, 3] select x * a) select x + b)"));
    }
}
