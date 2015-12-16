/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
import org.drools.examples.android.kiecontainer.R;
import org.drools.examples.helloworld.Message;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example showing loading a KieContainer with drools-compiler dependency.
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

    private KieContainer mContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BasicLogcatConfigurator.configureDefaultContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        fireRulesButton = (Button) findViewById(R.id.fireRules);
        logView = (TextView) findViewById(R.id.log);

        DroolsAndroidContext.setContext(this);

        new LoadContainerTask().execute();

        fireRulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FireRulesTask().execute();
            }
        });
    }

    private class LoadContainerTask extends AsyncTask<Void, Void, KieContainer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DroolsActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected KieContainer doInBackground(Void... params) {
            try {
                logger.debug("Loading Classpath container");
                return KieServices.Factory.get().getKieClasspathContainer();
            }catch(Exception e) {
                logger.error("Drools exception", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(KieContainer result) {
            DroolsActivity.this.setProgressBarIndeterminateVisibility(false);
            mContainer = result;
        }
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
            logger.debug("Firing rules");
            try{
                final KieBase cBase = mContainer.getKieBase("HelloKB");
                final Message message = new Message();
                message.setMessage("Hello World");
                message.setStatus(Message.HELLO);
                final StatelessKieSession ksession = mContainer.newStatelessKieSession("android-session");
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
