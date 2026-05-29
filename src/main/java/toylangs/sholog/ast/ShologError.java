package toylangs.sholog.ast;

public record ShologError(int code) implements ShologNode {
    @Override
    public String toString() {
        return "error(" +
                "" + code +
                ")";
    }
}
