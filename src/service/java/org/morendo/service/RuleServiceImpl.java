package org.morendo.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.morendo.rete.Rete;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RuleServiceImpl implements RuleService {

    private Logger log = LogManager.getLogger(RuleServiceImpl.class);
    private long totalResponseTime = 0;
    private long averageResponseTime = 0;
    private long averageRulesFired = 0;
    private long requests = 0;
    private long totalRulesFired = 0;
    private String serviceName = null;
    private List<Object> applications = new ArrayList<>();
    private Map<String, RuleApplication> applicationMap = new HashMap<>();
    private final Map<String, EnginePool> pools = new java.util.concurrent.ConcurrentHashMap<>();
    private ServiceConfiguration serviceConfiguration = null;
    private ServiceAdministration administration = null;
    private static ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("this-escape") // the administration object refers back to its service
    public RuleServiceImpl() {
        applications = new ArrayList<>();
        administration = new ServiceAdministrationImpl(this);
    }

    public ServiceAdministration getServiceAdmin() {
        return this.administration;
    }

    public long getAverageResponseTime() {
        return this.averageResponseTime;
    }

    public long getAverageRulesFired() {
        return this.averageRulesFired;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public long getRequests() {
        return this.requests;
    }

    public long getTotalRulesFired() {
        return this.totalRulesFired;
    }

    public void initialize() {
        log.info("--- Start initializing RuleService ---");
        for (int idx = 0; idx < applications.size(); idx++) {
            RuleApplication application = (RuleApplication) this.applications.get(idx);
            String key = application.getName() + "::" + application.getVersion();
            EnginePool pool = new EnginePool(application);
            this.applicationMap.put(key, application);
            this.pools.put(key, pool);
            pool.fill();
        }
        log.info("--- End initializing RuleService ---");
    }

    /** Reinitialize calls close and then initialize. */
    public void reinitialize() {
        close();
        initialize();
    }

    /** Close method iterates over all the engine instances and calls Rete.close() */
    public void close() {
        log.info("--- Start closing RuleService ---");
        for (EnginePool pool : this.pools.values()) {
            pool.closeAll();
        }
        this.pools.clear();
        Iterator<String> itr = this.applicationMap.keySet().iterator();
        while (itr.hasNext()) {
            String key = itr.next();
            RuleApplicationImpl app = (RuleApplicationImpl) this.applicationMap.remove(key);
            app.close();
        }
        this.applicationMap.clear();
        log.info("--- End closing RuleService ---");
    }

    public EngineContext getEngine(String applicationName, String version) {
        EnginePool pool = getEnginePool(applicationName, version);
        if (pool == null) {
            return null;
        }
        try {
            Rete engine = pool.checkOut(pool.getApplication().getCheckoutTimeout());
            if (engine == null) {
                log.warn(
                        "no engine free for "
                                + applicationName
                                + " within "
                                + pool.getApplication().getCheckoutTimeout()
                                + " ms; the pool holds "
                                + pool.createdCount()
                                + " of at most "
                                + pool.getApplication().getMaxPool());
                return null;
            }
            return new EngineContextImpl(this, engine, applicationName, version);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public EnginePool getEnginePool(String applicationName, String version) {
        return this.pools.get(applicationName + "::" + version);
    }

    public void setAverageResponseTime(long milliseconds) {
        this.averageResponseTime = milliseconds;
    }

    public void setAverageRulesFired(long average) {
        this.averageRulesFired = average;
    }

    public void setServiceName(String name) {
        this.serviceName = name;
    }

    public void setRequests(long requests) {
        this.requests = requests;
    }

    public void setTotalRulesFired(long count) {
        this.totalRulesFired = count;
    }

    public List<Object> getRuleApplications() {
        return applications;
    }

    public void queueEngine(String application, String version, org.morendo.rete.Rete engine) {
        EnginePool pool = getEnginePool(application, version);
        if (pool != null) {
            pool.checkIn(engine);
        } else {
            engine.close();
        }
    }

    public synchronized void updateStatistics(long time, int rulesFired) {
        this.requests++;
        this.totalResponseTime += time;
        this.averageResponseTime = this.totalResponseTime / this.requests;
        this.totalRulesFired += rulesFired;
        this.averageRulesFired = this.totalRulesFired / this.requests;
    }

    public static RuleService createInstance(String filepath) {
        FileReader reader;
        try {
            reader = new FileReader(filepath);
            ServiceConfiguration config = mapper.readValue(reader, ServiceConfiguration.class);
            RuleServiceImpl ruleService = new RuleServiceImpl();
            for (RuleApplicationBean bean : config.getApplications()) {
                RuleApplicationImpl application = new RuleApplicationImpl();
                application.readBean(bean);
                ruleService.applications.add(application);
            }
            ruleService.setServiceName(config.getServiceName());
            ruleService.serviceConfiguration = config;
            return ruleService;
        } catch (Exception e) {
            Logger log = LogManager.getLogger(RuleApplicationImpl.class);
            log.fatal(e.toString(), e);
        }
        return null;
    }

    public static void saveConfiguration(String filename, ServiceConfiguration configuration) {
        File output = new File(filename.substring(0, filename.lastIndexOf('/')));
        output.mkdirs();
        FileWriter writer;
        try {
            writer = new FileWriter(filename);
            mapper.writeValue(writer, configuration);
            writer.close();
        } catch (IOException e) {
            Logger log = LogManager.getLogger(RuleApplicationImpl.class);
            log.fatal(e.toString(), e);
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

    public void setServiceConfiguration(ServiceConfiguration serviceConfiguration) {
        this.serviceConfiguration = serviceConfiguration;
    }
}
