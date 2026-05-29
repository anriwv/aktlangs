package toylangs.sholog;

import cma.CMaLabel;
import cma.CMaProgram;
import cma.CMaProgramWriter;
import cma.CMaUtils;
import toylangs.sholog.ast.*;

import java.util.List;

import static cma.instruction.CMaBasicInstruction.Code.*;
import static cma.instruction.CMaIntInstruction.Code.*;
import static cma.instruction.CMaLabelInstruction.Code.*;

public class ShologCompiler {

    /**
     * Defineerimata muutuja veakood.
     */
    private static final int UNDEFINED_CODE = 127;

    private final CMaProgramWriter pw = new CMaProgramWriter();
    private final List<String> variables;

    public ShologCompiler(List<String> variables) {
        this.variables = variables;
    }

    public static CMaProgram compile(ShologNode node, List<String> variables) {
        ShologCompiler shologCompiler = new ShologCompiler(variables);
        shologCompiler.compile(node);
        return shologCompiler.pw.toProgram();
    }

    /**
     * Viska CMa-s erind kokkulepitud viisil.
     */
    private void compileThrow(int code) {
        pw.visit(LOADC, -code);
        pw.visit(HALT);
    }

    private void compile(ShologNode node) {
        switch (node) {
            case ShologLit(boolean value) -> pw.visit(LOADC, CMaUtils.bool2int(value));
            case ShologVar(String name) -> {
                int variableIndex = variables.indexOf(name);
                if (variableIndex < 0)
                    compileThrow(UNDEFINED_CODE);
                else
                    pw.visit(LOADA, variableIndex);
            }
            case ShologError(int code) -> compileThrow(code);
            case ShologEager(ShologEager.Op op, ShologNode left, ShologNode right) -> {
                compile(left);
                compile(right);
                switch (op) {
                    case And -> pw.visit(AND);
                    case Or -> pw.visit(OR);
                    case Xor -> pw.visit(XOR);
                    default -> throw new UnsupportedOperationException("unknown eager op");
                }
            }
            case ShologLazy(ShologLazy.Op op, ShologNode left, ShologNode right) -> {
                compile(left);
                CMaLabel shortCircuit = new CMaLabel();

                pw.visit(DUP); // duubelda vasaku argumendi väärtus
                switch (op) {
                    case And:
                        // kasuta pealmist vasaku argumendi väärtust kontrolliks
                        pw.visit(JUMPZ, shortCircuit);
                        // kasuta alumist vasaku argumendi väärtust tehteks
                        compile(right);
                        pw.visit(AND);
                        break;
                    case Or:
                        // kasuta pealmist vasaku argumendi väärtust kontrolliks
                        pw.visit(NOT);
                        pw.visit(JUMPZ, shortCircuit);
                        // kasuta alumist vasaku argumendi väärtust tehteks
                        compile(right);
                        pw.visit(OR);
                        break;
                    default:
                        throw new UnsupportedOperationException("unknown lazy op");
                }

                pw.visit(shortCircuit);
                // short-circuit korral kasuta alumist vasaku argumendi väärtust otse
            }
        }
    }
}
