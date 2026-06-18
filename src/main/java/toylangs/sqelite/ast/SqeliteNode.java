package toylangs.sqelite.ast;

import toylangs.AbstractNode;
import java.util.List;

public sealed interface SqeliteNode extends AbstractNode permits SqeliteNum, SqeliteVar, SqeliteQuery, SqeliteBinOp, SqeliteSource, SqeliteSelect, SqeliteWhere, SqeliteAvg, SqeliteMin, SqeliteMax, SqeliteAssign, SqeliteProg {

    static SqeliteNode prog(List<SqeliteAssign> assignments, SqeliteNode expr) { return new SqeliteProg(assignments, expr); }
    static SqeliteNode assign(String varName, SqeliteNode expr) { return new SqeliteAssign(varName, expr); }
    static SqeliteNode var(String name) { return new SqeliteVar(name); }
    static SqeliteNode num(int value) { return new SqeliteNum(value); }
    static SqeliteNode source(List<Integer> elements) { return new SqeliteSource(elements); } // hetkel ainult arvud
    static SqeliteNode where(SqeliteNode condition) { return new SqeliteWhere(condition); }
    static SqeliteNode select(SqeliteNode expression) { return new SqeliteSelect(expression); }
    static SqeliteNode query(String type, SqeliteNode left, SqeliteNode right) { return new SqeliteQuery(type, left, right); }
    static SqeliteNode avg(SqeliteNode expression) { return new SqeliteAvg(expression); }
    static SqeliteNode min(SqeliteNode expression) { return new SqeliteMin(expression); }
    static SqeliteNode max(SqeliteNode expression) { return new SqeliteMax(expression); }

    static SqeliteBinOp binOp(String op, SqeliteNode left, SqeliteNode right) {
        return new SqeliteBinOp(op, left, right);
    }

}
