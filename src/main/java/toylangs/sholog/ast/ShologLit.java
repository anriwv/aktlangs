package toylangs.sholog.ast;

public record ShologLit(boolean value) implements ShologNode {
    @Override
    public String toString() {
        return "lit(" +
                "" + value +
                ")";
    }
}
