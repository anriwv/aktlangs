package toylangs.sqelite;

import cma.*;
import toylangs.sqelite.ast.*;
import java.util.*;
import static cma.instruction.CMaBasicInstruction.Code.*;
import static cma.instruction.CMaIntInstruction.Code.*;
import static cma.instruction.CMaLabelInstruction.Code.*;

public class SqeliteCompiler {
    private final CMaProgramWriter pw = new CMaProgramWriter();
    private final List<String> globalVariables;
    private final Map<String, Integer> env = new HashMap<>();
    private int nextReg, HEAP_PTR_IDX, HEAP_BASE;

    public SqeliteCompiler(List<String> variables) {
        this.globalVariables = variables;
        this.HEAP_PTR_IDX = variables.size();
        this.nextReg = this.HEAP_PTR_IDX + 1;
        this.HEAP_BASE = this.HEAP_PTR_IDX + 100;
    }

    public static CMaProgram compile(SqeliteNode node, List<String> variables) {
        SqeliteCompiler compiler = new SqeliteCompiler(variables);
        compiler.compileProgram(node);
        return compiler.pw.toProgram();
    }

    private void compileProgram(SqeliteNode root) {
        for (int i = 0; i < globalVariables.size(); i++) env.put(globalVariables.get(i), i);
        for (int i = globalVariables.size(); i < HEAP_BASE + 2000; i++) pw.visit(LOADC, 0);
        pw.visit(LOADC, HEAP_BASE); pw.visit(STOREA, HEAP_PTR_IDX);
        compile(root);
    }

