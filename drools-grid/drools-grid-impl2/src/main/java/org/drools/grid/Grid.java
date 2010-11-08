package org.drools.grid;

public interface Grid {
    public <T> T get(Class<T> serviceClass);

    public GridNode createGridNode(String id);

    public GridNode createGridNode(GridServiceDescription gsd);

    public GridNode getGridNode(String id);

    public GridNodeConnection getGridNodeConnection(GridServiceDescription serviceDescription);
}
