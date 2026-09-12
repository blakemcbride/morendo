/*
 * Copyright 2002-2010 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://ruleml-dev.sourceforge.net/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.morendo.service.servlet;

// import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.morendo.rete.Rete;
import org.morendo.service.EngineContext;
import org.morendo.service.EnginePool;
import org.morendo.service.RuleApplication;
import org.morendo.service.RuleApplicationBean;
import org.morendo.service.RuleApplicationImpl;
import org.morendo.service.RuleService;
import org.morendo.service.ServiceAdministration;
import org.morendo.service.ServiceConfiguration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RuleStartupService implements ServletContextListener, RuleService {

    public static final String CONFIGURATION_FILE = "Ruleconfig.json";
    private long totalResponseTime = 0;
    private long averageResponseTime = 0;
    private long averageRulesFired = 0;
    private long requests = 0;
    private long totalRulesFired = 0;
    private String serviceName = null;
    private List<RuleApplicationImpl> applications = new ArrayList<>();
    private Map<String, RuleApplication> applicationMap = new HashMap<>();
    private final Map<String, EnginePool> pools = new java.util.concurrent.ConcurrentHashMap<>();
    protected ServiceConfiguration serviceConfiguration = null;
    private ServletServiceAdmin administration = null;
    protected ServletContext servletContext = null;
    private static ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("this-escape") // the administration object refers back to its service
    public RuleStartupService() {
        applications = new ArrayList<>();
        administration = new ServletServiceAdmin(this);
    }

    public void contextDestroyed(ServletContextEvent context) {
        this.close();
    }

    public void contextInitialized(ServletContextEvent context) {
        this.servletContext = context.getServletContext();
        administration.setServletContext(this.servletContext);
        this.serviceConfiguration = this.loadConfiguration();
        servletContext.log("--- configuration loaded from Ruleconfig.json ---");
        this.readRuleApps();
        this.serviceName = this.serviceConfiguration.getServiceName();
        this.initialize();
        context.getServletContext().setAttribute(this.serviceName, this);
        servletContext.log("--- RuleService has been initialized ---");
    }

    private void readRuleApps() {
        for (RuleApplicationBean rab : this.serviceConfiguration.getApplications()) {
            RuleApplicationImpl rai = new RuleApplicationImpl();
            rai.readBean(rab);
            this.applications.add(rai);
        }
    }

    public void close() {
        servletContext.log("--- Start closing RuleService ---");
        for (EnginePool pool : this.pools.values()) {
            pool.closeAll();
        }
        this.pools.clear();
        Iterator<String> itr = this.applicationMap.keySet().iterator();
        while (itr.hasNext()) {
            String key = itr.next();
            RuleApplication app = this.applicationMap.remove(key);
            app.close();
        }
        this.applicationMap.clear();
        servletContext.log("--- End closing RuleService ---");
    }

    public long getAverageResponseTime() {
        return averageResponseTime;
    }

    public long getAverageRulesFired() {
        return averageRulesFired;
    }

    public EngineContext getEngine(String applicationName, String version) {
        EnginePool pool = getEnginePool(applicationName, version);
        if (pool == null) {
            return null;
        }
        try {
            Rete engine = pool.checkOut(pool.getApplication().getCheckoutTimeout());
            if (engine == null) {
                this.servletContext.log(
                        "No engine free for "
                                + applicationName
                                + " within "
                                + pool.getApplication().getCheckoutTimeout()
                                + " ms; the pool holds "
                                + pool.createdCount()
                                + " of at most "
                                + pool.getApplication().getMaxPool()
                                + ". Try increasing the configuration.");
                return null;
            }
            return new ServletEngineContext(
                    this, engine, applicationName, version, this.servletContext);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public EnginePool getEnginePool(String applicationName, String version) {
        return this.pools.get(applicationName + "::" + version);
    }

    public long getRequests() {
        return requests;
    }

    public List<RuleApplicationImpl> getRuleApplications() {
        return applications;
    }

    public ServiceAdministration getServiceAdmin() {
        return administration;
    }

    public String getServiceName() {
        return serviceName;
    }

    public long getTotalRulesFired() {
        return totalRulesFired;
    }

    public void initialize() {
        this.servletContext.log("--- Start initializing RuleService ---");
        for (int idx = 0; idx < applications.size(); idx++) {
            RuleApplication application = this.applications.get(idx);
            ((RuleApplicationImpl) application).setServletContext(this.servletContext);
            String key = application.getName() + "::" + application.getVersion();
            EnginePool pool = new EnginePool(application);
            this.applicationMap.put(key, application);
            this.pools.put(key, pool);
            pool.fill();
        }
        this.servletContext.log("--- End initializing RuleService ---");
    }

    public void reinitialize() {
        close();
        initialize();
    }

    public void setServiceName(String name) {
        this.serviceName = name;
    }

    public synchronized void updateStatistics(long time, int rulesFired) {
        this.requests++;
        this.totalResponseTime += time;
        this.averageResponseTime = this.totalResponseTime / this.requests;
        this.totalRulesFired += rulesFired;
        this.averageRulesFired = this.totalRulesFired / this.requests;
    }

    public void queueEngine(String application, String version, org.morendo.rete.Rete engine) {
        EnginePool pool = getEnginePool(application, version);
        if (pool != null) {
            pool.checkIn(engine);
        } else {
            engine.close();
        }
    }

    public Map<String, RuleApplication> getRuleApplicationMap() {
        return this.applicationMap;
    }

    /** The pools by application key, name::version. */
    public Map<String, EnginePool> getEnginePools() {
        return this.pools;
    }

    public ServiceConfiguration getServiceConfiguration() {
        return serviceConfiguration;
    }

    public void setServiceConfiguration(ServiceConfiguration config) {
        this.serviceConfiguration = config;
    }

    protected ServiceConfiguration loadConfiguration() {
        if (this.serviceConfiguration == null) {
            String path = "/WEB-INF/" + CONFIGURATION_FILE;
            this.servletContext.log("Path: " + path);
            InputStream input = this.servletContext.getResourceAsStream(path);
            ServiceConfiguration config;
            try {
                config = mapper.readValue(input, ServiceConfiguration.class);
                this.serviceConfiguration = config;
            } catch (Exception e) {
                this.servletContext.log(" -- loadConfiguration -- " + e.getMessage());
            }
            return this.serviceConfiguration;
        } else {
            return this.serviceConfiguration;
        }
    }
}
