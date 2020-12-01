/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.data;

import java.io.Serializable;

import org.kie.api.remote.Remotable;

@Remotable
public class Money implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int amount;
    
    private String currency;

    public Money() {
    }

    public int getAmount() {
        return amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public Money(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + amount;
        if (currency != null) {
            result = 31 * result + currency.hashCode();
        }
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Money))
            return false;
        Money other = (Money)o;
        boolean currencyEquals = (this.currency == null && other.currency == null)
          || (this.currency != null && this.currency.equals(other.currency));
        return this.amount == other.amount && currencyEquals;
    }
    
    @Override
    public String toString() {
        return "Money [amount=" + amount + ", currency=" + currency + "]";
    }

}