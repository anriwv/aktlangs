package toylangs.sqelite;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import toylangs.sqelite.ast.SqeliteNode;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static toylangs.sqelite.ast.SqeliteNode.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SqeliteAstTest {

    private void legal(String input, SqeliteNode expectedAst) {
        SqeliteNode actualAst = SqeliteAst.makeSqeliteAst(input);
        assertEquals(expectedAst, actualAst);
    }

    private void illegal(String input) {
        try {
            SqeliteAst.makeSqeliteAst(input);
            fail("expected parse error: " + input);
        } catch (Exception ignored) {}
    }

    @Test
    public void test01_num() {
        legal("1", prog(List.of(), num(1)));
        legal("42", prog(List.of(), num(42)));
    }

    @Test
    public void test02_var() {
        legal("a", prog(List.of(), var("a")));
        legal("Table", prog(List.of(), var("Table")));
        illegal("2a");
    }

    @Test
    public void test03_operator() {
        legal("a + b", prog(List.of(), binOp("+", var("a"), var("b"))));
        legal("a * b", prog(List.of(), binOp("*", var("a"), var("b"))));
        legal("a > b", prog(List.of(), binOp(">", var("a"), var("b"))));
    }

    @Test
    public void test04_prio_assoc() {
        legal("a + b * c", prog(List.of(), binOp(
                "+",
                var("a"),
                binOp("*", var("b"), var("c")))));

        legal("(a + b) * c", prog(List.of(), binOp(
                "*",
                binOp("+", var("a"), var("b")),
                var("c"))));
    }

    @Test
    public void test05_source() {
        legal("from [1, 2, 3]", prog(List.of(), source(List.of(1, 2, 3))));
        legal("from []", prog(List.of(), source(List.of())));
    }

    @Test
    public void test06_query() {
        legal("from [1, 2] select x * 2", prog(List.of(), query("select", source(List.of(1, 2)), binOp("*", var("x"), num(2)))));
        legal("from [1, 2] where x > 1", prog(List.of(), query("where", source(List.of(1, 2)), binOp(">", var("x"), num(1)))));
    }

    @Test
    public void test07_aggregations() {
        legal("max(from [1, 2])", prog(List.of(), max(source(List.of(1, 2)))));
        legal("avg(from [1, 2])", prog(List.of(), avg(source(List.of(1, 2)))));
    }

    @Test
    public void test08_assignments_complex() {
        legal("a = 10, a + 5", prog(List.of(
                (toylangs.sqelite.ast.SqeliteAssign) assign("a", num(10))
        ), binOp("+", var("a"), num(5))));
        
        legal("a = 2, from [1, 2] select x * a", prog(List.of(
                (toylangs.sqelite.ast.SqeliteAssign) assign("a", num(2))
        ), query("select", source(List.of(1, 2)), binOp("*", var("x"), var("a")))));
    }
}
