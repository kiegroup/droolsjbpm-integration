package org.kie.server.api.marshalling;

public enum MarshallingFormat {
    XSTREAM(0), JAXB(1), JSON(2);

    private final int id;

    MarshallingFormat( int id ) {
        this.id = id;
    }

    public int getId() { return id; }

    public static MarshallingFormat fromId( int id ) {
        switch ( id ) {
            case 0 : return XSTREAM;
            case 1 : return JAXB;
            case 2 : return JSON;
            default: return null;
        }
    }
}
