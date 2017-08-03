/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.websocket.common.handlers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.kie.server.api.model.ServiceResponse;


@SuppressWarnings("rawtypes")
public class WebsocketServiceResponse extends ServiceResponse implements InternalMessageHandler {

    private CountDownLatch latch;
    private ServiceResponse<?> result;
    private Function<String, ServiceResponse<?>> handler;
    
    
    public WebsocketServiceResponse(boolean isBlocking, Function<String, ServiceResponse<?>> handler) {
        this.handler = handler;
        if (isBlocking) {
            this.latch = new CountDownLatch(1);
        }
    }
    
    @Override
    public ResponseType getType() {
        waitIfNeeded();
        return result.getType();
    }

    @Override
    public String getMsg() {
        waitIfNeeded();
        return result.getMsg();
    }

    @Override
    public Object getResult() {
        waitIfNeeded();
        return result.getResult();
    }

    public String onMessage(String message) {
        this.result = handler.apply(message);
        
        if (latch != null) {
           this.latch.countDown();
        }
        
        return null;
    }
    
    protected void waitIfNeeded() {
        if (latch != null) {
            try {
                this.latch.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {                
            }
        }
    }
}
