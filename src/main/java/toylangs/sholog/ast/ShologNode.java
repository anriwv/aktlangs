package toylangs.sholog.ast;

import toylangs.AbstractNode;

public sealed interface ShologNode extends AbstractNode permits ShologBinary, ShologError, ShologLit, ShologVar {

    static ShologLit lit(boolean value) {
        return new ShologLit(value);
    }

    static ShologVar var(String name) {
        return new ShologVar(name);
    }

    static ShologError error(int code) {
        return new ShologError(code);
    }

    static ShologEager eand(ShologNode left, ShologNode right) {
        return new ShologEager(ShologEager.Op.And, left, right);
    }

    static ShologEager eor(ShologNode left, ShologNode right) {
        return new ShologEager(ShologEager.Op.Or, left, right);
    }

    static ShologEager xor(ShologNode left, ShologNode right) {
        return new ShologEager(ShologEager.Op.Xor, left, right);
    }

    static ShologLazy land(ShologNode left, ShologNode right) {
        return new ShologLazy(ShologLazy.Op.And, left, right);
    }

    static ShologLazy lor(ShologNode left, ShologNode right) {
        return new ShologLazy(ShologLazy.Op.Or, left, right);
    }

    static ShologBinary binary(String op, ShologNode left, ShologNode right) {
        return switch (op) {
            case "/\\" -> eand(left, right);
            case "&&" -> land(left, right);
            case "+" -> xor(left, right);
            case "\\/" -> eor(left, right);
            case "||" -> lor(left, right);
            default -> throw new IllegalArgumentException("unknown op");
        };
    }
}
