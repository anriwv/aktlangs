package toylangs.sqelite.ast;

public record SqeliteQuery(String type, SqeliteNode left, SqeliteNode right) implements SqeliteNode {
    @Override
    public String toString() {
        return left.toString() + type + right.toString();
    }
}
