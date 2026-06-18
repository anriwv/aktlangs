package toylangs.sqelite.ast;


public record SqeliteVar(String name) implements SqeliteNode {
    @Override
    public String toString() {
        return "var(" +
                "\"" + name + "\"" +
                ")";
    }
}

