package org.kie.server.api.marshalling;

public enum MarshallingFormat {
    XSTREAM(0, "xstream"), JAXB(1, "xml"), JSON(2, "json");

    private final int id;
    private final String type;

    MarshallingFormat( int id, String type ) {
        this.id = id;
        this.type = type;
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

    public static MarshallingFormat fromType( String type ) {
        if ("xstream".equals(type)) {
            return XSTREAM;
        } else if ("xml".equals(type)) {
            return JAXB;
        } else if ("json".equals(type)) {
            return JSON;
        } else {
            return null;
        }
    }
}
