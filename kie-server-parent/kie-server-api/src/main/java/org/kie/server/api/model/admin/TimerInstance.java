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

package org.kie.server.api.model.admin;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "timer-instance")
public class TimerInstance {

    @XmlElement(name="name")
    private String timerName;
    @XmlElement(name="id")
    private long timerId;
    @XmlElement(name="activation-time")
    private Date activationTime;
    @XmlElement(name="last-fire-time")
    private Date lastFireTime;
    @XmlElement(name="next-fire-time")
    private Date nextFireTime;
    @XmlElement(name="delay")
    private long delay;
    @XmlElement(name="period")
    private long period;
    @XmlElement(name="repeat-limit")
    private int repeatLimit;
    @XmlElement(name="process-instance-id")
    private long processInstanceId;
    @XmlElement(name="session-id")
    private long sessionId;

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public long getTimerId() {
        return timerId;
    }

    public void setTimerId(long timerId) {
        this.timerId = timerId;
    }

    public Date getActivationTime() {
        return activationTime;
    }

    public void setActivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }

    public Date getLastFireTime() {
        return lastFireTime;
    }

    public void setLastFireTime(Date lastFireTime) {
        this.lastFireTime = lastFireTime;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public int getRepeatLimit() {
        return repeatLimit;
    }

    public void setRepeatLimit(int repeatLimit) {
        this.repeatLimit = repeatLimit;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private TimerInstance timerInstance = new TimerInstance();

        public TimerInstance build() {
            return timerInstance;
        }

        public Builder timerName(String name) {
            timerInstance.setTimerName(name);
            return this;
        }

        public Builder timerId(long timerId) {
            timerInstance.setTimerId(timerId);
            return this;
        }

        public Builder activationTime(Date activationTime) {
            timerInstance.setActivationTime(activationTime);
            return this;
        }

        public Builder lastFireTime(Date lastFireTime) {
            timerInstance.setLastFireTime(lastFireTime);
            return this;
        }

        public Builder nextFireTime(Date nextFireTime) {
            timerInstance.setNextFireTime(nextFireTime);
            return this;
        }

        public Builder delay(long delay) {
            timerInstance.setDelay(delay);
            return this;
        }

        public Builder period(long period) {
            timerInstance.setPeriod(period);
            return this;
        }

        public Builder repeatLimit(int repeatLimit) {
            timerInstance.setRepeatLimit(repeatLimit);
            return this;
        }

        public Builder processInstanceId(long processInstanceId) {
            timerInstance.setProcessInstanceId(processInstanceId);
            return this;
        }

        public Builder sessionId(long sessionId) {
            timerInstance.setSessionId(sessionId);
            return this;
        }
    }

    @Override
    public String toString() {
        return "TimerInstance{" +
                "timerName='" + timerName + '\'' +
                ", timerId=" + timerId +
                ", activationTime=" + activationTime +
                ", lastFireTime=" + lastFireTime +
                ", nextFireTime=" + nextFireTime +
                ", delay=" + delay +
                ", period=" + period +
                ", repeatLimit=" + repeatLimit +
                ", processInstanceId=" + processInstanceId +
                ", sessionId=" + sessionId +
                '}';
    }
}
