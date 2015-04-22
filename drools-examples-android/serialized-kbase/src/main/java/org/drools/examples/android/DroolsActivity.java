/*
 * Copyright 2015 JBoss by Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.examples.android;

import android.app.Activity;
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
import org.drools.android.DroolsAndroidContext;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.examples.android.serialized.R;
import org.drools.examples.helloworld.Message;
import org.kie.api.KieBase;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Example showing loading a pre-serialized knowledge base with drools-core dependency.
 */
public class DroolsActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(DroolsActivity.class);

    private Button fireRulesButton;
    private TextView logView;

    private String log = "";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            logView.setText(log);
        }
    };

    private KieBase mKnowledgeBase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BasicLogcatConfigurator.configureDefaultContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        fireRulesButton = (Button) findViewById(R.id.fireRules);
        logView = (TextView) findViewById(R.id.log);

        //Initialize android context and set system properties
        DroolsAndroidContext.setContext(this);

        //load serialized KnowledgePackages
        new LoadRulesTask().execute(getResources().openRawResource(R.raw.hellokb));

        fireRulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FireRulesTask().execute();
            }
        });
    }

    private class LoadRulesTask extends AsyncTask<InputStream, Void, KieBase> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DroolsActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected KieBase doInBackground(InputStream... params) {
            try {
                logger.debug("Loading knowledge base");
                final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
                kbase.addKnowledgePackages((List<KnowledgePackage>) DroolsStreamUtils.streamIn(params[0]));
                return kbase;
            }catch(Exception e) {
                logger.error("Drools exception", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(KieBase result) {
            mKnowledgeBase = result;
            DroolsActivity.this.setProgressBarIndeterminateVisibility(false);
        }
    }

    private class FireRulesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DroolsActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                logger.debug("Firing rules");
                final Message message = new Message();
                message.setMessage("Hello World");
                message.setStatus(Message.HELLO);

                final StatelessKieSession ksession = mKnowledgeBase.newStatelessKieSession();
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
