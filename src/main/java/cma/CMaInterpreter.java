package cma;

import cma.instruction.CMaBasicInstruction;
import cma.instruction.CMaInstruction;
import cma.instruction.CMaIntInstruction;
import cma.instruction.CMaLabelInstruction;

import static cma.instruction.CMaBasicInstruction.Code.LOAD;
import static cma.instruction.CMaBasicInstruction.Code.STORE;
import static cma.instruction.CMaIntInstruction.Code.LOADC;

public class CMaInterpreter {

    private final CMaProgram program;
    private int pc = 0;
    private final CMaStack stack;

    private CMaInterpreter(CMaProgram program, CMaStack initialStack) {
        this.program = program;
        this.stack = new CMaStack(initialStack);
    }

    public static CMaStack run(CMaProgram program) {
        return run(program, new CMaStack());
    }

    public static CMaStack run(CMaProgram program, CMaStack initialStack) {
        CMaInterpreter interpreter = new CMaInterpreter(program, initialStack);
        return interpreter.execute();
    }

    private CMaStack execute() {
        while (0 <= pc && pc < program.instructions().size()) {
            CMaInstruction<?> instruction = program.instructions().get(pc);
            pc++;
            execute(instruction);
        }
        return stack;
    }

    private void execute(CMaInstruction<?> instruction) {
        switch (instruction) {
            case CMaBasicInstruction(CMaBasicInstruction.Code code) -> {
                int arg, lhs, rhs;
                switch (code) {
                    case ADD, SUB, MUL, DIV, MOD, AND, OR, XOR, EQ, NEQ, LE, LEQ, GE, GR, GEQ -> {
                        rhs = stack.pop();
                        lhs = stack.pop();
                        switch (code) {
                            case ADD -> stack.push(lhs + rhs);
                            case SUB -> stack.push(lhs - rhs);
                            case MUL -> stack.push(lhs * rhs);
                            case DIV -> stack.push(lhs / rhs);
                            case MOD -> stack.push(lhs % rhs);
                            case AND -> stack.push(lhs & rhs);
                            case OR -> stack.push(lhs | rhs);
                            case XOR -> stack.push(lhs ^ rhs);
                            case EQ -> stack.push(CMaUtils.bool2int(lhs == rhs));
                            case NEQ -> stack.push(CMaUtils.bool2int(lhs != rhs));
                            case LE -> stack.push(CMaUtils.bool2int(lhs < rhs));
                            case LEQ -> stack.push(CMaUtils.bool2int(lhs <= rhs));
                            case GE, GR -> stack.push(CMaUtils.bool2int(lhs > rhs));
                            case GEQ -> stack.push(CMaUtils.bool2int(lhs >= rhs));
                        }
                    }
                    case NEG -> {
                        arg = stack.pop();
                        stack.push(-arg);
                    }
                    case NOT -> {
                        arg = stack.pop();
                        stack.push(CMaUtils.bool2int(!CMaUtils.int2bool(arg)));
                    }
                    case POP -> stack.pop();
                    case DUP -> stack.push(stack.peek());
                    case LOAD -> {
                        arg = stack.pop();
                        stack.push(stack.get(arg));
                    }
                    case STORE -> {
                        arg = stack.pop();
                        stack.set(arg, stack.peek());
                    }
                    case HALT -> pc = -1; // out of range pc halts
                }
            }
            case CMaIntInstruction(CMaIntInstruction.Code code, int arg) -> {
                switch (code) {
                    case LOADC -> stack.push(arg);
                    case LOADA -> {
                        execute(new CMaIntInstruction(LOADC, arg));
                        execute(new CMaBasicInstruction(LOAD));
                    }
                    case STOREA -> {
                        execute(new CMaIntInstruction(LOADC, arg));
                        execute(new CMaBasicInstruction(STORE));
                    }
                }

            }
            case CMaLabelInstruction(CMaLabelInstruction.Code code, CMaLabel label) -> {
                switch (code) {
                    case JUMP -> pc = getLabelTarget(label);
                    case JUMPZ -> {
                        if (!CMaUtils.int2bool(stack.pop()))
                            pc = getLabelTarget(label);
                    }
                }
            }
        }
    }

    private int getLabelTarget(CMaLabel label) {
        Integer target = program.labels().get(label);
        if (target != null)
            return target;
        else
            throw new CMaException("label '%s' not placed".formatted(label));
    }
}
