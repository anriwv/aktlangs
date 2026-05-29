package toylangs.safdi.ast;

import toylangs.AbstractNode;

public sealed interface SafdiNode extends AbstractNode permits SafdiAdd, SafdiDiv, SafdiMul, SafdiNeg, SafdiNum, SafdiVar {

    static SafdiNum num(int value) {
        return new SafdiNum(value);
    }

    static SafdiVar var(String name) {
        return new SafdiVar(name);
    }

    static SafdiNeg neg(SafdiNode expr) {
        return new SafdiNeg(expr);
    }

    static SafdiAdd add(SafdiNode left, SafdiNode right) {
        return new SafdiAdd(left, right);
    }

    static SafdiMul mul(SafdiNode left, SafdiNode right) {
        return new SafdiMul(left, right);
    }

    static SafdiDiv div(SafdiNode left, SafdiNode right) {
        return div(left, right, null);
    }

    static SafdiDiv div(SafdiNode left, SafdiNode right, SafdiNode recover) {
        return new SafdiDiv(left, right, recover);
    }

}
