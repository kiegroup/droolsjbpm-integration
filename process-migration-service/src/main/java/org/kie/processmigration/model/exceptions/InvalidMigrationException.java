package org.kie.processmigration.model.exceptions;

public class InvalidMigrationException extends Exception {

    private static final long serialVersionUID = 7144906219120602668L;

    public InvalidMigrationException(String reason) {
        super(reason);
    }

    @Override
    public String getMessage() {
        return String.format("Invalid migration: %s", super.getMessage());
    }
}
