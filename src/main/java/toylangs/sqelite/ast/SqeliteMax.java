package toylangs.sqelite.ast;

public record SqeliteMax(SqeliteNode expression) implements SqeliteNode {
    @Override
    public String toString() {
        return "max(" + expression + ")";
    }
}