    private void compile(SqeliteNode node) {
        switch (node) {
            case SqeliteProg p -> {
                for (SqeliteAssign a : p.assignments()) {
                    compile(a.expr());
                    pw.visit(STOREA, nextReg); env.put(a.varName(), nextReg++);
                }
                compile(p.expr());
            }
            case SqeliteAssign _ -> pw.visit(HALT);

            case SqeliteNum n -> pw.visit(LOADC, n.value());

            /** Laeb pinule muutuja väärtuse, viskab erindi kui defineerimata. */
            case SqeliteVar v -> {
                if (env.containsKey(v.name())) pw.visit(LOADA, env.get(v.name()));
                else pw.visit(HALT);
            }

            /** Binaarsed tehted (aritmeetika ja võrdlused). */
            case SqeliteBinOp b -> {
                compile(b.left()); compile(b.right());
                switch (b.op()) {
                    case "+" -> pw.visit(ADD); case "-" -> pw.visit(SUB);
                    case "*" -> pw.visit(MUL); case "/" -> pw.visit(DIV);

                    case "==" -> pw.visit(EQ); case "!=" -> pw.visit(NEQ);
                    case ">" -> pw.visit(GR);  case ">=" -> pw.visit(GEQ);
                    case "<" -> pw.visit(LE);  case "<=" -> pw.visit(LEQ);
                    default -> pw.visit(HALT);
                }
            }

            /** Andmeallikas (from): loob uue listi ja salvestab elemendid mällu. */
            case SqeliteSource s -> {
                // Reserveerime registri, mis hoiab listi algusaadressi heapis.
                int listAddr = nextReg++, size = s.elements().size();

                // Võtame heap'i järgmise vaba aadressi ja salvestame selle listi aadressiks.
                pw.visit(LOADA, HEAP_PTR_IDX); pw.visit(STOREA, listAddr);

                // Listi esimesse mälupesasse salvestatakse listi pikkus.
                pw.visit(LOADC, size); pw.visit(LOADA, listAddr); pw.visit(STORE);

                // Salvestame kõik elemendid kujul [size][elem1][elem2]...
                for (int i = 0; i < size; i++) {
                    pw.visit(LOADC, s.elements().get(i)); pw.visit(LOADA, listAddr);
                    pw.visit(LOADC, i + 1); pw.visit(ADD); pw.visit(STORE);
                }

                // Nihutame heap'i osutit edasi, et järgmine list kirjutataks vabasse kohta.
                pw.visit(LOADA, HEAP_PTR_IDX); pw.visit(LOADC, size + 1); pw.visit(ADD); pw.visit(STOREA, HEAP_PTR_IDX);

                // Tulemuseks jääb pinu tippu loodud listi aadress.
                pw.visit(LOADA, listAddr); nextReg--;
            }

            /** Query (select/where): itereerib üle andmeallika elementide ja filtreerib või teisendab neid. */
            case SqeliteQuery q -> {
                // Kompileerime lähteandmed ja saame pinule töödeldava listi aadressi.
                compile(q.left());

                // Loome registrid iteratsiooni jaoks.
                int listAddr = nextReg++, listSize = nextReg++, idx = nextReg++, newListAddr = nextReg++, newListSize = nextReg++;

                // Leiame query muutuja nime (nt x avaldises 'x > 5').
                String varName = findFirstVarName(q.right()); if (varName == null) varName = "$dummy";
                int loopVar = nextReg++; Integer oldVarIdx = env.put(varName, loopVar);

                // Loeme listi aadressi ja pikkuse ning valmistame ette uue tulemuste listi.
                pw.visit(STOREA, listAddr); pw.visit(LOADA, listAddr); pw.visit(LOAD); pw.visit(STOREA, listSize);
                pw.visit(LOADC, 0); pw.visit(STOREA, idx);
                pw.visit(LOADA, HEAP_PTR_IDX); pw.visit(STOREA, newListAddr);
                pw.visit(LOADC, 0); pw.visit(STOREA, newListSize);
                pw.visit(LOADA, HEAP_PTR_IDX); pw.visit(LOADC, 1); pw.visit(ADD); pw.visit(STOREA, HEAP_PTR_IDX);

                // Tsükli algus ja lõpp.
                CMaLabel start = new CMaLabel(), end = new CMaLabel();
                pw.visit(start);
                pw.visit(LOADA, idx); pw.visit(LOADA, listSize); pw.visit(EQ); pw.visit(NOT); pw.visit(JUMPZ, end);

                // Laeme käesoleva elemendi mälust ja seome selle query muutujaga.
                pw.visit(LOADA, listAddr); pw.visit(LOADC, 1); pw.visit(ADD); pw.visit(LOADA, idx); pw.visit(ADD); pw.visit(LOAD); pw.visit(STOREA, loopVar);

                // WHERE filtreerib elemente, SELECT teisendab elemente.
                if (q.type().equals("where")) {

                    // Kui tingimus on tõene, lisatakse element uude listi.
                    compile(q.right()); CMaLabel cont = new CMaLabel(); pw.visit(JUMPZ, cont);
                    append(loopVar, newListSize); pw.visit(cont);
                } else {

                    // SELECT arvutab uue väärtuse ja salvestab selle tulemuste listi.
                    compile(q.right()); int temp = nextReg++; pw.visit(STOREA, temp);
                    append(temp, newListSize); nextReg--;
                }

                // Liigume järgmise elemendi juurde.
                pw.visit(LOADA, idx); pw.visit(LOADC, 1); pw.visit(ADD); pw.visit(STOREA, idx); pw.visit(JUMP, start);
                pw.visit(end);

                // Salvestame tulemuste listi pikkuse ja jätame selle aadressi pinule.
                pw.visit(LOADA, newListSize); pw.visit(LOADA, newListAddr); pw.visit(STORE); pw.visit(LOADA, newListAddr);
                
                if (oldVarIdx == null) env.remove(varName); else env.put(varName, oldVarIdx);
                nextReg -= 6;
            }

            case SqeliteAvg a -> compileAggr(a.expression(), "avg");
            case SqeliteMin m -> compileAggr(m.expression(), "min");
            case SqeliteMax m -> compileAggr(m.expression(), "max");

            case SqeliteWhere _ -> pw.visit(HALT);
            case SqeliteSelect _ -> pw.visit(HALT);
        }
    }

