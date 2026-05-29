package toylangs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public interface AbstractNode {

    /**
     * Abimeetod klassile AbstractNodeVisualizer.
     * @return tipu klassinimi väiketähtedes ja ilma keelenimeta.
     */
    default String getNodeLabel() {
        String className = this.getClass().getSimpleName();
        return className.replaceFirst("[A-Z][a-z]*", "").toLowerCase();
    }

    /**
     * Abimeetod klassile AbstractNodeVisualizer.
     * @return järjend kõikidest välja kutsuva isendi alamtippudest
     */
    default List<Object> getChildren() {
        Class<?> clazz = this.getClass();
        if (!clazz.isRecord())
            throw new IllegalArgumentException("getChildren() oskab käsitleda vaid kirjeid (Record).");

        try {
            List<Object> children = new ArrayList<>();
            for (RecordComponent recordComponent : clazz.getRecordComponents()) {
                Object field = recordComponent.getAccessor().invoke(this);
                children.add(field);
            }
            return children;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param path failitee, kuhu loodav pilt salvestada.
     */
    default void renderPngFile(Path path) throws IOException {
        AbstractNodeVisualizer.renderPngFile(this, path);
    }
}
