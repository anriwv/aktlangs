package toylangs.sqelite.ast;

public record SqeliteBinOp(String op, SqeliteNode left, SqeliteNode right) implements SqeliteNode {
    @Override
    public String toString() {
        return "(" + left + " " + op + " " + right + ")";
    }
}
