/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.runtime.pipeline.impl;

import org.drools.runtime.pipeline.Stage;
import org.drools.runtime.pipeline.StageExceptionHandler;

public class BaseStage implements Stage {
    private StageExceptionHandler exceptionHandler;

    public BaseStage() {
        super();
    }
    
    public void setStageExceptionHandler(StageExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    
    protected void handleException(Stage stage, Object object, Exception exception) {
        if ( this.exceptionHandler != null ) {
            this.exceptionHandler.handleException( stage, object, exception );
        } else {
            throw new RuntimeException( exception );
        }
    }

}
