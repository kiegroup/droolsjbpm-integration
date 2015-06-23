/*
 * Copyright 2015 JBoss Inc
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

package org.drools.benchmark.model;

import java.util.Date;
import java.util.Random;

public class Bean {

    private String s;
    private int i;
    private long l;
    private double d;
    private boolean b;
    private Date date;

    public String getS() {
        return s;
    }
    public void setS(String s) {
        this.s = s;
    }

    public int getI() {
        return i;
    }
    public void setI(int i) {
        this.i = i;
    }

    public long getL() {
        return l;
    }
    public void setL(long l) {
        this.l = l;
    }

    public double getD() {
        return d;
    }
    public void setD(double d) {
        this.d = d;
    }

    public boolean isB() {
        return b;
    }
    public void setB(boolean b) {
        this.b = b;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isEnabled(Bean bean) {
        return false;
    }

    public static Bean[] generateRandomBeans(int nr) {
        Bean[] beans = new Bean[nr];
        Random random = new Random(nr);

        for (int i = 0; i < nr; i++) {
            beans[i] = new Bean();
            beans[i].setS("" + random.nextInt());
            beans[i].setI(random.nextInt());
            beans[i].setL(random.nextLong());
            beans[i].setD(random.nextDouble());
            beans[i].setB(random.nextBoolean());
            beans[i].setDate(new Date(random.nextLong()));
        }

        return beans;
    }

}
