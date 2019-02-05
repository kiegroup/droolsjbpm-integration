/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.dmn;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult.DecisionEvaluationStatus;
import org.kie.dmn.core.impl.DMNContextImpl;
import org.kie.dmn.core.impl.DMNDecisionResultImpl;
import org.kie.dmn.core.impl.DMNResultImpl;

public class DMNResultKSStubTest {

    @Test
    public void testDMNContextContainingListAndSublist() {
        // DROOLS-2713 DMN evaluation response containing list and sublist fail on server
        DMNContext ctx = new DMNContextImpl();
        List<?> list = IntStream.range(1, 11).boxed().collect(Collectors.toList());
        List<?> sublist = list.subList(1, 3);
        ctx.set("list", list);
        ctx.set("sublist", sublist);

        DMNResultImpl dmnResults = new DMNResultImpl(null);
        dmnResults.setContext(ctx);
        dmnResults.addDecisionResult(new DMNDecisionResultImpl("list", "list", DecisionEvaluationStatus.SUCCEEDED, list, Collections.emptyList()));
        dmnResults.addDecisionResult(new DMNDecisionResultImpl("sublist", "sublist", DecisionEvaluationStatus.SUCCEEDED, sublist, Collections.emptyList()));

        DMNResultKS results = new DMNResultKS(dmnResults);
        Assertions.assertThat(results.getContext().get("list")).isEqualTo(list);
        Assertions.assertThat(results.getContext().get("sublist")).isEqualTo(sublist);
        Assertions.assertThat(results.getDecisionResultByName("list").getResult()).isEqualTo(list);
        Assertions.assertThat(results.getDecisionResultByName("sublist").getResult()).isEqualTo(sublist);
    }
}
