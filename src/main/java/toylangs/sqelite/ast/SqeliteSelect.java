package toylangs.sqelite.ast;

public record SqeliteSelect(SqeliteNode expression) implements SqeliteNode {
    @Override
    public String toString() {
        return "select" + expression;
    }
}
