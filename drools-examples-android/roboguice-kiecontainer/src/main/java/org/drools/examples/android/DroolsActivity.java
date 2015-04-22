package org.drools.examples.android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ch.qos.logback.classic.android.BasicLogcatConfigurator;
import org.drools.examples.android.roboguice.kiecontainer.R;
import org.drools.examples.helloworld.Message;
import org.kie.api.KieBase;
import org.kie.api.cdi.KBase;
import org.kie.api.cdi.KContainer;
import org.kie.api.cdi.KSession;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import javax.inject.Inject;

/**
 * Example showing injecting KieContainers, KieBases, and KSession with drools-compiler dependency and roboguice.
 */
@ContentView(R.layout.main)
public class DroolsActivity extends RoboActivity {
    private static final Logger logger = LoggerFactory.getLogger(DroolsActivity.class);

    static {
        System.setProperty("drools.dialect.java.compiler", "JANINO");
    }

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

    @Inject
    @KContainer
    KieContainer mContainer;

    @Inject
    @KBase("HelloKB")
    KieBase mKieBase;
    
    @Inject
    @KSession("android-session")
    StatelessKieSession kSession;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        BasicLogcatConfigurator.configureDefaultContext();
        super.onCreate(savedInstanceState);

        kSession.registerChannel("log", new Channel() {
            @Override
            public void send(Object object) {
                log += object.toString() + "\n";
                handler.obtainMessage().sendToTarget();
            }
        });
        
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
            DroolsActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            logger.debug("Firing rules");
            try{
                final Message message = new Message();
                message.setMessage("Hello World");
                message.setStatus(Message.HELLO);
                kSession.execute(message);
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
