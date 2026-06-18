package toylangs.sqelite.ast;

import java.util.List;

public record SqeliteSource(List<Integer> elements) implements SqeliteNode {
    @Override
    public String toString() {
        return "from " + elements;
    }
}
