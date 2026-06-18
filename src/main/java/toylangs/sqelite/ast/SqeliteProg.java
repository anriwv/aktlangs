package toylangs.sqelite.ast;

import java.util.List;

public record SqeliteProg(List<SqeliteAssign> assignments, SqeliteNode expr) implements SqeliteNode {
    @Override
    public String toString() {
        if (assignments.isEmpty()) return expr.toString();
        String assigns = String.join(", ", assignments.stream().map(Object::toString).toList());
        return assigns + ", " + expr;
    }
}
