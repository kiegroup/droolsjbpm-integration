package org.drools.examples.android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ch.qos.logback.classic.android.BasicLogcatConfigurator;
import org.drools.examples.android.roboguice.serialized.R;
import org.drools.examples.helloworld.Message;
import org.kie.api.KieBase;
import org.kie.api.cdi.KBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.List;

/**
 * Example showing injecting a preserialized knowledge base with drools-core dependency and roboguice.
 */
@ContentView(R.layout.main)
public class DroolsActivity extends RoboActivity {

    private static final Logger logger = LoggerFactory.getLogger(DroolsActivity.class);

    @InjectView(R.id.fireRules)
    private Button fireRulesButton;
    @InjectView(R.id.log)
    private TextView logView;

    private String log = "";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            logView.setText(log);
        }
    };

    //load serialized KnowledgePackages
    @KBase("HelloKB")
    private KieBase kBase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BasicLogcatConfigurator.configureDefaultContext();
        super.onCreate(savedInstanceState);

        for(KiePackage pkg : kBase.getKiePackages()) {
            logger.debug("Loaded rule package: " + pkg.toString());
            for (Rule rule : pkg.getRules()) {
                logger.debug("Rule: " + rule.getName());
            }
        }

        fireRulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FireRulesTask().execute();
            }
        });
    }

    private class FireRulesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            logger.debug("Processing rules");
            DroolsActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                logger.debug("Firing rules");
                final Message message = new Message();
                message.setMessage("Hello World");
                message.setStatus(Message.HELLO);

                final StatelessKieSession ksession = kBase.newStatelessKieSession();
                ksession.addEventListener(new DebugAgendaEventListener());
                ksession.addEventListener(new DebugRuleRuntimeEventListener());
                ksession.registerChannel("log", new Channel() {
                    @Override
                    public void send(Object object) {
                        log += object.toString() + "\n";
                        handler.obtainMessage().sendToTarget();
                    }
                });
                ksession.execute(message);
            }catch(Exception e) {
                logger.error("Drools exception", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            DroolsActivity.this.setProgressBarIndeterminateVisibility(false);
        }
    }
}
