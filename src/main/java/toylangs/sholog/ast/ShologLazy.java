package toylangs.sholog.ast;

import java.util.List;

public record ShologLazy(Op op, ShologNode left, ShologNode right) implements ShologBinary {
    public enum Op {
        And("land"),
        Or("lor");

        private final String nodeInfo;

        Op(String nodeInfo) {
            this.nodeInfo = nodeInfo;
        }
    }

    @Override
    public List<Object> getChildren() {
        return List.of(left, right);
    }

    @Override
    public String getNodeLabel() {
        return op.nodeInfo;
    }

    @Override
    public String toString() {
        return  op.nodeInfo + "(" +
                "" + left +
                ", " + right +
                ")";
    }
}
