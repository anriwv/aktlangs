package toylangs.sqelite.ast;

public record SqeliteAssign(String varName, SqeliteNode expr) implements SqeliteNode {
    @Override
    public String toString() {
        return varName + " = " + expr;
    }
}
