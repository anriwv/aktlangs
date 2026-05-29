package toylangs.safdi.ast;

public record SafdiNum(int value) implements SafdiNode {
    @Override
    public String toString() {
        return "num(" +
                "" + value +
                ")";
    }
}
