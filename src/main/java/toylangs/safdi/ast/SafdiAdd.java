package toylangs.safdi.ast;

public record SafdiAdd(SafdiNode left, SafdiNode right) implements SafdiNode {
    @Override
    public String toString() {
        return "add(" +
                "" + left +
                ", " + right +
                ")";
    }
}
