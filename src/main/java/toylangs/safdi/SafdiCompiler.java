package toylangs.safdi;

import cma.CMaLabel;
import cma.CMaProgram;
import cma.CMaProgramWriter;
import toylangs.safdi.ast.*;

import java.util.List;

import static cma.instruction.CMaBasicInstruction.Code.*;
import static cma.instruction.CMaIntInstruction.Code.LOADA;
import static cma.instruction.CMaIntInstruction.Code.LOADC;
import static cma.instruction.CMaLabelInstruction.Code.JUMP;
import static cma.instruction.CMaLabelInstruction.Code.JUMPZ;

public class SafdiCompiler {
    private final CMaProgramWriter pw = new CMaProgramWriter();
    private final List<String> variables;

    public SafdiCompiler(List<String> variables) {
        this.variables = variables;
    }

    public static CMaProgram compile(SafdiNode node, List<String> variables) {
        SafdiCompiler safdiCompiler = new SafdiCompiler(variables);
        safdiCompiler.compile(node);
        return safdiCompiler.pw.toProgram();
    }

    /**
     * Viska CMa-s erind kokkulepitud viisil.
     */
    private void compileThrow() {
        pw.visit(HALT);
    }

    private void compile(SafdiNode node) {
        switch (node) {
            case SafdiNum(int value) -> pw.visit(LOADC, value);
            case SafdiVar(String name) -> {
                int variableIndex = variables.indexOf(name);
                if (variableIndex < 0)
                    compileThrow();
                else
                    pw.visit(LOADA, variableIndex);
            }
            case SafdiNeg(SafdiNode expr) -> {
                compile(expr);
                pw.visit(NEG);
            }
            case SafdiAdd(SafdiNode left, SafdiNode right) -> {
                compile(left);
                compile(right);
                pw.visit(ADD);
            }
            case SafdiMul(SafdiNode left, SafdiNode right) -> {
                compile(left);
                compile(right);
                pw.visit(MUL);
            }
            case SafdiDiv(SafdiNode left, SafdiNode right, SafdiNode recover) -> {
                // jagaja kontroll
                compile(right);
                CMaLabel zeroDiv = new CMaLabel();
                pw.visit(JUMPZ, zeroDiv);

                // tavaline jagamine
                compile(left);
                compile(right); // ei saa DUP kasutada, sest DIV järjekord vastupidine
                pw.visit(DIV);
                CMaLabel end = new CMaLabel();
                pw.visit(JUMP, end);

                // nulliga jagamine
                pw.visit(zeroDiv);
                if (recover != null)
                    compile(recover);
                else
                    compileThrow();

                pw.visit(end);
            }
        }
    }
}
