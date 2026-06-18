package toylangs.sqelite.ast;

public record SqeliteNum(int value) implements SqeliteNode {
    @Override
    public String toString() {
        return "num(" + "" + value + ")";
    }
}
