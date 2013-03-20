package org.jbpm.simulation;

import java.util.List;

public interface PathFormatConverter<E> {

    E convert(List<PathContext> paths);
}
