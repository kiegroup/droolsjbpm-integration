package org.drools.benchmark.benchmarks;

import org.drools.KnowledgeBase;
import org.drools.PackageIntegrationException;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.RuleIntegrationException;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.benchmark.BenchmarkDefinition;
import org.drools.benchmark.model.manners.Count;
import org.drools.benchmark.model.waltz.Line;
import org.drools.benchmark.model.waltz.Stage;
import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsError;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.lang.descr.PackageDescr;
import org.drools.rule.*;
import org.drools.rule.Pattern;
import org.drools.runtime.StatefulKnowledgeSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.*;

public class WaltzBenchmark extends AbstractBenchmark {

    private RuleBase ruleBase;
    private List<Line> lines = new ArrayList<Line>();

    @Override
    public void init(BenchmarkDefinition definition) {
        try {
            ruleBase = readRule();
            loadLines("/waltz50.dat");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(int repNr) {
        StatefulSession session = ruleBase.newStatefulSession();
        for (Line l : lines) {
            session.insert( l );
        }
        session.insert( new Stage( Stage.DUPLICATE ) );
        session.fireAllRules();
        session.dispose();
    }

    private RuleBase readRule() throws Exception,
            DroolsParserException,
            RuleIntegrationException,
            PackageIntegrationException,
            InvalidPatternException {
        //read in the source
        final Reader reader = new InputStreamReader( WaltzBenchmark.class.getResourceAsStream( "/waltz.drl" ) );
        final DrlParser parser = new DrlParser();
        final PackageDescr packageDescr = parser.parse( reader );

        //pre build the package
        final PackageBuilder builder = new PackageBuilder();
        builder.addPackage( packageDescr );
        final org.drools.rule.Package pkg = builder.getPackage();

        //add the package to a rulebase
        final RuleBase ruleBase = RuleBaseFactory.newRuleBase(RuleBase.RETEOO);
        ruleBase.addPackage( pkg );
        return ruleBase;
    }

    private void loadLines(final String filename) throws IOException {
        if (!lines.isEmpty()) {
            return;
        }

        BufferedReader reader = new BufferedReader( new InputStreamReader( WaltzBenchmark.class.getResourceAsStream( filename ) ) );
        java.util.regex.Pattern pat = java.util.regex.Pattern.compile(".*make line \\^p1 ([0-9]*) \\^p2 ([0-9]*).*");
        String line = reader.readLine();

        while ( line != null ) {
            final Matcher m = pat.matcher( line );
            if ( m.matches() ) {
                Line l = new Line( Integer.parseInt( m.group( 1 ) ), Integer.parseInt( m.group( 2 ) ) );
                lines.add( l );
            }
            line = reader.readLine();
        }
        reader.close();
    }
}
