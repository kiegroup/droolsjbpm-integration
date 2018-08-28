/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.springboot.autoconfigure;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jbpm")
public class JBPMProperties implements InitializingBean {

    private Executor executor = new Executor();
    private Quartz quartz = new Quartz();

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Quartz getQuartz() {
        return quartz;
    }

    public void setQuartz(Quartz quartz) {
        this.quartz = quartz;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public static class Executor {

        private int interval = 0;
        private int threadPoolSize = 1;
        private int retries = 3;
        private String timeUnit = TimeUnit.SECONDS.name();

        private boolean enabled;

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public int getThreadPoolSize() {
            return threadPoolSize;
        }

        public void setThreadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
        }

        public int getRetries() {
            return retries;
        }

        public void setRetries(int retries) {
            this.retries = retries;
        }

        public String getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(String timeUnit) {
            this.timeUnit = timeUnit;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Quartz {

        private int startDelay = 2;
        private int rescheduleDelay = 500;
        private int failedJobRetry = 5;
        private int failedJobDelay = 1000;

        private boolean enabled = false;
        private boolean db = false;
        private String configuration = "quartz.properties";

        public int getStartDelay() {
            return startDelay;
        }

        public void setStartDelay(int startDelay) {
            this.startDelay = startDelay;
        }

        public int getRescheduleDelay() {
            return rescheduleDelay;
        }

        public void setRescheduleDelay(int rescheduleDelay) {
            this.rescheduleDelay = rescheduleDelay;
        }

        public int getFailedJobRetry() {
            return failedJobRetry;
        }

        public void setFailedJobRetry(int failedJobRetry) {
            this.failedJobRetry = failedJobRetry;
        }

        public int getFailedJobDelay() {
            return failedJobDelay;
        }

        public void setFailedJobDelay(int failedJobDelay) {
            this.failedJobDelay = failedJobDelay;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isDb() {
            return db;
        }

        public void setDb(boolean db) {
            this.db = db;
        }

        public String getConfiguration() {
            return configuration;
        }

        public void setConfiguration(String configuration) {
            this.configuration = configuration;
        }

    }
}
