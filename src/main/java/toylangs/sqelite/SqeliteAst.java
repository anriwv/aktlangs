package toylangs.sqelite;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import toylangs.sqelite.ast.SqeliteNode;
import toylangs.sqelite.ast.SqeliteAssign;
import toylangs.sqelite.ast.SqeliteProg;
import utils.ExceptionErrorListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static toylangs.sqelite.ast.SqeliteNode.*;

public class SqeliteAst {

    public static SqeliteNode makeSqeliteAst(String input) {
        SqeliteLexer lexer = new SqeliteLexer(CharStreams.fromString(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ExceptionErrorListener());

        SqeliteParser parser = new SqeliteParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());

        ParseTree tree = parser.init();
        return parseTreeToAst(tree);
    }

    private static SqeliteNode parseTreeToAst(ParseTree tree) {
        return new SqeliteBaseVisitor<SqeliteNode>() {

            @Override
            public SqeliteNode visitInit(SqeliteParser.InitContext ctx) { return visit(ctx.prog()); }

            @Override
            public SqeliteNode visitProg(SqeliteParser.ProgContext ctx) {
                List<SqeliteAssign> assigns = new ArrayList<>();
                for (var assignCtx : ctx.assignement()) {
                    assigns.add((SqeliteAssign) visit(assignCtx));
                }
                return prog(assigns, visit(ctx.expr()));
            }

            @Override
            public SqeliteNode visitAssignement(SqeliteParser.AssignementContext ctx) {
                return assign(ctx.VAR().getText(), visit(ctx.expr()));
            }

            @Override
            public SqeliteNode visitSqliteSource(SqeliteParser.SqliteSourceContext ctx) {
                List<Integer> elements = new ArrayList<>();
                for (var node : ctx.INT()) {
                    elements.add(Integer.parseInt(node.getText()));
                }
                return source(elements);
            }




            @Override
            public SqeliteNode visitSqliteVar(SqeliteParser.SqliteVarContext ctx) { return var(ctx.getText()); }

            @Override
            public SqeliteNode visitSqeliteMin(SqeliteParser.SqeliteMinContext ctx) { return min(ctx.expr() != null ? visit(ctx.expr()) : null); }

            @Override
            public SqeliteNode visitSqeliteMax(SqeliteParser.SqeliteMaxContext ctx) { return max(ctx.expr() != null ? visit(ctx.expr()) : null); }

            @Override
            public SqeliteNode visitSqeliteAvg(SqeliteParser.SqeliteAvgContext ctx) { return avg(ctx.expr() != null ? visit(ctx.expr()) : null); }

            @Override
            public SqeliteNode visitSqliteNum(SqeliteParser.SqliteNumContext ctx) { return num(Integer.parseInt(ctx.getText())); }

            @Override
            public SqeliteNode visitSqliteBinOp(SqeliteParser.SqliteBinOpContext ctx) { return
                    binOp(ctx.op.getText(), visit(ctx.left), visit(ctx.right)); }

            @Override
            public SqeliteNode visitParen(SqeliteParser.ParenContext ctx) { return visit(ctx.expr()); }

            @Override
            public SqeliteNode visitSqliteQuery(SqeliteParser.SqliteQueryContext ctx) {
                String op = ctx.SELECT() != null ? "select" : "where";
                return query(op, visit(ctx.expr(0)), visit(ctx.expr(1)));
            }
        }.visit(tree);
    }

    public static void main(String[] args) throws IOException {
        SqeliteNode ast = SqeliteAst.makeSqeliteAst("from [1, 2] select x * 2");
        System.out.println(ast);
    }
}
