package cma.instruction;

public sealed interface CMaInstruction<Code> permits CMaBasicInstruction, CMaIntInstruction, CMaLabelInstruction {

    Code code();

    @Override
    String toString();
}
