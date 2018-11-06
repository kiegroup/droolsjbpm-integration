package org.acme.test_generateModel_kjararchetype;

import java.math.BigDecimal;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;

import static org.acme.test_generateModel_kjararchetype.Utils.b;
import static org.acme.test_generateModel_kjararchetype.Utils.entry;
import static org.acme.test_generateModel_kjararchetype.Utils.mapOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import static org.junit.Assert.assertThat;
public class DMNTest {

    @Test
    public void testBasic() {
        KieServices kieServices = KieServices.Factory.get();

        KieContainer kieContainer = kieServices.getKieClasspathContainer();

        DMNRuntime dmnRuntime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);

        DMNModel dmnModel = dmnRuntime.getModel("http://www.trisotech.com/dmn/definitions/_73732c1d-f5ff-4219-a705-f551a5161f88", "Bank monthly fees");

        DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("Account holder", mapOf(entry("age", b(36)),
                                               entry("employed", true)));
        dmnContext.set("Account balance", 10000);
                                               
        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        assertResult(dmnResult);
    }

    public static void assertResult(DMNResult dmnResult) {
        System.out.println(dmnResult);

        for (DMNDecisionResult dr : dmnResult.getDecisionResults()) {
            System.out.println("Decision '" + dr.getDecisionName() + "' : " + dr.getResult());
        }

        assertThat(dmnResult.getDecisionResultByName("Account Profile").getResult(), is("Standard"));
        assertThat(dmnResult.getDecisionResultByName("Exemptions").getResult(), is("Standard"));
        assertThat((BigDecimal) dmnResult.getDecisionResultByName("Monthly fee").getResult(), closeTo(b(50), b(0)));
    }
}
