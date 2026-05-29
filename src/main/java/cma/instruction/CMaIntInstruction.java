package cma.instruction;

public record CMaIntInstruction(Code code, int arg) implements CMaInstruction<CMaIntInstruction.Code> {

    public enum Code {
        //@formatter:off
        /** lisa stackile konstant */
        LOADC,

        /** loe väärtus indeksilt */
        LOADA,

        /** salvesta väärtus indeksile */
        STOREA,
        //@formatter:on
    }

    @Override
    public String toString() {
        return code.name() + " " + arg;
    }
}
