/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.integration.tomcat.jta;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import jakarta.servlet.annotation.WebListener;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class KieNarayanaJtaServletContextListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(KieNarayanaJtaServletContextListener.class.getSimpleName());

    private static final String DEFAULT_NODE_IDENTIFIER = "1";

    private static final List<String> DEFAULT_RECOVERY_MODULES = Arrays.asList(AtomicActionRecoveryModule.class.getName(),
            XARecoveryModule.class.getName());

    private static final List<String> DEFAULT_ORPHAN_FILTERS = Arrays
            .asList(JTATransactionLogXAResourceOrphanFilter.class.getName(), JTANodeNameXAResourceOrphanFilter.class.getName());

    private static final List<String> DEFAULT_EXPIRY_SCANNERS = Arrays
            .asList(ExpiredTransactionStatusManagerScanner.class.getName());

    /**
     * Initialize and start Narayana JTA services.
     * <p>
     * During the setup node identifier, recovery modules, orphan filters, and expiry scanners are setup. Configuration file
     * will be used to get initial values. If one doesn't exist, following defaults will be used.
     * The settings from configuration file is transfered to {@link com.arjuna.ats.jta.common.JTAEnvironmentBean} where runtime
     * configuration resides.
     * <p>
     * <ul>
     * <li>Node identifier: "1"
     * <li>Recovery modules: {@link AtomicActionRecoveryModule}, {@link XARecoveryModule}
     * <li>Orphan filters: {@link JTATransactionLogXAResourceOrphanFilter}, {@link JTANodeNameXAResourceOrphanFilter}
     * <li>Expiry scanners: {@link ExpiredTransactionStatusManagerScanner}
     * </ul>
     * <p>
     * After setup recovery manager, transaction status manager, and transaction reaper are started.
     *
     * @param servletContextEvent containing the ServletContext that is being initialized
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.fine("Initializing Narayana");
        initNodeIdentifier();
        initRecoveryModules();
        initOrphanFilters();
        initExpiryScanners();
        RecoveryManager.manager().initialize();
        TxControl.enable();
        TransactionReaper.instantiate();
    }

    /**
     * First, stop recovery manager, transaction status manager, and transaction reaper. Then, remove transactional driver from
     * jdbc driver manager's list.
     *
     * @param servletContextEvent containing the ServletContext that is being destroyed
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        LOGGER.fine("Disabling Narayana");
        TransactionReaper.terminate(false);
        TxControl.disable(true);
        RecoveryManager.manager().terminate();
        Collections.list(DriverManager.getDrivers()).stream().filter(d -> d instanceof TransactionalDriver).forEach(d -> {
            try {
                DriverManager.deregisterDriver(d);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        });
    }

    /**
     * If node identifier wasn't set by property manager, then set default {@link #DEFAULT_NODE_IDENTIFIER}.
     */
    private void initNodeIdentifier() {
        if (arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier() == null) {
            LOGGER.warning("Node identifier was not set. Setting it to the default value: " + DEFAULT_NODE_IDENTIFIER);
            try {
                arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier(DEFAULT_NODE_IDENTIFIER);
            } catch (CoreEnvironmentBeanException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }

        jtaPropertyManager.getJTAEnvironmentBean()
                .setXaRecoveryNodes(Collections.singletonList(arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier()));
    }

    /**
     * If recovery modules were not set by property manager, then set defaults {@link #DEFAULT_RECOVERY_MODULES}.
     */
    private void initRecoveryModules() {
        if (!recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryModuleClassNames().isEmpty()) {
            return;
        }

        LOGGER.fine("Recovery modules were not enabled. Enabling default modules: " + DEFAULT_RECOVERY_MODULES);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(DEFAULT_RECOVERY_MODULES);
    }

    /**
     * If orphan filters were not set by property manager, then set defaults {@link #DEFAULT_ORPHAN_FILTERS}.
     */
    private void initOrphanFilters() {
        if (!jtaPropertyManager.getJTAEnvironmentBean().getXaResourceOrphanFilterClassNames().isEmpty()) {
            return;
        }

        LOGGER.fine("Orphan filters were not enabled. Enabling default filters: " + DEFAULT_ORPHAN_FILTERS);
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceOrphanFilterClassNames(DEFAULT_ORPHAN_FILTERS);
    }

    /**
     * If expiry scanners were not set by property manager, then set defaults {@link #DEFAULT_EXPIRY_SCANNERS}.
     */
    private void initExpiryScanners() {
        if (!recoveryPropertyManager.getRecoveryEnvironmentBean().getExpiryScannerClassNames().isEmpty()) {
            return;
        }

        LOGGER.fine("Expiry scanners were not enabled. Enabling default scanners: " + DEFAULT_EXPIRY_SCANNERS);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setExpiryScannerClassNames(DEFAULT_EXPIRY_SCANNERS);
    }

}