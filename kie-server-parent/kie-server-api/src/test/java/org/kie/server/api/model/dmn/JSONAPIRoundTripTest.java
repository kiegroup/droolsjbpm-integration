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

package org.kie.server.api.model.dmn;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult.DecisionEvaluationStatus;
import org.kie.dmn.core.impl.DMNContextImpl;
import org.kie.dmn.core.impl.DMNDecisionResultImpl;
import org.kie.dmn.core.impl.DMNResultImpl;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONAPIRoundTripTest {

    private static final Logger LOG = LoggerFactory.getLogger(JSONAPIRoundTripTest.class);
    private Marshaller marshaller;

    @Before
    public void init() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(Applicant.class);
        marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
    }

    @Test
    public void test() {
        Applicant applicant = new Applicant("John Doe", 47);
        Applicant applicant2 = roundTrip(applicant);
        Assertions.assertThat(applicant2).isEqualTo(applicant);

        DMNContext ctx = new DMNContextImpl();
        ctx.set("Applicant", applicant); // VERY IMPORTANT that the map key is "Applicant" with the capital-A to clash with Application.class simple name.

        DMNContextKS contextKS = new DMNContextKS("ns1", "model1", ctx.getAll());
        DMNContextKS contextKS2 = roundTrip(contextKS);
        Assertions.assertThat(contextKS2.getDmnContext()).isEqualTo(contextKS.getDmnContext());
        Assertions.assertThat(contextKS2.toString()).isEqualTo(contextKS.toString());

        DMNResultImpl dmnResults = new DMNResultImpl(null);
        dmnResults.setContext(ctx);
        dmnResults.addDecisionResult(new DMNDecisionResultImpl("decision", "decision", DecisionEvaluationStatus.SUCCEEDED, applicant, Collections.emptyList()));

        DMNResultKS resultsKS = new DMNResultKS(dmnResults);
        DMNResultKS resultsKS2 = roundTrip(resultsKS);
        Assertions.assertThat(resultsKS2.getContext().getAll()).isEqualTo(resultsKS.getContext().getAll());

        ServiceResponse<DMNResultKS> sr = new ServiceResponse<DMNResultKS>(ResponseType.SUCCESS, "ok", resultsKS);
        ServiceResponse<DMNResultKS> sr2 = roundTrip(sr);
        Assertions.assertThat(sr2.getResult().getContext().getAll()).isEqualTo(sr.getResult().getContext().getAll());
    }

    private <T> T roundTrip(T input) {
        String asJSON = marshaller.marshall(input);
        LOG.debug("{}", asJSON);
        @SuppressWarnings("unchecked") // intentional due to type-erasure.
        T unmarshall = (T) marshaller.unmarshall(asJSON, input.getClass());
        LOG.debug("{}", unmarshall);
        return unmarshall;
    }

    public static class Applicant {

        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Applicant() {

        }

        public Applicant(String name, int age) {
            super();
            this.name = name;
            this.age = age;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + age;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Applicant other = (Applicant) obj;
            if (age != other.age) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Applicant [name=" + name + ", age=" + age + "]";
        }

    }
}
