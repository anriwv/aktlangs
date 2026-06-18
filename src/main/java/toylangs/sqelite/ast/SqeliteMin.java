package toylangs.sqelite.ast;

public record SqeliteMin(SqeliteNode expression) implements SqeliteNode {
    @Override
    public String toString() {
        return "min(" + expression + ")";
    }
}
