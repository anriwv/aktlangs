package toylangs;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

public class AbstractNodeVisualizer {

    private int nextNodeId = 0;

    private Node makeNode(String label) {
        return node(Integer.toString(nextNodeId++)).with(Label.of(label));
    }

    private Node visit(Object object) {
        return switch (object) {
            case AbstractNode node -> visit(node);
            default -> makeNode(Objects.toString(object));
        };
    }

    private Node visit(AbstractNode node) {
        return makeNode(node.getNodeLabel()).link(
                node.getChildren().stream()
                        .flatMap(AbstractNodeVisualizer::flattenChild)
                        .filter(Objects::nonNull)
                        .map(this::visit)
                        .toList()
        );
    }

    private static Stream<?> flattenChild(Object child) {
        if (child instanceof Collection<?> collection)
            return collection.stream();
        else if (child != null && child.getClass().isArray())
            return Arrays.stream((Object[]) child);
        else
            return Stream.of(child);
    }

    public static void renderPngFile(AbstractNode node, Path path) throws IOException {
        Node rootNode = new AbstractNodeVisualizer().visit(node);
        Graph graph = graph("tree").directed().with(rootNode).graphAttr().with("ordering", "out");
        //System.out.println(Graphviz.fromGraph(graph).render(Format.DOT));
        Graphviz.fromGraph(graph).scale(3).render(Format.PNG).toFile(path.toFile().getCanonicalFile());
    }
}
