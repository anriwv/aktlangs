package toylangs.sholog.ast;

public record ShologVar(String name) implements ShologNode {
    @Override
    public String toString() {
        return "var(" +
                "\"" + name + "\"" +
                ")";
    }
}
