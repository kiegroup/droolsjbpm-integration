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

import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ServiceResponse;


@SuppressWarnings("rawtypes")
public class WebSocketServiceResponse extends ServiceResponse implements InternalMessageHandler {

    private CountDownLatch latch;
    private KieServiceResponse<?> result;
    private Function<String, KieServiceResponse<?>> handler;
    
    public WebSocketServiceResponse(boolean isBlocking, Function<String, KieServiceResponse<?>> handler) {
        this.handler = handler;
        if (isBlocking) {
            this.latch = new CountDownLatch(1);
        }
    }
    
    @Override
    public ResponseType getType() {
        return getWrapperResult().getType();
    }

    @Override
    public String getMsg() {
        return getWrapperResult().getMsg();
    }

    @Override
    public Object getResult() {
        return getWrapperResult().getResult();
    }

    public String onMessage(String message) {
        this.result = handler.apply(message);

        if (latch != null) {
           this.latch.countDown();
        }
        
        return null;
    }
    
    protected KieServiceResponse<?> getWrapperResult() {
        if (latch != null) {
            try {
                this.latch.await(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {                
            }
        }
        if(result == null){
            throw new RuntimeException("Service response not received");
        } else {
            return result;
        }
    }

    @Override
    public String toString() {
        return "WebSocketServiceResponse{" +
                "latch=" + latch +
                ", result=" + result +
                ", handler=" + handler +
                "} " + super.toString();
    }
}
