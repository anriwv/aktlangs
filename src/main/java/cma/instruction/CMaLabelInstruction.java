package cma.instruction;

import cma.CMaLabel;

public record CMaLabelInstruction(Code code, CMaLabel label) implements CMaInstruction<CMaLabelInstruction.Code> {

    public enum Code {
        //@formatter:off
        /** hüppa labelile */
        JUMP,

        /** hüppa labelile kui stackipealne väärtus on 0 */
        JUMPZ,
        //@formatter:on
    }

    @Override
    public String toString() {
        return code.name() + " " + label;
    }
}
