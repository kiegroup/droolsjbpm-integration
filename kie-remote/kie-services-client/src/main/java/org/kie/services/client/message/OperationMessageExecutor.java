package org.kie.services.client.message;

public abstract class OperationMessageExecutor {

    protected Object executeOperation(OperationMessage request, Object serviceInstance) {
        Object result = null;
        try {
            result = request.getMethod().invoke(serviceInstance, request.getArgs());
        } catch (Exception e ) {
            handleException(request, e);
        }

        return result;
    }
    
    protected abstract void handleException(OperationMessage request, Exception e);
    
}
