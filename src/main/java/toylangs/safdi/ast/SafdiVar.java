package toylangs.safdi.ast;

public record SafdiVar(String name) implements SafdiNode {
    @Override
    public String toString() {
        return "var(" +
                "\"" + name + "\"" +
                ")";
    }
}
