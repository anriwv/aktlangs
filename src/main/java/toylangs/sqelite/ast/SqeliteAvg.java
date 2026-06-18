package toylangs.sqelite.ast;

public record SqeliteAvg(SqeliteNode expression) implements SqeliteNode {
    @Override
    public String toString() {
        return "avg(" + expression + ")";
    }
}
