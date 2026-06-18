package toylangs.sqelite.ast;

public record SqeliteWhere(SqeliteNode condition) implements SqeliteNode {
    @Override
    public String toString() {
        return "where(" + condition + ")";
    }
}
