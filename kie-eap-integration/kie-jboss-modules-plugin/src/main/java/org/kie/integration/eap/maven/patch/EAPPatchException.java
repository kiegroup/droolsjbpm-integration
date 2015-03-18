package org.kie.integration.eap.maven.patch;

public class EAPPatchException extends Exception {
    private String patchId;

    public EAPPatchException(String message, String patchId) {
        super(message);
        this.patchId = patchId;
    }

    public EAPPatchException(String message, Throwable cause, String patchId) {
        super(message, cause);
        this.patchId = patchId;
    }

    public EAPPatchException(Throwable cause, String patchId) {
        super(cause);
        this.patchId = patchId;
    }
}
