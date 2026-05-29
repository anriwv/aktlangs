package toylangs.safdi.ast;

public record SafdiNeg(SafdiNode expr) implements SafdiNode {
    @Override
    public String toString() {
        return "neg(" +
                "" + expr +
                ")";
    }
}