    private void append(int valReg, int sizeReg) {
        // Kirjutame uue elemendi heap'i järgmisele vabale aadressile.
        pw.visit(LOADA, valReg); pw.visit(LOADA, HEAP_PTR_IDX); pw.visit(STORE);

        // Nihutame heap'i osuti järgmisele vabale kohale.
        pw.visit(LOADA, HEAP_PTR_IDX); pw.visit(LOADC, 1); pw.visit(ADD); pw.visit(STOREA, HEAP_PTR_IDX);

        // Suurendame tulemuste listi pikkust ühe võrra.
        pw.visit(LOADA, sizeReg); pw.visit(LOADC, 1); pw.visit(ADD); pw.visit(STOREA, sizeReg);
    }

    private void compileAggr(SqeliteNode expr, String type) {
        // Kompileerime avaldise, mille tulemuseks peab olema list.
        compile(expr);

        // Registrid listi aadressi, pikkuse, indeksi hoidmiseks.
        int listAddr = nextReg++, listSize = nextReg++, idx = nextReg++, acc = nextReg++;

        // Loeme listi aadressi ning selle pikkuse.
        pw.visit(STOREA, listAddr); pw.visit(LOADA, listAddr); pw.visit(LOAD); pw.visit(STOREA, listSize);

        // Tühja listi korral katkestame programmi (HALT).
        CMaLabel empty = new CMaLabel(); pw.visit(LOADA, listSize); pw.visit(JUMPZ, empty);

        // Algväärtustame: avg jaoks 0, min/max jaoks esimene element.
        if (type.equals("avg")) pw.visit(LOADC, 0);
        else { pw.visit(LOADA, listAddr); pw.visit(LOADC, 1); pw.visit(ADD); pw.visit(LOAD); }
        pw.visit(STOREA, acc);

        // Avg alustab esimesest elemendist, min/max teisest
        pw.visit(LOADC, type.equals("avg") ? 0 : 1); pw.visit(STOREA, idx);

        // Aggregatsiooni põhitsükkel.
        CMaLabel start = new CMaLabel(), end = new CMaLabel(); pw.visit(start);
        pw.visit(LOADA, idx); pw.visit(LOADA, listSize); pw.visit(EQ); pw.visit(NOT); pw.visit(JUMPZ, end);

        // Laeme käesoleva elemendi mälust.
        pw.visit(LOADA, listAddr); pw.visit(LOADC, 1); pw.visit(ADD); pw.visit(LOADA, idx); pw.visit(ADD); pw.visit(LOAD);

        // Uuendame vastavalt aggregatsiooni tüübile.
        if (type.equals("avg")) {
            // Avg: liidame elemendi jooksvale summale.
            pw.visit(LOADA, acc); pw.visit(ADD); pw.visit(STOREA, acc);
        }

        else {
            // Min/Max: võrdleme elementi senise parima väärtusega.
            int elem = nextReg++; pw.visit(STOREA, elem); pw.visit(LOADA, elem); pw.visit(LOADA, acc);

            pw.visit(type.equals("min") ? LE : GR); CMaLabel skip = new CMaLabel(); pw.visit(JUMPZ, skip);
            pw.visit(LOADA, elem); pw.visit(STOREA, acc); pw.visit(skip); nextReg--;
        }

        // Liigume järgmise elemendi juurde.
        pw.visit(LOADA, idx); pw.visit(LOADC, 1); pw.visit(ADD); pw.visit(STOREA, idx); pw.visit(JUMP, start);
        pw.visit(end);

        // Tsükli lõpus arvutame lõpptulemuse.
        if (type.equals("avg")) { pw.visit(LOADA, acc); pw.visit(LOADA, listSize); pw.visit(DIV); }
        else pw.visit(LOADA, acc);
        CMaLabel done = new CMaLabel(); pw.visit(JUMP, done);

        // Tühja listi aggregatsioon ei ole lubatud.
        pw.visit(empty); pw.visit(HALT); pw.visit(done); nextReg -= 4;
    }

    private String findFirstVarName(SqeliteNode node) {
        return switch (node) {
            case SqeliteVar v -> v.name(); case SqeliteWhere w -> findFirstVarName(w.condition());
            case SqeliteSelect s -> findFirstVarName(s.expression());
            case SqeliteBinOp b -> { String l = findFirstVarName(b.left()); yield l != null ? l : findFirstVarName(b.right()); }
            default -> null;
        };
    }
}
