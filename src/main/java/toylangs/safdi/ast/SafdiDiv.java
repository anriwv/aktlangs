package toylangs.safdi.ast;

public record SafdiDiv(SafdiNode left, SafdiNode right, SafdiNode recover) implements SafdiNode {
    @Override
    public String toString() {
        return "div(" +
                "" + left +
                ", " + right +
                ", " + recover +
                ")";
    }
}
