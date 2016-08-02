/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.client.jms;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.Wrapped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple blocking response callback backed by blocking queue that will allow sequential access to
 * responses and block
 * <ul>
 *     <li>client if there is no message yet</li>
 *     <li>server if the queue is full</li>
 * </ul>.
 */
public class BlockingResponseCallback implements ResponseCallback {

    private static final Logger logger = LoggerFactory.getLogger(BlockingResponseCallback.class);
    private BlockingQueue<ServiceResponsesList> responses;

    private Marshaller marshaller;

    public BlockingResponseCallback(Marshaller marshaller) {
        this.marshaller = marshaller;
        this.responses = new ArrayBlockingQueue<ServiceResponsesList>(10);
    }

    public BlockingResponseCallback(Marshaller marshaller, int queueSize) {
        this.marshaller = marshaller;
        this.responses = new ArrayBlockingQueue<ServiceResponsesList>(queueSize);
    }

    @Override
    public void onResponse(String selector, ServiceResponsesList response) {
        logger.debug("Message response {} for selector {} delivered to callback", response, selector);
        try {
            responses.put(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted exception while putting message to response queue in callback");
        }
    }

    @Override
    public ServiceResponsesList get() {
        try {
            return responses.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted exception while taking message from responses queue in callback");
        }

        return null;
    }

    @Override
    public <T> T get(Class<T> type) {
        if (marshaller == null) {
            throw new IllegalStateException("No marshaller given, can't use get(Class) to return response");
        }
        ServiceResponsesList responsesList = get();

        if (responsesList.getResponses() == null || responsesList.getResponses().isEmpty()) {
            logger.debug("No data found in the response, returning null");
            return null;
        }

        ServiceResponse response = responsesList.getResponses().get(0);
        if (response.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {

            Object result = response.getResult();

            if (result instanceof String) {
                logger.debug("Response '{}' of type string, unmarshalling it...", result);

                result = marshaller.unmarshall((String) result, type);
                logger.debug("Result after unmarshall operation {}", result);
            }
            // handle wrapped objects
            if (result instanceof Wrapped) {
                result = ((Wrapped)result).unwrap();
            }

            return (T)result;
        } else {
            logger.debug("Non successful response '{}', returning null", response.getMsg());
            return null;
        }
    }
}
