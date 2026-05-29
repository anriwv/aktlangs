package toylangs.safdi.ast;

public record SafdiMul(SafdiNode left, SafdiNode right) implements SafdiNode {
    @Override
    public String toString() {
        return "mul(" +
                "" + left +
                ", " + right +
                ")";
    }
}
