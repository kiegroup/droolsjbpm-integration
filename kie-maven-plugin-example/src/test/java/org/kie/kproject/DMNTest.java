package org.kie.kproject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.util.DMNRuntimeUtil;
import org.kie.dmn.feel.codegen.feel11.CompilerBytecodeLoader;
import org.kie.kproject.util.MockEventListener;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import static org.junit.Assert.*;
import static org.kie.dmn.core.util.DynamicTypeUtils.entry;
import static org.kie.dmn.core.util.DynamicTypeUtils.mapOf;
import static org.kie.kproject.util.Utils.b;

public class DMNTest {

    @Test
    public void testBasic() {
        List<CompilationUnit> generatedClasses = new ArrayList<>();
        CompilerBytecodeLoader.generateClassListener = generatedClasses::add;
        KieServices kieServices = KieServices.Factory.get();

        KieContainer kieContainer = kieServices.getKieClasspathContainer();

        DMNRuntime dmnRuntime = kieContainer.newKieSession("dmnTest.session").getKieRuntime(DMNRuntime.class);

        System.out.println("dmnRuntime.getModels() = " + dmnRuntime.getModels());

        DMNModel dmnModel = dmnRuntime.getModel("http://www.trisotech.com/dmn/definitions/_73732c1d-f5ff-4219-a705-f551a5161f88", "Bank monthly fees");

        DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("Account holder", mapOf(entry("age", b(36)),
                                               entry("employed", true)));
        dmnContext.set("Account balance", 10000);

        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        assertResult(dmnResult);
        assertThat(generatedClasses, is(empty()));
    }

    @Test
    public void testSolutionCase1() {
        List<CompilationUnit> generatedClasses = new ArrayList<>();
        CompilerBytecodeLoader.generateClassListener = generatedClasses::add;
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime("org/kie/example/0020-vacation-days.dmn", this.getClass());
        DMNModel dmnModel = runtime.getModel("https://www.drools.org/kie-dmn", "0020-vacation-days");
        assertThat(dmnModel, notNullValue());

        DMNContext context = DMNFactory.newContext();

        context.set("Age", 16);
        context.set("Years of Service", 1);

        DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);

        DMNContext result = dmnResult.getContext();

        assertThat(result.get("Total Vacation Days"), is(BigDecimal.valueOf(27)));
        assertThat(generatedClasses, is(empty()));
    }

    @Test
    public void testDecisionTableDefaultValue() {
        List<CompilationUnit> generatedClasses = new ArrayList<>();
        CompilerBytecodeLoader.generateClassListener = generatedClasses::add;

        final DMNRuntime runtime = DMNRuntimeUtil.createRuntime("decisiontable-default-value.dmn", this.getClass());
        final MockEventListener listener = new MockEventListener();
        runtime.addListener(listener);

        final DMNModel dmnModel = runtime.getModel("https://github.com/kiegroup/kie-dmn", "decisiontable-default-value");
        assertThat(dmnModel, notNullValue());
        assertThat(dmnModel.getMessages().toString(), dmnModel.hasErrors(), is(false));

        final DMNContext context = DMNFactory.newContext();
        context.set("Age", new BigDecimal(16));
        context.set("RiskCategory", "Medium");
        context.set("isAffordable", true);

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        assertThat(dmnResult.getMessages().toString(), dmnResult.hasErrors(), is(false));

        final DMNContext result = dmnResult.getContext();
        assertThat(result.get("Approval Status"), is("Declined"));

        assertThat(listener.getMatches(), is(empty()));
        assertThat(listener.getSelected(), is(empty()));
        assertThat(generatedClasses, is(empty()));
    }

    @Test
    public void testDMNv1_2_ch11Modified() {
        List<CompilationUnit> generatedClasses = new ArrayList<>();
        CompilerBytecodeLoader.generateClassListener = generatedClasses::add;
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntime("v1_2/ch11MODIFIED.dmn", this.getClass());
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_3068644b-d2c7-4b81-ab9d-64f011f81f47", "DMN Specification Chapter 11 Example");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        final DMNContext context = DMNFactory.newContext();
        context.set("Applicant data", mapOf(entry("Age", new BigDecimal(51)),
                                            entry("MaritalStatus", "M"),
                                            entry("EmploymentStatus", "EMPLOYED"),
                                            entry("ExistingCustomer", false),
                                            entry("Monthly", mapOf(entry("Income", new BigDecimal(100_000)),
                                                                   entry("Repayments", new BigDecimal(2_500)),
                                                                   entry("Expenses", new BigDecimal(10_000)))))); // DMN v1.2 spec page 181, first image: errata corrige values for Income and Expenses are likely inverted, corrected here.
        context.set("Bureau data", mapOf(entry("Bankrupt", false),
                                         entry("CreditScore", new BigDecimal(600))));
        context.set("Requested product", mapOf(entry("ProductType", "STANDARD LOAN"),
                                               entry("Rate", new BigDecimal(0.08)),
                                               entry("Term", new BigDecimal(36)),
                                               entry("Amount", new BigDecimal(100_000))));
        context.set("Supporting documents", null);
        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        assertThat(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()), dmnResult.hasErrors(), is(false));

        final DMNContext result = dmnResult.getContext();
        assertThat(result.get("Strategy"), is("THROUGH"));
        assertThat(result.get("Routing"), is("ACCEPT"));
        assertThat(generatedClasses, is(empty()));
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
